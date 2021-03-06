package uk.co.brunella.qof;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;


public class CreationFromSuperClassTest {

    @Test
    public void testCreation() {
        TestInterface test = QueryObjectFactory.createQueryObjectFromSuperClass(TestInterface.class, TestSuperClass.class);
        assertNotNull(test);
        // cast and call
        ((TestSuperClass) test).someMethod();
    }

    @Test
    public void testCreationFailed() {
        try {
            QueryObjectFactory.createQueryObjectFromSuperClass(TestSuperClass.class, String.class);
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Invalid class hierarchy", e.getMessage());
        }
    }

    public interface TestInterface extends BaseQuery {
        @Query(sql = "select id {%%} from test")
        int select() throws SQLException;
    }

    public static abstract class TestSuperClass {
        public void someMethod() {
        }
    }
}
