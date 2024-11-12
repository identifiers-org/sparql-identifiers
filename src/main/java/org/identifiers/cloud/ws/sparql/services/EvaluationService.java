package org.identifiers.cloud.ws.sparql.services;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.identifiers.cloud.ws.sparql.query_evaluators.QueryEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class EvaluationService {
    private final Repository repository;
    private final List<QueryEvaluator> queryEvaluators;

    public void evaluate(HttpServletResponse result, String query, String acceptHeader,
                          String[] defaultGraphUri, String[] namedGraphUris)
            throws MalformedQueryException, IOException, IllegalStateException, HttpMediaTypeNotSupportedException {
        try (RepositoryConnection connection = repository.getConnection()) {
            Query preparedQuery = connection.prepareQuery(QueryLanguage.SPARQL, query);
            preparedQuery.setDataset(getQueryDataSet(defaultGraphUri, namedGraphUris, connection));

            for (QueryEvaluator qt : queryEvaluators) {
                if (qt.accepts(preparedQuery, acceptHeader)) {
                    qt.evaluateAndRespond(result, preparedQuery, acceptHeader);
                    return;
                }
            }
            String msg = String.format("This SPARQL endpoint cannot respond with %s for queries of type %s",
                    acceptHeader, preparedQuery.getClass().getSimpleName());
            throw new HttpMediaTypeNotSupportedException(msg);
        }
    }

    private Dataset getQueryDataSet(String[] defaultGraphUri, String[] namedGraphUris,
                                    RepositoryConnection connection) {
        SimpleDataset dataset = new SimpleDataset();

        ValueFactory valueFactory = connection.getValueFactory();
        if (defaultGraphUri != null) {
            Arrays.stream(defaultGraphUri)
                    .map(valueFactory::createIRI)
                    .forEach(dataset::addDefaultGraph);
        }

        if (namedGraphUris != null) {
            Arrays.stream(namedGraphUris)
                    .map(valueFactory::createIRI)
                    .forEach(dataset::addNamedGraph);
        }
        return dataset;
    }
}
