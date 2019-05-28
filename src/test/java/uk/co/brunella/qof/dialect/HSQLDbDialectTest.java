package uk.co.brunella.qof.dialect;

import org.junit.Test;

import static org.junit.Assert.*;

public class HSQLDbDialectTest {

    @Test
    public void testGetLimitStringNoOffset() {
        assertEquals("select top ? a from b",
                new HSQLDbDialect().getLimitString("select a from b", false));
    }

    @Test
    public void testGetLimitStringWithOffset() {
        assertEquals("select limit ? ? a from b",
                new HSQLDbDialect().getLimitString("select a from b", true));
    }

    @Test
    public void testLimitParametersBeforeQueryParameters() {
        assertTrue(new HSQLDbDialect().limitParametersBeforeQueryParameters());
    }

    @Test
    public void testLimitAddOffset() {
        assertFalse(new HSQLDbDialect().limitAddOffset());
    }

    @Test
    public void testLimitOffsetFirst() {
        assertTrue(new HSQLDbDialect().limitOffsetFirst());
    }

}