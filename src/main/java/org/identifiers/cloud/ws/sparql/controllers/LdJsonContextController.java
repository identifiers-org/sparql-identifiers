package org.identifiers.cloud.ws.sparql.controllers;

import lombok.RequiredArgsConstructor;
import org.identifiers.cloud.ws.sparql.data.LdJsonContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", maxAge = 3600)
public class LdJsonContextController {
    private final LdJsonContextService ldJsonContextService;

    @GetMapping(value = "/context", produces = {APPLICATION_JSON_VALUE, "application/ld+json"})
    ResponseEntity<Map<String, Map<String, String>>> getContexts() {
        var jsonLdContexts = ldJsonContextService.getJsonLdContexts();
        if (jsonLdContexts.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok().body(jsonLdContexts);
        }
    }
}
