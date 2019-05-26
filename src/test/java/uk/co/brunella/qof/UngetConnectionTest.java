package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UngetConnectionTest extends TestCase {

    private Connection connection;
    private Queries queries;

    public void setUp() {
        queries = QueryObjectFactory.createQueryObject(Queries.class);
        connection = MockConnectionFactory.getConnection();
        queries.setConnection(connection);
        queries.setFetchSize(99);
    }

    public void testSelectNoCollection() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "abc");
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(queries.ungetConnectionCalled);
        assertEquals("abc", queries.selectString("xyz"));
        assertTrue(queries.ungetConnectionCalled);
    }

    public void testSelectCollection() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "abc");
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(queries.ungetConnectionCalled);
        assertEquals(1, queries.selectStrings("xyz").size());
        assertTrue(queries.ungetConnectionCalled);
    }

    public void testInsertNoCollection() throws SQLException {
        assertFalse(queries.ungetConnectionCalled);
        queries.insert(1);
        assertTrue(queries.ungetConnectionCalled);
    }

    public void testInsertCollection() throws SQLException {
        assertFalse(queries.ungetConnectionCalled);
        List<Integer> ints = new ArrayList<Integer>();
        ints.add(1);
        ints.add(2);
        queries.insert(ints);
        assertTrue(queries.ungetConnectionCalled);
    }

    public void testCallNoCollection() throws SQLException {
        assertFalse(queries.ungetConnectionCalled);
        queries.call(1);
        assertTrue(queries.ungetConnectionCalled);
    }

    public void testCallCollection() throws SQLException {
        assertFalse(queries.ungetConnectionCalled);
        List<Integer> ints = new ArrayList<Integer>();
        ints.add(1);
        ints.add(2);
        queries.call(ints);
        assertTrue(queries.ungetConnectionCalled);
    }

    public static abstract class Queries implements BaseQuery {

        public boolean ungetConnectionCalled = false;

        @Query(sql = "select value {%%} from test where id1 = {%1})")
        public abstract String selectString(String a) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1})")
        public abstract List<String> selectStrings(String a) throws SQLException;

        @Insert(sql = "insert into test values ({%1})")
        public abstract void insert(int i) throws SQLException;

        @Insert(sql = "insert into test values ({%1})")
        public abstract void insert(List<Integer> ints) throws SQLException;

        @Call(sql = "{ call func ({%1}) }")
        public abstract void call(int a) throws SQLException;

        @Call(sql = "{ call func ({%1}) }")
        public abstract void call(List<Integer> ints) throws SQLException;

        public void ungetConnection(Connection connection) {
            ungetConnectionCalled = true;
        }
    }
}
