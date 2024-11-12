package org.identifiers.cloud.ws.sparql.controllers;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.*;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.identifiers.cloud.ws.sparql.services.EvaluationService;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@AllArgsConstructor
@CrossOrigin(originPatterns = "*", maxAge = 3600)
public class SparqlController {
    private static final String[] ALL_GRAPHS = {};
    private final EvaluationService evaluationService;

    @PostMapping(value = "/sparql", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public void sparqlPostURLencoded(
            @RequestParam(value = "default-graph-uri", required = false) String defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String namedGraphUri,
            @RequestParam(value = "query") String query,
            @RequestHeader(value = ACCEPT, defaultValue = APPLICATION_JSON_VALUE) String acceptHeader,
            HttpServletResponse response) throws IOException, HttpMediaTypeNotSupportedException, MalformedQueryException {
        try {
            evaluationService.evaluate(response, query, acceptHeader,
                    toArray(defaultGraphUri), toArray(namedGraphUri));
        } catch (IllegalStateException | IOException e) {
            log.error("error on sparql post", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(value = "/sparql")
    public void sparqlGet(@RequestParam(value = "default-graph-uri", required = false) String defaultGraphUri,
                          @RequestParam(value = "named-graph-uri", required = false) String namedGraphUri,
                          @RequestParam(value = "query") String query,
                          @RequestHeader(value = ACCEPT, defaultValue = APPLICATION_JSON_VALUE) String acceptHeader,
                          HttpServletResponse response) throws IOException, HttpMediaTypeNotSupportedException, MalformedQueryException {

        try {
            evaluationService.evaluate(response, query, acceptHeader,
                    toArray(defaultGraphUri), toArray(namedGraphUri));
        } catch (MalformedQueryException | IllegalStateException | IOException e) {
            log.error("error on sparql get", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(value = "/sparql", produces = {TEXT_HTML_VALUE, APPLICATION_XHTML_XML_VALUE})
    public RedirectView redirectToIndex() {
        // Redirect browser requests to index when users try to go to sparql endpoint URL
        return new RedirectView("/");
    }

    private String[] toArray(String namedGraphUri) {
        if (Objects.nonNull(namedGraphUri)) {
            return new String[] { namedGraphUri };
        }
        return ALL_GRAPHS;
    }
}
