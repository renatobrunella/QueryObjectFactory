package uk.co.brunella.qof.dialect;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DefaultDialectTest {

    @Test
    public void testGetLimitString() {
        try {
            new DefaultDialect().getLimitString("", true);
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Not supported", e.getMessage());
        }
    }

    @Test
    public void testLimitParametersBeforeQueryParameters() {
        try {
            new DefaultDialect().limitParametersBeforeQueryParameters();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Not supported", e.getMessage());
        }
    }

    @Test
    public void testLimitAddOffset() {
        try {
            new DefaultDialect().limitAddOffset();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Not supported", e.getMessage());
        }
    }

    @Test
    public void testLimitOffsetFirst() {
        try {
            new DefaultDialect().limitOffsetFirst();
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Not supported", e.getMessage());
        }
    }

}
