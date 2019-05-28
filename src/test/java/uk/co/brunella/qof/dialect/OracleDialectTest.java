package uk.co.brunella.qof.dialect;

import org.junit.Test;

import static org.junit.Assert.*;

public class OracleDialectTest {

    @Test
    public void testGetLimitStringNoOffset() {
        assertEquals("select * from ( select a from b ) where rownum <= ?",
                new OracleDialect().getLimitString("select a from b", false));
    }

    @Test
    public void testGetLimitStringWithOffset() {
        assertEquals("select * from ( select qof_row_.*, rownum qof_rownum_ from ( select a from b ) qof_row_ where rownum <= ?) where qof_rownum_ > ?",
                new OracleDialect().getLimitString("select a from b", true));
    }

    @Test
    public void testGetLimitStringNoOffsetSelectForUpdate() {
        assertEquals("select * from ( select a from b ) where rownum <= ? for update",
                new OracleDialect().getLimitString("select a from b for update ", false));
    }

    @Test
    public void testLimitParametersBeforeQueryParameters() {
        assertFalse(new OracleDialect().limitParametersBeforeQueryParameters());
    }

    @Test
    public void testLimitAddOffset() {
        assertTrue(new OracleDialect().limitAddOffset());
    }

    @Test
    public void testLimitOffsetFirst() {
        assertFalse(new OracleDialect().limitOffsetFirst());
    }

}
