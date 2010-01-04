package sf.qof.dialect;

import junit.framework.TestCase;

public class HSQLDbDialectTest extends TestCase {
  
  public void testGetLimitStringNoOffset() {
	assertEquals("select top ? a from b", 
		new HSQLDbDialect().getLimitString("select a from b", false));
  }

  public void testGetLimitStringWithOffset() {
	assertEquals("select limit ? ? a from b", 
		new HSQLDbDialect().getLimitString("select a from b", true));
  }

  public void testLimitParametersBeforeQueryParameters() {
	assertTrue(new HSQLDbDialect().limitParametersBeforeQueryParameters());
  }

  public void testLimitAddOffset() {
	assertFalse(new HSQLDbDialect().limitAddOffset());
  }

  public void testLimitOffsetFirst() {
	assertTrue(new HSQLDbDialect().limitOffsetFirst());
  }

}