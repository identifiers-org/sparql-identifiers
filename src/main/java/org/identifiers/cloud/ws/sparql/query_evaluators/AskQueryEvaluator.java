package org.identifiers.cloud.ws.sparql.query_evaluators;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.resultio.*;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
class AskQueryEvaluator extends QueryEvaluator {
    public AskQueryEvaluator() {
        super(BooleanQuery.class::isInstance, BooleanQueryResultFormat.TEXT,
                BooleanQueryResultFormat.JSON, BooleanQueryResultFormat.SPARQL);
    }

    @Override
    public void doEvaluateAndRespond(HttpServletResponse result, Query q, String acceptHeader)
            throws QueryEvaluationException, RDFHandlerException, UnsupportedRDFormatException, IOException {
        BooleanQuery bq = (BooleanQuery) q;
        QueryResultFormat format = (QueryResultFormat) bestFormat(acceptHeader);
        result.setContentType(format.getDefaultMIMEType());
        var optional = BooleanQueryResultWriterRegistry
                .getInstance()
                .get(format);
        if (optional.isPresent()) {
            BooleanQueryResultWriter writer = optional.get().getWriter(result.getOutputStream());
            writer.handleBoolean(bq.evaluate());
        }
    }
}
