package uk.co.brunella.qof;

import junit.framework.TestCase;

import java.sql.Connection;

public class ConstructorTest extends TestCase {

    public static abstract class TestConstructorClass implements BaseQuery {

        protected Connection connection;
        protected int num;

        public TestConstructorClass(Connection connection) {
            this(connection, -1);
        }

        public TestConstructorClass(Connection connection, int num) {
            this.connection = connection;
            this.num = num;
        }

        @Query(sql = "select id {%%} from test")
        protected abstract int select();
    }

    public void testConstructor() {
        TestConstructorClass test = QueryObjectFactory.createQueryObject(TestConstructorClass.class, null, 99);
        assertNotNull(test);
        assertEquals(99, test.num);

        test = QueryObjectFactory.createQueryObject(TestConstructorClass.class, new Object[]{null});
        assertNotNull(test);
        assertEquals(-1, test.num);

        try {
            QueryObjectFactory.createQueryObject(TestConstructorClass.class);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }
}
