package uk.co.brunella.qof.dialect;

import junit.framework.TestCase;

public class OracleDialectTest extends TestCase {

    public void testGetLimitStringNoOffset() {
        assertEquals("select * from ( select a from b ) where rownum <= ?",
                new OracleDialect().getLimitString("select a from b", false));
    }

    public void testGetLimitStringWithOffset() {
        assertEquals("select * from ( select qof_row_.*, rownum qof_rownum_ from ( select a from b ) qof_row_ where rownum <= ?) where qof_rownum_ > ?",
                new OracleDialect().getLimitString("select a from b", true));
    }

    public void testGetLimitStringNoOffsetSelectForUpdate() {
        assertEquals("select * from ( select a from b ) where rownum <= ? for update",
                new OracleDialect().getLimitString("select a from b for update ", false));
    }

    public void testLimitParametersBeforeQueryParameters() {
        assertFalse(new OracleDialect().limitParametersBeforeQueryParameters());
    }

    public void testLimitAddOffset() {
        assertTrue(new OracleDialect().limitAddOffset());
    }

    public void testLimitOffsetFirst() {
        assertFalse(new OracleDialect().limitOffsetFirst());
    }

}
