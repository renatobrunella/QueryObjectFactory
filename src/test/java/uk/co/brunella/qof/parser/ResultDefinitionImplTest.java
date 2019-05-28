package uk.co.brunella.qof.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResultDefinitionImplTest {

    @Test
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

    @Test
    public void testToString2() {
        ResultDefinitionImpl impl = new ResultDefinitionImpl();
        impl.setConstructorParameter(1);
        impl.setField("field");
        impl.setIsMapKey(true);
        impl.setType("type");
        assertEquals("Result: type ( ) field", impl.toString());
    }
}
