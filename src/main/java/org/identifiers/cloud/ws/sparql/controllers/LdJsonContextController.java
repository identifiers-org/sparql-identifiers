package org.identifiers.cloud.ws.sparql.controllers;

import lombok.RequiredArgsConstructor;
import org.identifiers.cloud.ws.sparql.data.LdJsonContextService;
import org.springframework.http.MediaType;
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
    private final MediaType ldJsonMediaType = MediaType.parseMediaType("application/ld+json");

    @GetMapping(value = "/context")
    ResponseEntity<Map<String, Map<String, String>>> getContexts() {
        var jsonLdContexts = ldJsonContextService.getJsonLdContexts();
        if (jsonLdContexts.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok()
                    .contentType(ldJsonMediaType)
                    .body(jsonLdContexts);
        }
    }
}
