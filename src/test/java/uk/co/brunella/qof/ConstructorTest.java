package uk.co.brunella.qof;

import org.junit.Test;

import java.sql.Connection;

import static org.junit.Assert.*;

public class ConstructorTest {

    @Test
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

    public static abstract class TestConstructorClass implements BaseQuery {

        protected Connection connection;
        int num;

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
}
