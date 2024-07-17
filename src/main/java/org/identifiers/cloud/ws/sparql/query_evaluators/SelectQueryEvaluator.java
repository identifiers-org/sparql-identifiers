package org.identifiers.cloud.ws.sparql.query_evaluators;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
class SelectQueryEvaluator extends QueryEvaluator {
    public SelectQueryEvaluator() {
        super(TupleQuery.class::isInstance,
                TupleQueryResultFormat.JSON, TupleQueryResultFormat.SPARQL,
                TupleQueryResultFormat.CSV, TupleQueryResultFormat.TSV);
    }

    @Override
    public void doEvaluateAndRespond(HttpServletResponse result, Query q, String acceptHeader)
            throws QueryEvaluationException, RDFHandlerException, UnsupportedRDFormatException, IOException {
        TupleQuery tq = (TupleQuery) q;
        QueryResultFormat format = (QueryResultFormat) bestFormat(acceptHeader);
        result.setContentType(format.getDefaultMIMEType());
        tq.evaluate(QueryResultIO.createTupleWriter(format, result.getOutputStream()));
    }
}
