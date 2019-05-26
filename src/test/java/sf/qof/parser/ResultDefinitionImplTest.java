package sf.qof.parser;

import junit.framework.TestCase;

public class ResultDefinitionImplTest extends TestCase {

    public void testToString() {
        ResultDefinitionImpl impl = new ResultDefinitionImpl();
        impl.setColumns(new String[]{"column1", "column2"});
        impl.setConstructorParameter(1);
        impl.setField("field");
        impl.setIndexes(new int[]{2, 3});
        impl.setIsMapKey(true);
        impl.setType("type");
        assertEquals("Result: type (\"column1\",\"column2\" 2,3) field", impl.toString());
    }

    public void testToString2() {
        ResultDefinitionImpl impl = new ResultDefinitionImpl();
        impl.setConstructorParameter(1);
        impl.setField("field");
        impl.setIsMapKey(true);
        impl.setType("type");
        assertEquals("Result: type ( ) field", impl.toString());
    }
}
