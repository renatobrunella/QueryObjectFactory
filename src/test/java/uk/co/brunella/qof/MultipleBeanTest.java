package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MultipleBeanTest extends TestCase {

    public interface Queries extends BaseQuery {
        @Insert(sql = "insert into test values ({%1.parent.green})")
        void insert2(TestBean bean) throws SQLException;

        @Insert(sql = "insert into test values ({%1.parent.parent.name})")
        void insert3(TestBean bean) throws SQLException;
    }

    Connection connection;
    Queries queries;
    List<String> log;

    public void setUp() {
        queries = QueryObjectFactory.createQueryObject(Queries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        queries.setConnection(connection);
        queries.setFetchSize(99);
    }

    public void testInsert2() throws SQLException {
        TestBean bean = new TestBean();
        TestBean parent = new TestBean();
        bean.setParent(parent);
        parent.setGreen(true);
        queries.insert2(bean);
        int i = 0;
        assertEquals(4, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log.get(i++));
        assertEquals("setBoolean(1,true)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsert3() throws SQLException {
        TestBean bean = new TestBean();
        TestBean parent = new TestBean();
        TestBean parent2 = new TestBean();
        bean.setParent(parent);
        parent.setParent(parent2);
        parent2.setName("bean");
        queries.insert3(bean);
        int i = 0;
        assertEquals(4, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log.get(i++));
        assertEquals("setString(1,bean)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsert2WithNullValue() throws SQLException {
        TestBean bean = new TestBean();
        bean.setParent(null);
        // this will throw an exception because parent.green is a primitive!
        try {
            queries.insert2(bean);
            fail("should throw an exception");
        } catch (NullPointerException e) {
            assertNull(e.getMessage());
        }
    }

    public void testInsert3WithNullValue() throws SQLException {
        TestBean bean = new TestBean();
        TestBean parent = new TestBean();
        bean.setParent(parent);
        parent.setParent(null);
        queries.insert3(bean);
        int i = 0;
        assertEquals(4, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log.get(i++));
        assertEquals("setString(1,null)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

}
