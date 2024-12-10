package org.identifiers.cloud.ws.sparql.data;

import lombok.Getter;
import org.identifiers.cloud.ws.sparql.data.resolution_models.EndpointResponse;
import org.identifiers.cloud.ws.sparql.data.resolution_models.Namespace;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Service
public class LdJsonContextService {
    private Map<String, Map<String, String>> jsonLdContexts = Map.of();

    public void updatePrefixes(EndpointResponse endpointResponse) {
        var expandedPrefixes = endpointResponse.namespaces().stream().map(Namespace::prefix)
                .collect(Collectors.toMap(Function.identity(), this::getContextExpandedPrefix));
        jsonLdContexts = Map.of("@context", expandedPrefixes);
    }

    private String getContextExpandedPrefix(String idorgPrefix) {
        return String.format("http://identifiers.org/%s/", idorgPrefix);
    }
}
