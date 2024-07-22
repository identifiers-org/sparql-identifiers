package org.identifiers.cloud.ws.sparql.data;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.sparql.services.SameAsResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.HOURS;

@Slf4j
@Service
public final class ResolutionDatasetUpdater implements Runnable {
    @Value("${org.identifiers.cloud.ws.sparql.updater.resolverDatasetUrl}")
    URI resolverURI;
    @Value("${org.identifiers.cloud.ws.sparql.updater.timeIntervalHours}")
    int timeInterval;

    private final SameAsResolver sameAsResolver;
    private final HttpClient httpClient;
    private final ScheduledExecutorService updateExecutorService;
    public ResolutionDatasetUpdater(HttpClient httpClient,
                                    ScheduledExecutorService updateExecutorService,
                                    SameAsResolver sameAsResolver) {
        this.sameAsResolver = sameAsResolver;
        this.httpClient = httpClient;
        this.updateExecutorService = updateExecutorService;
    }

    @Override
    @PostConstruct
    public void run() {
        this.update();
        updateExecutorService.scheduleAtFixedRate(this, timeInterval, timeInterval, HOURS);
    }

    public void update() {
        log.debug("Updating resolve dataset from {}", resolverURI);
        HttpRequest get = HttpRequest
                .newBuilder(resolverURI)
                .GET()
                .build();

        try {
            BodyHandler<String> bh = BodyHandlers.ofString();
            final HttpResponse<String> send = httpClient.send(get, bh);
            int code = send.statusCode();
            if (code == 200) {
                String json = send.body();
                sameAsResolver.parseResolverDataset(json);
            } else {
                log.error("Failed to update resolution data with HTTP code: {}", code);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Failed to update resolution data", e);
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        }
        log.info("Resolution dataset updated successfully");
    }
}
