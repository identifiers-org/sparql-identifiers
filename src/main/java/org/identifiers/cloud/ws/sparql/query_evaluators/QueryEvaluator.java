package org.identifiers.cloud.ws.sparql.query_evaluators;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import java.io.IOException;
import java.util.function.Predicate;

@Slf4j
public abstract class QueryEvaluator {
    protected final FileFormat[] formats;
    protected final Predicate<Query> typeChecker;

    QueryEvaluator(Predicate<Query> typeChecker, FileFormat... formats) {
        this.typeChecker = typeChecker;
        this.formats = formats;
    }

    public boolean accepts(Query preparedQuery, String acceptHeader) throws IllegalStateException {
        if (accepts(preparedQuery)) {
            if (acceptHeader == null || acceptHeader.isEmpty()) {
                return true;
            } else {
                for (FileFormat format : formats) {
                    boolean isMimeTypeAccepted = format.getMIMETypes()
                            .stream().anyMatch(acceptHeader::contains);
                    if (isMimeTypeAccepted) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean accepts(Query q) {
        return typeChecker.test(q);
    }

    protected FileFormat bestFormat(String acceptHeader) {
        if (acceptHeader == null || acceptHeader.isEmpty()) {
            return formats[0];
        } else {
            for (FileFormat format : formats) {
                for (String mimeType : format.getMIMETypes()) {
                    if (acceptHeader.contains(mimeType)) {
                        return format;
                    }
                }
            }
        }
        return formats[0];
    }

    public void evaluateAndRespond(HttpServletResponse result, Query q, String acceptHeader)
            throws QueryEvaluationException, RDFHandlerException, UnsupportedRDFormatException, IOException {
        log.debug("Evaluating {} for query {}", acceptHeader, q);
        doEvaluateAndRespond(result, q, acceptHeader);
    }

    public abstract void doEvaluateAndRespond(HttpServletResponse result, Query q, String acceptHeader)
            throws QueryEvaluationException, RDFHandlerException, UnsupportedRDFormatException, IOException;
}
