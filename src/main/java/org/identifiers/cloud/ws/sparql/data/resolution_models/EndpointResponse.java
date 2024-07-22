package org.identifiers.cloud.ws.sparql.data.resolution_models;

import java.util.List;
import java.util.Map;

public record EndpointResponse (
    String apiVersion,
    String errorMessage,
    Map<String, List<Namespace>> payload //This is ugly but I couldn't find a better way
) {
    public List<Namespace> namespaces() {
        return payload.get("namespaces");
    }
}
