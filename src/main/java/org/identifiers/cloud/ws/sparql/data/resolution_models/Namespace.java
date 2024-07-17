package org.identifiers.cloud.ws.sparql.data.resolution_models;

import java.util.List;

public record Namespace (
    String prefix,
    String pattern,
    boolean namespaceEmbeddedInLui,
    List<Resource> resources
) {}
