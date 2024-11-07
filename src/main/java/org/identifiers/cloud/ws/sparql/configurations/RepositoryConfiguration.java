package org.identifiers.cloud.ws.sparql.configurations;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.identifiers.cloud.ws.sparql.services.SameAsResolver;
import org.identifiers.cloud.ws.sparql.data.sail.IdorgStore;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class RepositoryConfiguration {
	@Bean
	public Repository getRepository(SameAsResolver sameAsResolver,
                                    @Value("${org.identifiers.cloud.ws.sparql.idotdatafolder}")
                                    File dataDir) throws IOException{
        var idotStore = new MemoryStore();
		SailRepository updatableRepository = new SailRepository(idotStore);
		updatableRepository.init();
		try (var conn = updatableRepository.getConnection()) {
			var idotFile = new File(dataDir, "idorg-ontology.ttl");
			conn.add(idotFile, null, RDFFormat.TURTLE);

			var triplesFile = new File(dataDir, "idorg-ontology-materialized.ttl");
			conn.add(triplesFile, null, RDFFormat.TURTLE);
		}

		IdorgStore idorgStore = new IdorgStore(sameAsResolver);
		idorgStore.setBaseSail(idotStore);
		SailRepository sailRepository = new SailRepository(idorgStore);
		sailRepository.init();

		return sailRepository;
	}
}
