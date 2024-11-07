package org.identifiers.cloud.ws.sparql.data.sail;

import java.util.Collections;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.common.iteration.UnionIteration;
import org.eclipse.rdf4j.common.transaction.IsolationLevel;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.DefaultEvaluationStrategy;
import org.eclipse.rdf4j.repository.sparql.federation.SPARQLServiceResolver;
import org.eclipse.rdf4j.sail.*;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;
import org.eclipse.rdf4j.sail.helpers.AbstractSailConnection;
import org.identifiers.cloud.ws.sparql.services.SameAsResolver;

public class IdorgConnection extends AbstractSailConnection {
    private final ValueFactory vf;
    private final SameAsResolver sameAsResolver;
    private final SPARQLServiceResolver fd = new SPARQLServiceResolver();

    public IdorgConnection(ValueFactory vf, SameAsResolver sameAsResolver, AbstractSail sailBase) {
        super(sailBase);
        this.vf = vf;
        this.sameAsResolver = sameAsResolver;
    }

    @Override
    protected CloseableIteration<? extends BindingSet> evaluateInternal(TupleExpr tupleExpr,
                                                                        Dataset dataset,
                                                                        BindingSet bindingSet,
                                                                        boolean b) throws SailException {
        try {
            IdorgTripleSource tripleSource = new IdorgTripleSource(vf, sameAsResolver);
            EvaluationStrategy strategy = new DefaultEvaluationStrategy(tripleSource, fd);
            tupleExpr = tupleExpr.clone();

            var baseResultSet = getSailBase().getConnection()
                    .evaluate(tupleExpr, dataset, bindingSet, b);
            var idorgResultSet = strategy.evaluate(tupleExpr, bindingSet);
            return new UnionIteration<>(baseResultSet, idorgResultSet);
        } catch (QueryEvaluationException e) {
            throw new SailException(e);
        }
    }

    @Override
    protected CloseableIteration<? extends Resource> getContextIDsInternal() throws SailException {
        var it = Collections.singletonList(vf.createIRI("id:active")).iterator();
        return new CloseableIteratorIteration<>(it);
    }

    @Override
    protected CloseableIteration<? extends Statement> getStatementsInternal(Resource subj,
                                                                            IRI pred,
                                                                            Value obj,
                                                                            boolean b,
                                                                            Resource... contexts) throws SailException {
		try {
			var bedFileFilterReader = new IdorgTripleSource(vf, sameAsResolver).getStatements(subj, pred, obj, contexts);
            var baseStatements = this.getSailBase().getConnection().getStatements(subj, pred, obj, b, contexts);
            return  new CloseableIteratorIteration<>(new UnionIteration<>(bedFileFilterReader, baseStatements));
		} catch (QueryEvaluationException e1) {
			throw new SailException(e1);
		}
	}

	@Override
	protected long sizeInternal(Resource... resources) throws SailException {
		return resources.length;
	}

	@Override
    public CloseableIteration<? extends Namespace> getNamespacesInternal()
            throws SailException {

        var namespaces = Collections
                .singletonList(new SimpleNamespace(OWL.PREFIX, OWL.NAMESPACE))
                .iterator();

        return new CloseableIteratorIteration<>(namespaces);
    }

    @Override
    public String getNamespaceInternal(String prefix) throws SailException {
        if (OWL.PREFIX.equals(prefix))
            return OWL.NAMESPACE;
        return getSailBase().getConnection().getNamespace(prefix);
    }

    @Override
    public void setNamespaceInternal(String prefix, String name) throws SailException {
        throw new SailException("Identifiers files can not be updated via SPARQL");

    }

    @Override
    public void removeNamespaceInternal(String prefix) throws SailException {
        throw new SailException("Identifiers files can not be updated via SPARQL");

    }

    @Override
    public void clearNamespacesInternal() throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");

    }

    @Override
    public void addStatement(UpdateContext arg0, Resource arg1, IRI arg2,
                             Value arg3, Resource... arg4) throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");
    }

    @Override
    public void begin() throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");

    }

    @Override
    public void endUpdateInternal(UpdateContext arg0) throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");

    }

    @Override
    protected void closeInternal() throws SailException {
    }

    @Override
    public void prepareInternal() throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");
    }

    @Override
    protected void commitInternal() throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");
    }

    @Override
    protected void rollbackInternal() throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");
    }

    @Override
    public void removeStatement(UpdateContext arg0, Resource arg1, IRI arg2,
                                Value arg3, Resource... arg4) throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");
    }

    @Override
    public void startUpdate(UpdateContext arg0) throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");
    }

    @Override
    public void begin(IsolationLevel level) throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");
    }

    @Override
    public void flush() throws SailException {
        throw new SailException("Identifiers.org can not be updated via SPARQL");
    }

	@Override
	protected void addStatementInternal(Resource resource, IRI iri, Value value, Resource... resources) throws SailException {
		throw new SailException("Identifiers files can not be updated via SPARQL");
	}

	@Override
	public void removeStatementsInternal(Resource subj, IRI pred, Value obj,
										 Resource... contexts) throws SailException {
		throw new SailException("Identifiers files can not be updated via SPARQL");
	}

	@Override
	public void clearInternal(Resource... contexts) throws SailException {
		throw new SailException("Identifiers files can not be updated via SPARQL");

	}

	@Override
	protected void startTransactionInternal() throws SailException {
		throw new SailException("Identifiers files can not be updated via SPARQL");

	}
}
