package org.identifiers.cloud.ws.sparql.data.resolution_models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public record EndpointResponse (
    String apiVersion,
    String errorMessage,
    Map<String, List<Namespace>> payload //This is ugly, but I couldn't find a better way
) {
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<Namespace> namespaces() {
        return payload.get("namespaces");
    }

    public static EndpointResponse fromJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, EndpointResponse.class);
    }
}
