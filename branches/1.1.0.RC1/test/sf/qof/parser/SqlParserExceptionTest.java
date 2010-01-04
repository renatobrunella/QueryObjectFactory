package sf.qof.parser;

import sf.qof.exception.SqlParserException;
import sf.qof.parser.SqlParser;

import junit.framework.TestCase;

public class SqlParserExceptionTest extends TestCase {

  public void testParsingOneParameter1() {
    String sql = "select * from test where name = {%1";
    try {
      new SqlParser(sql, false);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Cannot parse definition: {%1", e.getMessage());
    }
  }

  public void testParsingOneResult1() {
    String sql = "select * {%% from test";
    try {
      new SqlParser(sql, false);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Cannot parse definition: {%%fromtest", e.getMessage());
    }
  }

  public void testParsingOneResult2() {
    String sql = "select * {%%.} from test";
    try {
      new SqlParser(sql, false);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Cannot parse definition: {%%.}", e.getMessage());
    }
  }

  public void testParsingOneParameter2() {
    String sql = "select * from test where name = {%1field}";
    try {
      new SqlParser(sql, false);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Cannot parse definition: {%1field}", e.getMessage());
    }
  }

  public void testParsingOneParameter3() {
    String sql = "select * from test where name = {%-1}";
    try {
      new SqlParser(sql, false);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Cannot parse definition: {%-1}", e.getMessage());
    }
  }
  
  public void testParsingOneParameter4() {
    String sql = "select * from test where name = {abcd}";
    try {
      new SqlParser(sql, false);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Cannot parse definition: {abcd}", e.getMessage());
    }
  }
  
  public void testParsingOneParameter5() {
    String sql = "select * from test where name = {@@%1}";
    try {
      new SqlParser(sql, false);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Cannot parse definition: {@@%1}", e.getMessage());
    }
  }
  
  public void testParsingOneParameter6() {
    String sql = "select * from test where name = {%1%2}";
    try {
      new SqlParser(sql, false);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Cannot parse definition: {%1%2}", e.getMessage());
    }
  }
  
  public void testParsingOneParameter7() {
    String sql = "select * from test where name = {%1,%2}";
    try {
      new SqlParser(sql, false);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("One of the definitions must be a map key definition: {%1,%2}", e.getMessage());
    }
  }
  
  public void testParsingOneParameter8() {
    String sql = "select * from test where name = {%1,%2}";
    try {
      new SqlParser(sql, true);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Only one parameter and result definition are allowed: {%1,%2}", e.getMessage());
    }
  }
  
  public void testParsingOneParameter9() {
    String sql = "select * from test where name = {%%,%%}";
    try {
      new SqlParser(sql, true);
      fail("Expected RuntimeException");
    } catch (SqlParserException e) {
      assertEquals("Only one parameter and result definition are allowed: {%%,%%}", e.getMessage());
    }
  }
  
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
