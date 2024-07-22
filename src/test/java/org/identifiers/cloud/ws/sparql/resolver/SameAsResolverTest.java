package org.identifiers.cloud.ws.sparql.resolver;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.identifiers.cloud.ws.sparql.data.URIextended;
import org.identifiers.cloud.ws.sparql.services.SameAsResolver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SameAsResolverTest {
    private static final SameAsResolver SAME_AS_RESOLVER = new SameAsResolver();

    @BeforeAll
    public static void before() throws IOException {
        File file = ResourceUtils.getFile("classpath:resolutionDataset.json");
        String json = IOUtils.toString(
                Files.readAllBytes(file.toPath()),
                Charset.defaultCharset().name()
        );
        SAME_AS_RESOLVER.parseResolverDataset(json);
    }

    @Test
    void testUniProt() {
        final String in = "http://purl.uniprot.org/uniprot/P05067";
        List<URIextended> sameAsURIs = SAME_AS_RESOLVER.getSameAsURIs(in, true);
        assertNotNull(sameAsURIs);
        assertTrue(sameAsURIs.contains(new URIextended("https://identifiers.org/uniprot:P05067", false)));
        sameAsURIs = SAME_AS_RESOLVER.getSameAsURIs(in, false);
        assertTrue(sameAsURIs.contains(new URIextended("https://identifiers.org/uniprot/P05067", true)));
    }

    @Test
    void testChebi() {
        final String in = "http://purl.bioontology.org/ontology/CHEBI/CHEBI:36927";
        List<URIextended> sameAsURIs = SAME_AS_RESOLVER.getSameAsURIs(in, true);
        assertNotNull(sameAsURIs);

        var expectedUri = new URIextended("https://www.ebi.ac.uk/ols4/ontologies/chebi/terms?obo_id=CHEBI:36927", false);
        assertTrue(sameAsURIs.contains(expectedUri));
    }
}
