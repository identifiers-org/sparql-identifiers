package org.identifiers.cloud.ws.sparql.configurations;

import org.identifiers.cloud.ws.sparql.services.SameAsResolver;
import org.identifiers.cloud.ws.sparql.data.sail.IdorgStore;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {
	@Bean
	public Repository getRepository(SameAsResolver sameAsResolver){
		IdorgStore store = new IdorgStore(sameAsResolver);
		//TODO inject the right store with the data from the identifiers.org ontology
		store.setBaseSail(new MemoryStore());
		SailRepository sailRepository = new SailRepository(store);
		sailRepository.init();
		return sailRepository;
	}
}
