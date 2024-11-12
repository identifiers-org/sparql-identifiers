package org.identifiers.cloud.ws.sparql.query_evaluators;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
class GraphQueryEvaluator extends QueryEvaluator {
    public GraphQueryEvaluator() {
        super(GraphQuery.class::isInstance,
                RDFFormat.TURTLE, RDFFormat.NTRIPLES, RDFFormat.JSONLD,
                RDFFormat.RDFXML, RDFFormat.TRIG, RDFFormat.NQUADS,
                RDFFormat.RDFJSON);
    }

    @Override
    public void doEvaluateAndRespond(HttpServletResponse result, Query q, String acceptHeader)
            throws QueryEvaluationException, RDFHandlerException, UnsupportedRDFormatException, IOException {
        GraphQuery gq = (GraphQuery) q;
        RDFFormat format = (RDFFormat) bestFormat(acceptHeader);
        result.setContentType(format.getDefaultMIMEType());
        gq.evaluate(Rio.createWriter(format, result.getOutputStream()));
    }
}
