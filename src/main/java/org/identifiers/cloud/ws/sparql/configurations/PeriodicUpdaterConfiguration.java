package org.identifiers.cloud.ws.sparql.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class PeriodicUpdaterConfiguration {
    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Bean
    ScheduledExecutorService updateExecutorService () {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
