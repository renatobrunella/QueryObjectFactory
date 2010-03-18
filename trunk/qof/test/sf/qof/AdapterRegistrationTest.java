package sf.qof;

import junit.framework.TestCase;

public class AdapterRegistrationTest extends TestCase {

  public void testRegistration() {
    assertFalse(QueryObjectFactory.isMapperRegistered("name"));
    QueryObjectFactory.registerMapper("name", new DynamicNameAdapter());
    assertTrue(QueryObjectFactory.isMapperRegistered("name"));
    QueryObjectFactory.unregisterMapper("name");
    assertFalse(QueryObjectFactory.isMapperRegistered("name"));
  }
}
