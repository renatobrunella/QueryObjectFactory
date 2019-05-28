package uk.co.brunella.qof.session;

import org.junit.Test;

import static org.junit.Assert.*;

public class SystemExceptionTest {

    @Test
    public void testCreation1() {
        try {
            throw new SystemException();
        } catch (SystemException e) {
            assertNull(e.getMessage());
        }
    }

    @Test
    public void testCreation2() {
        try {
            throw new SystemException("message");
        } catch (SystemException e) {
            assertEquals("message", e.getMessage());
        }
    }

    @Test
    public void testCreation3() {
        try {
            throw new SystemException(new RuntimeException());
        } catch (SystemException e) {
            assertEquals("java.lang.RuntimeException", e.getMessage());
            assertTrue(e.getCause() instanceof RuntimeException);
        }
    }

    @Test
    public void testCreation4() {
        try {
            throw new SystemException("message", new RuntimeException());
        } catch (SystemException e) {
            assertEquals("message", e.getMessage());
            assertTrue(e.getCause() instanceof RuntimeException);
        }
    }
}
