package org.identifiers.cloud.ws.sparql.data.sail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.identifiers.cloud.ws.sparql.data.URIextended;
import org.identifiers.cloud.ws.sparql.services.SameAsResolver;

@Slf4j
public class IdorgTripleSource implements TripleSource {

	private final ValueFactory vf;
	private final SameAsResolver sameAsResolver;

	public IdorgTripleSource(ValueFactory vf, SameAsResolver sameAsResolver) {
		this.vf = vf;
		this.sameAsResolver = sameAsResolver;
	}

	@Override
	public CloseableIteration<Statement> getStatements(Resource subj, IRI pred, Value obj,
			Resource... contexts) throws QueryEvaluationException {
		log.debug("getStatements for {} {} {}", subj, pred, obj);
		// Any predicate that is not OWL.SAMEAS is equivalent to an empty list
		if (!OWL.SAMEAS.equals(pred)) {
			return new EmptyIteration<>();
		}
		// If both sides are variables we can't do a conversion so return
		// nothing
		if (subj == null && obj == null) {
			return new EmptyIteration<>();
		}
		final Iterator<Statement> iter = getIterViaSameAsResolver(subj, obj, contexts);
		return new CloseableIteratorIteration<>(iter);
	}

	private Iterator<Statement> getIterViaSameAsResolver(Resource subj, Value obj, Resource... contexts) {
		if (obj instanceof Resource resource) {
			return getIterViaSameAsResolver(subj, resource, contexts);
		} else {
			return getIterViaSameAsResolver(subj, contexts);
		}
	}

	private CloseableIteration<Statement> getIterViaSameAsResolver(final Resource subj, Resource... contexts) {
		Optional<Boolean> activeflag = getActiveFlag(contexts);
		final String stringValue = subj.stringValue();
		final List<URIextended> sameAsURIs = sameAsResolver.getSameAsURIs(stringValue, activeflag);
		final Iterator<Statement> iter = sameAsURIs
				.stream()
				.map(urIextended -> {
					if (urIextended.urlPattern() != null) {
						IRI uri = vf.createIRI(urIextended.urlPattern());
						return vf.createStatement(subj, OWL.SAMEAS, uri);
					} else {
						return vf.createStatement(subj, OWL.SAMEAS, vf.createBNode());
					}
				})
				.iterator();

		return new CloseableIteratorIteration<>(iter);
	}

	private CloseableIteration<Statement> getIterViaSameAsResolver(final Resource subj,
																   final Resource obj,
																   Resource... contexts) {

		Optional<Boolean> activeflag = getActiveFlag(contexts);
		if (subj == null) {
			var uris = sameAsResolver.getSameAsURIs(obj.stringValue(), activeflag);
			final Iterator<Statement> iter = uris.stream()
					.map(uriExtended -> {
						IRI uri = vf.createIRI(uriExtended.urlPattern());
						return vf.createStatement(subj, OWL.SAMEAS, uri);
					})
					.iterator();
			// For each result we had from translation we now turn them into a
			// statement.
			return new CloseableIteratorIteration<>(iter);
		} else {
			var uris = sameAsResolver.getSameAsURIs(obj.stringValue(), activeflag);
			final Iterator<URIextended> iter = uris.iterator();
			List<Statement> l = new ArrayList<>(1);
			while (iter.hasNext()) {
				final URIextended next = iter.next();
				if (subj.stringValue().equals(next.urlPattern())) {
					l.add(vf.createStatement(subj, OWL.SAMEAS, obj));
				}
			}
			return new CloseableIteratorIteration<>(l.iterator());
		}
	}

	private Optional<Boolean> getActiveFlag(Resource... contexts) {
		var active = vf.createIRI("id:active");
		var obsolete = vf.createIRI("id:obsolete");
		for (Resource context : contexts) {
			if (active.equals(context)) {
				return Optional.of(Boolean.TRUE);
			}
			if (obsolete.equals(context)) {
				return Optional.of(Boolean.FALSE);
			}
		}
		return Optional.empty();
	}

	@Override
	public ValueFactory getValueFactory() {
		return vf;
	}

}
