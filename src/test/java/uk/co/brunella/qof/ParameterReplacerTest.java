package uk.co.brunella.qof;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParameterReplacerTest {

    @Test
    public void testReplace() {
        String sql = "select * from test where a = ? and b in (?)";
        assertEquals("select * from test where a = ? and b in (?,?,?)", ParameterReplacer.replace(sql, 2, 3, ","));
    }

    @Test
    public void testReplaceWithSeparator() {
        String sql = "select * from test where a = ? and (b like ?)";
        assertEquals("select * from test where a = ? and (b like ? or b like ? or b like ?)", ParameterReplacer.replace(sql, 2, 3, " or b like "));
    }

    @Test
    public void testReplace2() {
        String sql = "select * from test where a = ? and b in (?) and c = ?";
        assertEquals("select * from test where a = ? and b in (?,?,?) and c = ?", ParameterReplacer.replace(sql, 2, 3, ","));
    }

    @Test
    public void testReplaceWithComment() {
        String sql = "select /* Comment */ * from test where a = ? and b in (?)";
        assertEquals("select /* Comment */ * from test where a = ? and b in (?,?,?)", ParameterReplacer.replace(sql, 2, 3, ","));
    }

    @Test
    public void testReplaceWithCommentNotClosed() {
        String sql = "select /* Comment * / * from test where a = ? and b in (?)";
        assertEquals("select /* Comment * / * from test where a = ? and b in (?)", ParameterReplacer.replace(sql, 2, 3, ","));
    }

    @Test
    public void testReplaceWithCommentAtEnd() {
        String sql = "select * from test where a = ? and b in (?) /* Comment * /";
        assertEquals("select * from test where a = ? and b in (?,?,?) /* Comment * /", ParameterReplacer.replace(sql, 2, 3, ","));
    }

    @Test
    public void testConstructor() {
        ParameterReplacer parameterReplacer = new ParameterReplacer();
        assertNotNull(parameterReplacer);
    }
}
