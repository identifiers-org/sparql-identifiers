package org.identifiers.cloud.ws.sparql.data.sail;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.identifiers.cloud.ws.sparql.data.URIextended;
import org.identifiers.cloud.ws.sparql.services.SameAsResolver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.function.Predicate.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class IdorgTripleSourceTest {

    private static final int GO_RESOURCES = 14;
    static final SameAsResolver mockedSameAsResolver = mock();

    static final List<URIextended> GO_URIS = List.of(
            new URIextended("http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915", true),
            new URIextended("https://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915", false),
            new URIextended("http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=GO:0006915", false),
            new URIextended("http://www.bioinf.ebc.ee/EP/EP/GO/?Q=GO:0006915", true),
            new URIextended("http://www.informatics.jax.org/searches/GO.cgi?id=GO:0006915", false),
            new URIextended("http://www.pantherdb.org/panther/category.do?categoryAcc=GO:0006915", false),
            new URIextended("http://amigo2.berkeleybop.org/cgi-bin/amigo2/amigo/term/GO:0006915", false),
            new URIextended("http://purl.uniprot.org/go/0006915", false),
            new URIextended("http://bio2rdf.org/GO:0006915", false),
            new URIextended("http://identifiers.org/go/GO:0006915", false),
            new URIextended("urn:miriam:go:GO:0006915", false),
            new URIextended("http://www.geneontology.org/GO:0006915", false),
            new URIextended("urn:miriam:obo.go:GO:0006915", false),
            new URIextended("http://identifiers.org/obo.go/GO:0006915", false));

    static final List<URIextended> UNIPROT_URIS = List.of(
            new URIextended("http://www.ebi.uniprot.org/entry/P05067", true),
            new URIextended("http://www.pir.uniprot.org/cgi-bin/upEntry?id=P05067", true),
            new URIextended("http://us.expasy.org/uniprot/P05067", true),
            new URIextended("http://www.uniprot.org/uniprot/P05067", false),
            new URIextended("http://purl.uniprot.org/uniprot/P05067", false),
            new URIextended("http://www.ncbi.nlm.nih.gov/protein/P05067", false),
            new URIextended("http://identifiers.org/uniprot/P05067", false)
    );

    @BeforeAll
    static void setupMock() {
        doReturn(Collections.emptyList())
                .when(mockedSameAsResolver)
                .getSameAsURIs(anyString(), anyBoolean());

        doReturn(GO_URIS.stream().filter(not(URIextended::obsolete)).toList())
                .when(mockedSameAsResolver)
                .getSameAsURIs(endsWith("www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915"), eq(true));
        doReturn(GO_URIS.stream().filter(not(URIextended::obsolete)).toList())
                .when(mockedSameAsResolver)
                .getSameAsURIs(endsWith("www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915"), eq(Optional.of(true)));
        doReturn(GO_URIS.stream().filter(URIextended::obsolete).toList())
                .when(mockedSameAsResolver)
                .getSameAsURIs(endsWith("www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915"), eq(false));
        doReturn(GO_URIS.stream().filter(URIextended::obsolete).toList())
                .when(mockedSameAsResolver)
                .getSameAsURIs(endsWith("www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915"), eq(Optional.of(false)));
        doReturn(GO_URIS)
                .when(mockedSameAsResolver)
                .getSameAsURIs(endsWith("www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915"), eq(Optional.empty()));

        doReturn(UNIPROT_URIS.stream().filter(not(URIextended::obsolete)).toList())
                .when(mockedSameAsResolver)
                .getSameAsURIs(contains("uniprot"), eq(true));
        doReturn(UNIPROT_URIS.stream().filter(URIextended::obsolete).toList())
                .when(mockedSameAsResolver)
                .getSameAsURIs(contains("uniprot"), eq(false));
        doReturn(UNIPROT_URIS.stream().filter(not(URIextended::obsolete)).toList())
                .when(mockedSameAsResolver)
                .getSameAsURIs(contains("uniprot"), eq(Optional.of(true)));
        doReturn(UNIPROT_URIS.stream().filter(URIextended::obsolete).toList())
                .when(mockedSameAsResolver)
                .getSameAsURIs(contains("uniprot"), eq(Optional.of(false)));
        doReturn(UNIPROT_URIS)
                .when(mockedSameAsResolver)
                .getSameAsURIs(contains("uniprot"), eq(Optional.empty()));
    }

    private File dataDir;

    @BeforeEach
    public void setUp(@TempDir File tempDir) {
        dataDir = new File(tempDir, "data.dir");
        if (!dataDir.mkdirs()) {
            throw new IllegalStateException("Failed to create test folder!");
        }
    }

    String query1 = """
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            SELECT ?target WHERE {
                <http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915> owl:sameAs ?target
            }""";

    String query1https = """
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            SELECT ?target WHERE {
                <https://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915> owl:sameAs ?target
            }""";

    @Test
    void testBasicMatch() throws QueryEvaluationException, MalformedQueryException,
            RepositoryException, SailException {

        var conn = getConnection();
        TupleQuery pTQ = conn.prepareTupleQuery(QueryLanguage.SPARQL, query1);
        try (TupleQueryResult eval = pTQ.evaluate()) {
            for (int i = 0; i < GO_RESOURCES; i++) {
                assertTrue(eval.hasNext());
                final BindingSet next = eval.next();
                assertNotNull(next);
                assertTrue(next.getBinding("target").getValue().toString().endsWith("0006915"));
            }
            assertFalse(eval.hasNext());
        }
    }

    @Test
    void testBasicHttpsMatch() throws QueryEvaluationException, MalformedQueryException,
            RepositoryException, SailException {

        var conn = getConnection();
        TupleQuery pTQ = conn.prepareTupleQuery(QueryLanguage.SPARQL, query1https);
        try (TupleQueryResult eval = pTQ.evaluate()) {
            for (int i = 0; i < GO_RESOURCES; i++) {
                assertTrue(eval.hasNext());
                final BindingSet next = eval.next();
                assertNotNull(next);
                assertTrue(next.getBinding("target").getValue().toString().endsWith("0006915"));
            }
            assertFalse(eval.hasNext());
        }
    }

    String query2 = """
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                SELECT ?target
                WHERE {
                    <http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915> owl:AllDifferent ?target
                }""";

    @Test
    void testNoResultsForNotOWLSameAs() throws QueryEvaluationException, MalformedQueryException,
            RepositoryException, SailException {
        var conn = getConnection();
        TupleQuery pTQ = conn.prepareTupleQuery(QueryLanguage.SPARQL, query2);
        try (TupleQueryResult eval = pTQ.evaluate()) {
            assertFalse(eval.hasNext());
        }
    }

    String query3 = """
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                SELECT ?target
                WHERE {
                    <http://www.ebi.uniprot.org/entry/P05067> owl:sameAs ?target
                }""";

    @Test
    void testBasicUniProt() throws QueryEvaluationException, MalformedQueryException,
            RepositoryException, SailException {

        var conn = getConnection();
        TupleQuery pTQ = conn.prepareTupleQuery(QueryLanguage.SPARQL, query3);
        try (TupleQueryResult eval = pTQ.evaluate()) {
            for (int i = 0; i < 7; i++) {
                assertTrue(eval.hasNext());
                final BindingSet next = eval.next();
                assertNotNull(next);
                assertTrue(next.getBinding("target").getValue().toString().endsWith("P05067"));
            }
            assertFalse(eval.hasNext());
        }
    }

    String query4 = """
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                ASK {
                    <http://www.ebi.uniprot.org/entry/P05067> owl:sameAs <http://www.uniprot.org/uniprot/P05067>
                }""";

    @Test
    void testBasicUniProtSameAs() throws QueryEvaluationException, MalformedQueryException,
            RepositoryException, SailException {

        var conn = getConnection();
        BooleanQuery pTQ = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query4);
        assertTrue(pTQ.evaluate(), "Should return true");
    }

    String query5 = """
            PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
            PREFIX  up:   <http://purl.uniprot.org/core/>
            SELECT  ?target
            WHERE {
                <http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915> owl:sameAs ?target
                BIND(str(?target) as ?goa) .
            	FILTER ( STRSTARTS(?goa, "http://purl.uniprot.org") )
            }""";

    @Test
    void testBasicUniProtFilter() throws QueryEvaluationException, MalformedQueryException,
            RepositoryException, SailException {

        var conn = getConnection();
        TupleQuery pTQ = conn.prepareTupleQuery(QueryLanguage.SPARQL, query5);
        try (TupleQueryResult eval = pTQ.evaluate()) {
            for (int i = 0; i < 1; i++) {
                assertTrue(eval.hasNext());
                final BindingSet next = eval.next();
                assertNotNull(next);
                assertTrue(
                        next.getBinding("target").getValue().toString().endsWith("0006915"),
                        "Expect one more answer"
                );
            }
            assertFalse(eval.hasNext());
        }
    }

    String query6 = """
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                SELECT ?target
                FROM <id:active>
                WHERE {<http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915> owl:sameAs ?target}""";

    String query7 = """
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                SELECT ?target
                WHERE {
                    GRAPH <id:active> {
                        <http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0006915> owl:sameAs ?target
                    }
                }""";

    @Test
    void testActiveGraph() throws QueryEvaluationException, MalformedQueryException,
            RepositoryException, SailException {
        var conn = getConnection();
        TupleQuery pTQ = conn.prepareTupleQuery(QueryLanguage.SPARQL, query6);
        try (TupleQueryResult eval = pTQ.evaluate()) {
            for (int i = 0; i < 12; i++) {
                assertTrue(eval.hasNext());
                final BindingSet next = eval.next();
                assertNotNull(next);
                assertTrue(next.getBinding("target").getValue().toString().endsWith("0006915"));
            }
        }
        pTQ = conn.prepareTupleQuery(QueryLanguage.SPARQL, query7);
        try (TupleQueryResult eval = pTQ.evaluate()) {
            for (int i = 0; i < 12; i++) {
                assertTrue(eval.hasNext());
                final BindingSet next = eval.next();
                assertNotNull(next);
                assertTrue(next.getBinding("target").getValue().toString().endsWith("0006915"));
            }
            assertFalse(eval.hasNext());
        }
        pTQ = conn.prepareTupleQuery(QueryLanguage.SPARQL, query1);
        try (TupleQueryResult eval = pTQ.evaluate()) {
            for (int i = 0; i < GO_RESOURCES; i++) {
                assertTrue(eval.hasNext());
                final BindingSet next = eval.next();
                assertNotNull(next);
                assertTrue(next.getBinding("target").getValue().toString().endsWith("0006915"));
            }
            assertFalse(eval.hasNext());
        }
    }

    @Test
    void testGo() throws QueryEvaluationException, MalformedQueryException,
            RepositoryException, SailException {

        var conn = getConnection();
        TupleQuery pTQ = conn.prepareTupleQuery(QueryLanguage.SPARQL, query1https);
        try (TupleQueryResult eval = pTQ.evaluate()) {
            while (eval.hasNext()) {
                final BindingSet next = eval.next();
                assertNotNull(next);
                String target = next.getBinding("target").getValue().toString();
                assertTrue(StringUtils.containsIgnoreCase(target, "GO"));
            }
        }
    }

    private SailRepositoryConnection getConnection() {
        IdorgStore rep = new IdorgStore(mockedSameAsResolver);
        rep.setBaseSail(new MemoryStore());
        rep.setDataDir(dataDir);
        rep.setValueFactory(SimpleValueFactory.getInstance());
        SailRepository sr = new SailRepository(rep);
        rep.init();
        return sr.getConnection();
    }
}
