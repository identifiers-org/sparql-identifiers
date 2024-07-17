package org.identifiers.cloud.ws.sparql.controllers;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.identifiers.cloud.ws.sparql.services.EvaluationService;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class SparqlController {
    private static final String[] ALL_GRAPHS = {};
    private final EvaluationService evaluationService;

    @PostMapping(value = "/sparql", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public void sparqlPostURLencoded(
            @RequestParam(value = "default-graph-uri", required = false) String defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String namedGraphUri,
            @RequestParam(value = "query") String query,
            @RequestHeader(ACCEPT) String acceptHeader,
            HttpServletResponse response) throws IOException {
        try {
            evaluationService.evaluate(response, query, acceptHeader,
                    toArray(defaultGraphUri), toArray(namedGraphUri));
        } catch (MalformedQueryException | IllegalStateException | IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }


    @GetMapping(value = "/sparql")
    public void sparqlGet(@RequestParam(value = "default-graph-uri", required = false) String defaultGraphUri,
                          @RequestParam(value = "named-graph-uri", required = false) String namedGraphUri,
                          @RequestParam(value = "query") String query, @RequestHeader(ACCEPT) String acceptHeader,
                          HttpServletResponse response) throws IOException {

        try {
            evaluationService.evaluate(response, query, acceptHeader,
                    toArray(defaultGraphUri), toArray(namedGraphUri));
        } catch (MalformedQueryException | IllegalStateException | IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private String[] toArray(String namedGraphUri) {
        if (Objects.nonNull(namedGraphUri)) {
            return new String[] { namedGraphUri };
        }
        return ALL_GRAPHS;
    }
}
