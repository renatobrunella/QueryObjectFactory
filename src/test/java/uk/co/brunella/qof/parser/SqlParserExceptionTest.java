package uk.co.brunella.qof.parser;

import org.junit.Test;
import uk.co.brunella.qof.exception.SqlParserException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SqlParserExceptionTest {

    @Test
    public void testParsingOneParameter1() {
        String sql = "select * from test where name = {%1";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%1", e.getMessage());
        }
    }

    @Test
    public void testParsingOneResult1() {
        String sql = "select * {%% from test";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%%fromtest", e.getMessage());
        }
    }

    @Test
    public void testParsingOneResult2() {
        String sql = "select * {%%.} from test";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%%.}", e.getMessage());
        }
    }

    @Test
    public void testParsingOneParameter2() {
        String sql = "select * from test where name = {%1field}";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%1field}", e.getMessage());
        }
    }

    @Test
    public void testParsingOneParameter3() {
        String sql = "select * from test where name = {%-1}";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%-1}", e.getMessage());
        }
    }

    @Test
    public void testParsingOneParameter4() {
        String sql = "select * from test where name = {abcd}";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {abcd}", e.getMessage());
        }
    }

    @Test
    public void testParsingOneParameter5() {
        String sql = "select * from test where name = {@@%1}";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {@@%1}", e.getMessage());
        }
    }

    @Test
    public void testParsingOneParameter6() {
        String sql = "select * from test where name = {%1%2}";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {%1%2}", e.getMessage());
        }
    }

    @Test
    public void testParsingOneParameter7() {
        String sql = "select * from test where name = {%1,%2}";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("One of the definitions must be a map key definition: {%1,%2}", e.getMessage());
        }
    }

    @Test
    public void testParsingOneParameter8() {
        String sql = "select * from test where name = {%1,%2}";
        try {
            new SqlParser(sql, true);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Only one parameter and result definition are allowed: {%1,%2}", e.getMessage());
        }
    }

    @Test
    public void testParsingOneParameter9() {
        String sql = "select * from test where name = {%%,%%}";
        try {
            new SqlParser(sql, true);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Only one parameter and result definition are allowed: {%%,%%}", e.getMessage());
        }
    }

    @Test
    public void testParsingCallAsQuery() {
        String sql = "{ call xyz (%1)}";
        try {
            new SqlParser(sql, false);
            fail("Expected RuntimeException");
        } catch (SqlParserException e) {
            assertEquals("Cannot parse definition: {callxyz(%1)}", e.getMessage());
        }
    }

}
