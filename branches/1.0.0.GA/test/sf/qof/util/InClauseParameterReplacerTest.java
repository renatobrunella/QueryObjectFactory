package sf.qof.util;

import junit.framework.TestCase;

public class InClauseParameterReplacerTest extends TestCase {

  public void testReplace() {
    String sql = "select * from test where a = ? and b in (?)";
    assertEquals("select * from test where a = ? and b in (?,?,?)", InClauseParameterReplacer.replace(sql, 2, 3));
  }

  public void testReplace2() {
    String sql = "select * from test where a = ? and b in (?) and c = ?";
    assertEquals("select * from test where a = ? and b in (?,?,?) and c = ?", InClauseParameterReplacer.replace(sql, 2, 3));
  }

  public void testReplaceWithComment() {
    String sql = "select /* Comment */ * from test where a = ? and b in (?)";
    assertEquals("select /* Comment */ * from test where a = ? and b in (?,?,?)", InClauseParameterReplacer.replace(sql, 2, 3));
  }

  public void testReplaceWithCommentNotClosed() {
    String sql = "select /* Comment * / * from test where a = ? and b in (?)";
    assertEquals("select /* Comment * / * from test where a = ? and b in (?)", InClauseParameterReplacer.replace(sql, 2, 3));
  }

  public void testReplaceWithCommentAtEnd() {
    String sql = "select * from test where a = ? and b in (?) /* Comment * /";
    assertEquals("select * from test where a = ? and b in (?,?,?) /* Comment * /", InClauseParameterReplacer.replace(sql, 2, 3));
  }
  
}
