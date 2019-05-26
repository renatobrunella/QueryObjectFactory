package uk.co.brunella.qof;

import junit.framework.TestCase;

import java.sql.SQLException;


public class CreationFromSuperClassTest extends TestCase {

    public void testCreation() {
        TestInterface test = QueryObjectFactory.createQueryObjectFromSuperClass(TestInterface.class, TestSuperClass.class);
        assertNotNull(test);
        // cast and call
        ((TestSuperClass) test).someMethod();
    }

    public void testCreationFailed() {
        try {
            QueryObjectFactory.createQueryObjectFromSuperClass(TestSuperClass.class, String.class);
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Invalid class hierarchie", e.getMessage());
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
