package uk.co.brunella.qof.parser;

import junit.framework.TestCase;
import uk.co.brunella.qof.exception.SqlParserException;
import uk.co.brunella.qof.exception.ValidationException;

public class SqlParserTest extends TestCase {

    public void testNoParsing() {
        String sql = "select * from test where name = 'fred'";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals(sql, parser.getSql());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
    }

    public void testParsingNoChanges() {
        String sql = "select * from test where name = '{%%}'";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals(sql, parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
    }

    public void testParsingOneParameter() {
        String sql = "select * from test where name = {%1}";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select * from test where name = ?", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition def = parser.getParameterDefinitions()[0];
        assertEquals("auto", def.getType());
        assertEquals(1, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(1, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertNull(def.getFields());
    }

    public void testParsingOneParameterWithField() {
        String sql = "select * from test where name = {int%22.field}";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select * from test where name = ?", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition def = parser.getParameterDefinitions()[0];
        assertEquals("int", def.getType());
        assertEquals(22, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(1, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertEquals(1, def.getFields().length);
        assertEquals("field", def.getFields()[0]);
    }

    public void testParsingThreeParameters() {
        String sql = "select * from test where a = {%1} and b={int%2.field} and c={double %3 . field2}";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select * from test where a = ? and b= ? and c= ?", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(3, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition def = parser.getParameterDefinitions()[0];
        assertEquals("auto", def.getType());
        assertEquals(1, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(1, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertNull(def.getFields());
        def = parser.getParameterDefinitions()[1];
        assertEquals("int", def.getType());
        assertEquals(2, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(2, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertEquals(1, def.getFields().length);
        assertEquals("field", def.getFields()[0]);
        def = parser.getParameterDefinitions()[2];
        assertEquals("double", def.getType());
        assertEquals(3, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(3, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertEquals(1, def.getFields().length);
        assertEquals("field2", def.getFields()[0]);
    }

    public void testParsingOneResult() {
        String sql = "select abc {%%} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select abc from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(1, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("auto", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("abc", def.getColumns()[0]);
        assertNull(def.getField());
    }

    public void testParsingOneResult1() {
        String sql = "select count(*) as abc {%%} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select count(*) as abc from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(1, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("auto", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("abc", def.getColumns()[0]);
        assertNull(def.getField());
    }

    public void testParsingOneResult1b() {
        String sql = "select xyz,abc{%%} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select xyz,abc from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(1, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("auto", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("abc", def.getColumns()[0]);
        assertNull(def.getField());
    }

    public void testParsingOneResult2() {
        String sql = "select abc_def {int%%.field} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select abc_def from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(1, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("int", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("abc_def", def.getColumns()[0]);
        assertEquals("field", def.getField());
    }

    public void testParsingSimpleCall() {
        String sql = "{ call xyz ({%1})}";
        SqlParser parser = new SqlParser(sql, true);
        assertEquals("{  call xyz ( ? )  }", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition def = parser.getParameterDefinitions()[0];
        assertEquals("auto", def.getType());
        assertEquals(1, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(1, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertNull(def.getFields());
    }

    public void testParsingCallWithReturn() {
        String sql = "{ {int%%.field} = call xyz ({%1})}";
        SqlParser parser = new SqlParser(sql, true);
        assertEquals("{  ? = call xyz ( ? )  }", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(1, parser.getResultDefinitions().length);
        ParameterDefinition paramDef = parser.getParameterDefinitions()[0];
        assertEquals("auto", paramDef.getType());
        assertEquals(1, paramDef.getParameter());
        assertEquals(1, paramDef.getIndexes().length);
        assertEquals(2, paramDef.getIndexes()[0]);
        assertNull(paramDef.getNames());
        assertNull(paramDef.getFields());
        ResultDefinition resultDef = parser.getResultDefinitions()[0];
        assertEquals("int", resultDef.getType());
        assertNotNull(resultDef.getIndexes());
        assertEquals(1, resultDef.getIndexes().length);
        assertEquals(1, resultDef.getIndexes()[0]);
        assertNull(resultDef.getColumns());
        assertEquals("field", resultDef.getField());
    }

    public void testParsingSimpleCallInOut() {
        String sql = "{ call xyz ({%1,%%})}";
        SqlParser parser = new SqlParser(sql, true);
        assertEquals("{  call xyz ( ? )  }", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(1, parser.getResultDefinitions().length);
        ParameterDefinition paramDef = parser.getParameterDefinitions()[0];
        assertEquals("auto", paramDef.getType());
        assertEquals(1, paramDef.getParameter());
        assertEquals(1, paramDef.getIndexes().length);
        assertEquals(1, paramDef.getIndexes()[0]);
        assertNull(paramDef.getNames());
        assertNull(paramDef.getFields());
        ResultDefinition resultDef = parser.getResultDefinitions()[0];
        assertEquals("auto", resultDef.getType());
        assertNotNull(resultDef.getIndexes());
        assertEquals(1, resultDef.getIndexes().length);
        assertEquals(1, resultDef.getIndexes()[0]);
        assertNull(resultDef.getColumns());
        assertNull(resultDef.getField());
    }

    public void testParsingSimpleCallInWithQuestionMark() {
        String sql = "{ ? = call xyz ({%1})}";
        SqlParser parser = new SqlParser(sql, true);
        assertEquals("{  ? = call xyz ( ? )  }", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition paramDef = parser.getParameterDefinitions()[0];
        assertEquals("auto", paramDef.getType());
        assertEquals(1, paramDef.getParameter());
        assertEquals(1, paramDef.getIndexes().length);
        assertEquals(2, paramDef.getIndexes()[0]);
        assertNull(paramDef.getNames());
        assertNull(paramDef.getFields());
    }

    public void testParsingSimpleCallFailed() {
        String sql = "{ call xyz ({%%,%%})}";
        try {
            new SqlParser(sql, true);
            fail("exception expected");
        } catch (SqlParserException e) {
            assertEquals("Only one parameter and result definition are allowed: {%%,%%}", e.getMessage());
            assertEquals(12, e.getStart());
            assertEquals(7, e.getLength());
        }
    }

    public void testParsingWithMapKey() {
        String sql = "select id {%%,%%*} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select id from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(2, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("auto", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("id", def.getColumns()[0]);
        assertNull(def.getField());
        assertFalse(def.isMapKey());
        def = parser.getResultDefinitions()[1];
        assertEquals("auto", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("id", def.getColumns()[0]);
        assertNull(def.getField());
        assertTrue(def.isMapKey());
    }

    public void testParsingWithMapKey2() {
        String sql = "select id {%%*,%%} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select id from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(2, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("auto", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("id", def.getColumns()[0]);
        assertNull(def.getField());
        assertFalse(def.isMapKey());
        def = parser.getResultDefinitions()[1];
        assertEquals("auto", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("id", def.getColumns()[0]);
        assertNull(def.getField());
        assertTrue(def.isMapKey());
    }

    public void testParsingWithParameterSeparator() {
        String sql = "select id {%%} from test where (x like {%1 #or x like#})";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select id from test where (x like ? )", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        ParameterDefinition paramDef = parser.getParameterDefinitions()[0];
        assertEquals("auto", paramDef.getType());
        assertNull(paramDef.getPartialDefinitionGroup());
        assertNotNull(paramDef.getParameterSeparator());
        assertEquals(" or x like ", paramDef.getParameterSeparator());

        assertNotNull(parser.getResultDefinitions());
        assertEquals(1, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("auto", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("id", def.getColumns()[0]);
        assertNull(def.getField());
        assertFalse(def.isMapKey());
    }

    public void testParsingWithMapKey3() {
        String sql = "select id {%%,%%} from test";
        try {
            new SqlParser(sql, false);
            fail("exception expected");
        } catch (SqlParserException e) {
            assertEquals("One of the definitions must be a map key definition: {%%,%%}", e.getMessage());
            assertEquals(10, e.getStart());
            assertEquals(7, e.getLength());
        }
    }

    public void testParsingMissingBracket() {
        String sql = "select id {%% from test";
        try {
            new SqlParser(sql, false);
            fail("exception expected");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%%fromtest", e.getMessage());
            assertEquals(10, e.getStart());
            assertEquals(13, e.getLength());
        }
    }

    public void testParsingCallMissingBracket() {
        String sql = "{ call xyz ({%%) }";
        try {
            new SqlParser(sql, true);
            fail("exception expected");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%%)", e.getMessage());
            assertEquals(12, e.getStart());
            assertEquals(6, e.getLength());
        }
    }

    public void testParsingCallMissingBracket2() {
        String sql = "{ call xyz ({%%}) ";
        try {
            new SqlParser(sql, true);
            fail("exception expected");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {callxyz({%%}", e.getMessage());
            assertEquals(0, e.getStart());
            assertEquals(16, e.getLength());
        }
    }

    public void testParsingCallMissingBracket3() {
        String sql = " call xyz ({%%}) }";
        try {
            new SqlParser(sql, true);
            fail("exception expected");
        } catch (SqlParserException e) {
            assertEquals("Number of opening and closing curly brackets does not match", e.getMessage());
            assertEquals(0, e.getStart());
            assertEquals(18, e.getLength());
        }
    }

    public void testParsingFailed() {
        String sql = "select id {%%.} from test";
        try {
            new SqlParser(sql, false);
            fail("exception expected");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%%.}", e.getMessage());
            assertEquals(10, e.getStart());
            assertEquals(5, e.getLength());
        }
    }

    public void testParsingFailed2() {
        String sql = "select id {%1.} from test";
        try {
            new SqlParser(sql, false);
            fail("exception expected");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%1.}", e.getMessage());
            assertEquals(10, e.getStart());
            assertEquals(5, e.getLength());
        }
    }


    public void testParsingFailed3() {
        String sql = "select abc {%%} from x where s = {%1.}";
        try {
            new SqlParser(sql, false);
            fail("exception expected");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%1.}", e.getMessage());
            assertEquals(33, e.getStart());
            assertEquals(5, e.getLength());
        }
    }

    public void testParsingOneResultConstructor() {
        new PartialDefinitionCombiner();
        String sql = "select abc_def {int%%1} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select abc_def from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(1, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("int", def.getType());
        assertNull(def.getIndexes());
        assertEquals(1, def.getColumns().length);
        assertEquals("abc_def", def.getColumns()[0]);
        assertEquals(1, def.getConstructorParameter());
    }

    public void testParsingOnePartialResultForAdapter() {
        String sql = "select a {xyz%%@1}, b {xyz%%@2} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select a , b from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(1, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("xyz", def.getType());
        assertNull(def.getIndexes());
        assertEquals(2, def.getColumns().length);
        assertEquals("a", def.getColumns()[0]);
        assertEquals("b", def.getColumns()[1]);
    }

    public void testParsingOnePartialResult2ForAdapter() {
        String sql = "select a {xyz%%@1[1]}, b {xyz%%@2[1]}, c {xyz%%@2[2]}, d {xyz%%@1[2]} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select a , b , c , d from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(2, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("xyz", def.getType());
        assertNull(def.getIndexes());
        assertEquals(2, def.getColumns().length);
        assertEquals("a", def.getColumns()[0]);
        assertEquals("b", def.getColumns()[1]);
        def = parser.getResultDefinitions()[1];
        assertEquals("xyz", def.getType());
        assertNull(def.getIndexes());
        assertEquals(2, def.getColumns().length);
        assertEquals("d", def.getColumns()[0]);
        assertEquals("c", def.getColumns()[1]);
    }

    public void testParsingOnePartialResultFails() {
        try {
            String sql = "select a {xyz%%@1}, b {xyz%%@2}, c {xyz%%@2}, d {xyz%%@1} from test";
            new SqlParser(sql, false);
            fail("Exception expected");
        } catch (ValidationException e) {
            assertEquals("Duplicate partial definition", e.getMessage());
        }
    }

    public void testParsingOnePartialResult4ForAdapter() {
        String sql = "select a {xyz%%@1}, b {abc%%@2}, c {xyz%%@2}, d {abc%%@1}, e {abc%%@3} from test";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select a , b , c , d , e from test", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(0, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(2, parser.getResultDefinitions().length);
        ResultDefinition def = parser.getResultDefinitions()[0];
        assertEquals("abc", def.getType());
        assertNull(def.getIndexes());
        assertEquals(3, def.getColumns().length);
        assertEquals("d", def.getColumns()[0]);
        assertEquals("b", def.getColumns()[1]);
        assertEquals("e", def.getColumns()[2]);
        def = parser.getResultDefinitions()[1];
        assertEquals("xyz", def.getType());
        assertNull(def.getIndexes());
        assertEquals(2, def.getColumns().length);
        assertEquals("a", def.getColumns()[0]);
        assertEquals("c", def.getColumns()[1]);
    }

    public void testParsingOnePartialParameter1ForAdapter() {
        String sql = "insert into test values ({xyz%1@1}, {abc%2@1}, {xyz%1@2}, {name_gen%1.name@1})";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("insert into test values ( ? , ? , ? , ? )", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(3, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition def = parser.getParameterDefinitions()[0];
        assertEquals("abc", def.getType());
        assertNotNull(def.getIndexes());
        assertEquals(1, def.getIndexes().length);
        assertEquals(2, def.getIndexes()[0]);
        def = parser.getParameterDefinitions()[1];
        assertEquals("name_gen", def.getType());
        assertNotNull(def.getIndexes());
        assertEquals(1, def.getIndexes().length);
        assertEquals(4, def.getIndexes()[0]);
        def = parser.getParameterDefinitions()[2];
        assertEquals("xyz", def.getType());
        assertNotNull(def.getIndexes());
        assertEquals(2, def.getIndexes().length);
        assertEquals(1, def.getIndexes()[0]);
        assertEquals(3, def.getIndexes()[1]);
    }


    public void testParsingOneParameterWithTwoFields() {
        String sql = "select * from test where name = {int%5.field1.field2}";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select * from test where name = ?", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition def = parser.getParameterDefinitions()[0];
        assertEquals("int", def.getType());
        assertEquals(5, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(1, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertEquals(2, def.getFields().length);
        assertEquals("field1", def.getFields()[0]);
        assertEquals("field2", def.getFields()[1]);
    }

    public void testParsingOneParameterWithThreeFields() {
        String sql = "select * from test where name = {int%5.field1.field2.field3}";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select * from test where name = ?", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition def = parser.getParameterDefinitions()[0];
        assertEquals("int", def.getType());
        assertEquals(5, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(1, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertEquals(3, def.getFields().length);
        assertEquals("field1", def.getFields()[0]);
        assertEquals("field2", def.getFields()[1]);
        assertEquals("field3", def.getFields()[2]);
    }

    public void testParsingOneParameterWithFourFields() {
        String sql = "select * from test where name = {int%5.field1.field2.field3.field4}";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select * from test where name = ?", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition def = parser.getParameterDefinitions()[0];
        assertEquals("int", def.getType());
        assertEquals(5, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(1, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertEquals(4, def.getFields().length);
        assertEquals("field1", def.getFields()[0]);
        assertEquals("field2", def.getFields()[1]);
        assertEquals("field3", def.getFields()[2]);
        assertEquals("field4", def.getFields()[3]);
    }

    public void testParsingOneParameterWithFiveFields() {
        String sql = "select * from test where name = {int%5.field1.field2.field3.field4.field5}";
        SqlParser parser = new SqlParser(sql, false);
        assertEquals("select * from test where name = ?", parser.getSql().trim());
        assertNotNull(parser.getParameterDefinitions());
        assertEquals(1, parser.getParameterDefinitions().length);
        assertNotNull(parser.getResultDefinitions());
        assertEquals(0, parser.getResultDefinitions().length);
        ParameterDefinition def = parser.getParameterDefinitions()[0];
        assertEquals("int", def.getType());
        assertEquals(5, def.getParameter());
        assertEquals(1, def.getIndexes().length);
        assertEquals(1, def.getIndexes()[0]);
        assertNull(def.getNames());
        assertEquals(5, def.getFields().length);
        assertEquals("field1", def.getFields()[0]);
        assertEquals("field2", def.getFields()[1]);
        assertEquals("field3", def.getFields()[2]);
        assertEquals("field4", def.getFields()[3]);
        assertEquals("field5", def.getFields()[4]);
    }

    //TODO add test with multiple result mappings for one column
}
