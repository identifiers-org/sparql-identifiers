package org.identifiers.cloud.ws.sparql;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ApplicationTest {
    @Autowired
    SparqlApplication app;

    @Test
    void contextLoads() {
        assertNotNull(app);
    }
}
