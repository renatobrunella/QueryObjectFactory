package sf.qof;

import junit.framework.TestCase;

public class ParameterReplacerTest extends TestCase {

  public void testReplace() {
    String sql = "select * from test where a = ? and b in (?)";
    assertEquals("select * from test where a = ? and b in (?,?,?)", ParameterReplacer.replace(sql, 2, 3, ","));
  }
  
  public void testReplaceWithSeparator() {
    String sql = "select * from test where a = ? and (b like ?)";
    assertEquals("select * from test where a = ? and (b like ? or b like ? or b like ?)", ParameterReplacer.replace(sql, 2, 3, " or b like "));
  }

  public void testReplace2() {
    String sql = "select * from test where a = ? and b in (?) and c = ?";
    assertEquals("select * from test where a = ? and b in (?,?,?) and c = ?", ParameterReplacer.replace(sql, 2, 3, ","));
  }

  public void testReplaceWithComment() {
    String sql = "select /* Comment */ * from test where a = ? and b in (?)";
    assertEquals("select /* Comment */ * from test where a = ? and b in (?,?,?)", ParameterReplacer.replace(sql, 2, 3, ","));
  }

  public void testReplaceWithCommentNotClosed() {
    String sql = "select /* Comment * / * from test where a = ? and b in (?)";
    assertEquals("select /* Comment * / * from test where a = ? and b in (?)", ParameterReplacer.replace(sql, 2, 3, ","));
  }

  public void testReplaceWithCommentAtEnd() {
    String sql = "select * from test where a = ? and b in (?) /* Comment * /";
    assertEquals("select * from test where a = ? and b in (?,?,?) /* Comment * /", ParameterReplacer.replace(sql, 2, 3, ","));
  }
  
  public void testConstructor() {
    assertNotNull(new ParameterReplacer());
  }
}
