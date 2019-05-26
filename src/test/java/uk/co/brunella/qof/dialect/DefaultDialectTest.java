package uk.co.brunella.qof.dialect;

import junit.framework.TestCase;

public class DefaultDialectTest extends TestCase {

    public void testGetLimitString() {
        try {
            new DefaultDialect().getLimitString("", true);
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Not supported", e.getMessage());
        }
    }

    public void testLimitParametersBeforeQueryParameters() {
        try {
            new DefaultDialect().limitParametersBeforeQueryParameters();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Not supported", e.getMessage());
        }
    }

    public void testLimitAddOffset() {
        try {
            new DefaultDialect().limitAddOffset();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Not supported", e.getMessage());
        }
    }

    public void testLimitOffsetFirst() {
        try {
            new DefaultDialect().limitOffsetFirst();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Not supported", e.getMessage());
        }
    }

}
