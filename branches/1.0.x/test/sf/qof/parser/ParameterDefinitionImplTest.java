package sf.qof.parser;

import junit.framework.TestCase;

public class ParameterDefinitionImplTest extends TestCase {

  public void testToString() {
	ParameterDefinitionImpl impl = new ParameterDefinitionImpl();
	impl.setField("field");
	impl.setIndexes(new int[]{1, 2});
	impl.setNames(new String[]{"name1", "name2"});
	impl.setParameter(-1);
	impl.setType("type");
	assertEquals("Parameter: type -1 (\"name1\",\"name2\" 1,2) field", impl.toString());
  }

  public void testToString2() {
	ParameterDefinitionImpl impl = new ParameterDefinitionImpl();
	impl.setField("field");
	impl.setParameter(-1);
	impl.setType("type");
	assertEquals("Parameter: type -1 ( ) field", impl.toString());
  }
}
