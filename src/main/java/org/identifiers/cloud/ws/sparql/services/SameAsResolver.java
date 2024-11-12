package org.identifiers.cloud.ws.sparql.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.sparql.data.resolution_models.EndpointResponse;
import org.identifiers.cloud.ws.sparql.data.resolution_models.Namespace;
import org.identifiers.cloud.ws.sparql.data.resolution_models.Resource;
import org.identifiers.cloud.ws.sparql.data.URIextended;

import org.springframework.stereotype.Service;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.lang.Boolean.FALSE;


@Slf4j
@Service
public final class SameAsResolver {
    // Perhaps reimplement this class with a Radix Tree (also known as patricia trie, radix trie or compact prefix tree)
    // or a normal Trie tree for faster resolution. See https://github.com/npgall/concurrent-trees

    private List<PrefixPatterns> prefixPatterns;

    public void parseResolverDataset(String json) throws JsonProcessingException {
        parseResolverDataset(EndpointResponse.fromJson(json));
    }

    public void parseResolverDataset(EndpointResponse endpointResponse) throws JsonProcessingException {
        prefixPatterns = new ArrayList<>();
        for (Namespace namespace : endpointResponse.namespaces()) {
            PrefixPatterns prefixPattern = new PrefixPatterns(namespace.pattern());

            for (Resource resource : namespace.resources()) {
                add(prefixPattern, namespace.prefix(), resource.urlPattern(),
                        resource.deprecated(), namespace.namespaceEmbeddedInLui());
            }
            if (!namespace.namespaceEmbeddedInLui()) {
                add(prefixPattern,false,
                        "https://identifiers.org/" + namespace.prefix() + ":{$id}");
                add(prefixPattern,false,
                        "http://identifiers.org/" + namespace.prefix() + ":{$id}");
                add(prefixPattern,true,
                        "https://identifiers.org/" + namespace.prefix() + "/{$id}");
                add(prefixPattern,true,
                        "http://identifiers.org/" + namespace.prefix() + "/{$id}");
            } else {
                add(prefixPattern, false,
                        "http://identifiers.org/{$id}");
                add(prefixPattern, false,
                        "https://identifiers.org/{$id}");
            }
            prefixPatterns.add(prefixPattern);
        }
        log.debug("Parsed {} prefix patterns from resolver dataset", prefixPatterns.size());
    }

    private void add(PrefixPatterns prefixPattern, boolean deprecated, String urlPattern) {
        add(prefixPattern, null, urlPattern, deprecated, false);
    }

    private void add(PrefixPatterns prefixPattern, String prefix, String urlPattern,
                     boolean deprecated, boolean namespaceEmbeddedInLui) {
        final String idString = "{$id}";
        int startIndexOf = urlPattern.indexOf(idString);
        int endIndexOf = startIndexOf + idString.length();
        if (namespaceEmbeddedInLui) {
            startIndexOf = startIndexOf - prefix.length() - 1; //Slide to before prefix and sep character
        }

        String beforeId = urlPattern.substring(0, startIndexOf);
        String afterId = urlPattern.substring(endIndexOf);
        prefixPattern.beforeAndAfterId.add(new BeforeAfterActive(beforeId, afterId, !deprecated));
    }

    public List<URIextended> getSameAsURIs(String uri) {
        return getSameAsURIs(uri, Optional.empty());
    }

    public List<URIextended> getSameAsURIs(String uri, boolean activeFlag) {
        return getSameAsURIs(uri, Optional.of(activeFlag));
    }

    public List<URIextended> getSameAsURIs(String uri, Optional<Boolean> activeFlag) {
        log.debug("getSameAsURIs for {}, active flag: {}", uri, activeFlag);
        List<URIextended> resultList = new ArrayList<>();
        for (PrefixPatterns patterns : prefixPatterns) {
            for (BeforeAfterActive beforeAndAfterId : patterns.beforeAndAfterId) {

                if (beforeAndAfterId.matches(uri)) {
                    String id = beforeAndAfterId.id(uri);
                    if (patterns.idPattern == null || patterns.idPattern.matcher(id).matches()) {
                        log.debug("ID pattern {} matches {}", patterns.idPattern, id);
                        addAll(resultList, id, patterns.beforeAndAfterId, uri, activeFlag);
                    }
                }
            }
        }
        return resultList;
    }

    private void addAll(List<URIextended> resultList, String id,
                        List<BeforeAfterActive> beforeAndAfterIds,
                        String uri, Optional<Boolean> activeFlag) {
        for (BeforeAfterActive beforeAndAfterId : beforeAndAfterIds) {
            String newUrl = beforeAndAfterId.beforeId + id + beforeAndAfterId.afterId;
            if (uri.equals(newUrl)) continue;
            if (activeFlag.isEmpty() || Objects.equals(activeFlag.get(), beforeAndAfterId.active)) {
                resultList.add(new URIextended(newUrl, !activeFlag.orElse(FALSE)));
            }
        }
    }

    static class PrefixPatterns {
        private final Pattern idPattern;
        private final List<BeforeAfterActive> beforeAndAfterId = new ArrayList<>();

        public PrefixPatterns(String idPattern) {
            if (idPattern != null && !idPattern.isBlank()) {
                this.idPattern = Pattern.compile(idPattern);
            } else {
                this.idPattern = null;
            }
        }

        public void add(String beforeId, String afterId, boolean active) {
            beforeAndAfterId.add(new BeforeAfterActive(beforeId, afterId, active));
        }
    }

    record BeforeAfterActive (
            String beforeId,
            String afterId,
            boolean active
    ) {
        boolean matches(String uri1) {
            return uri1.startsWith(beforeId) && uri1.endsWith(afterId);
        }
        private String id(String uri) {
            return uri.substring(beforeId.length(), uri.length() - afterId.length());
        }
    }
}
