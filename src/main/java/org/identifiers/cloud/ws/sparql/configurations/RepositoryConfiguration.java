package org.identifiers.cloud.ws.sparql.configurations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.identifiers.cloud.ws.sparql.services.SameAsResolver;
import org.identifiers.cloud.ws.sparql.data.sail.IdorgStore;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RepositoryConfiguration {
    @Bean
    public Repository getRepository(SameAsResolver sameAsResolver,
                                    @Value("${org.identifiers.cloud.ws.sparql.imports}")
                                    URL[] imports) throws IOException {
        var idotStore = new MemoryStore();
        SailRepository updatableRepository = new SailRepository(idotStore);
        updatableRepository.init();
        try (var conn = updatableRepository.getConnection()) {
            for (URL importUrl : imports) {
                var format = Rio.getParserFormatForFileName(importUrl.toString())
                        .orElseGet(() -> {
                            log.error("Failed to guess rdf format by filename for '{}', trying RDFXML...", importUrl);
                            return RDFFormat.RDFXML;
                        });
                conn.add(importUrl, null, format);
            }
        }

        IdorgStore idorgStore = new IdorgStore(sameAsResolver);
        idorgStore.setBaseSail(idotStore);
        SailRepository sailRepository = new SailRepository(idorgStore);
        sailRepository.init();

        return sailRepository;
    }
}
