package uk.co.brunella.qof;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdapterRegistrationTest {

    @Test
    public void testRegistration() {
        assertFalse(QueryObjectFactory.isMapperRegistered("name"));
        QueryObjectFactory.registerMapper("name", new DynamicNameAdapter());
        assertTrue(QueryObjectFactory.isMapperRegistered("name"));
        QueryObjectFactory.unregisterMapper("name");
        assertFalse(QueryObjectFactory.isMapperRegistered("name"));
    }
}
