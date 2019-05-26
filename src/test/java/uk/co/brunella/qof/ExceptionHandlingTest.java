package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.dialect.OracleDialect;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExceptionHandlingTest extends TestCase {

    public static abstract class Base implements BaseQuery {
        boolean ungetConnectionFails = false;
        boolean ungetConnectionCalled = false;

        public void ungetConnection(Connection connection) {
            ungetConnectionCalled = true;
            if (ungetConnectionFails) {
                throw new RuntimeException("Bang! ungetConnection failed");
            }
        }
    }

    public static abstract class Queries extends Base {
        @Query(sql = "select value {%%} from test where id = {%1}")
        public abstract String selectOne(String s) throws SQLException;

        @Query(sql = "select value {%%} from test where id = {%1}")
        public abstract List<String> selectMany(String s) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}) }")
        public abstract String callOne(String a) throws SQLException;

        @Call(sql = "{ call func ({%1}) }")
        public abstract void callMany(List<String> list) throws SQLException;

        @Insert(sql = "insert into test values ({%1})")
        public abstract void insertOne(String s1) throws SQLException;

        @Insert(sql = "insert into test values ({%1})")
        public abstract void insertMany(List<String> list) throws SQLException;
    }

    public static abstract class QueriesPaging extends Base implements Paging {
        @Query(sql = "select value {%%} from test where id = {%1}")
        public abstract List<String> select(String s) throws SQLException;
    }

    private Connection connection;
    private List<String> log;

    public void setUp() {
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
    }

    private void setValues(String... values) {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (String value : values) {
            Map<String, Object> data = new HashMap<String, Object>();
            results.add(data);
            data.put("value", value);
        }
        ((MockConnectionData) connection).setResultSetData(results);
    }

    public void testPrepareStatementFailsSelectOne() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        setValues("data");
        queries.setConnection(connection);
        ((MockConnectionData) connection).setPrepareFailes(true);
        try {
            queries.selectOne("criteria");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("prepareStatement failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(2, log.size());
        int i = 0;
        assertEquals("setPrepareFailes(true)", log.get(i++));
        assertEquals("prepareStatement(select value from test where id = ? )", log.get(i++));
    }

    public void testPrepareStatementFailsSelectMany() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        setValues("data1", "data2");
        queries.setConnection(connection);
        ((MockConnectionData) connection).setPrepareFailes(true);
        try {
            queries.selectMany("criteria");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("prepareStatement failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(2, log.size());
        int i = 0;
        assertEquals("setPrepareFailes(true)", log.get(i++));
        assertEquals("prepareStatement(select value from test where id = ? )", log.get(i++));
    }

    public void testExecuteFailsSelectOne() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        setValues("data");
        queries.setConnection(connection);
        ((MockConnectionData) connection).setExecuteFailes(true);
        try {
            queries.selectOne("criteria");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("executeQuery failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(6, log.size());
        int i = 0;
        assertEquals("setExecuteFailes(true)", log.get(i++));
        assertEquals("prepareStatement(select value from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,criteria)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testExecuteFailsSelectMany() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        setValues("data1", "data2");
        queries.setConnection(connection);
        ((MockConnectionData) connection).setExecuteFailes(true);
        try {
            queries.selectMany("criteria");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("executeQuery failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(6, log.size());
        int i = 0;
        assertEquals("setExecuteFailes(true)", log.get(i++));
        assertEquals("prepareStatement(select value from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(100)", log.get(i++));
        assertEquals("setString(1,criteria)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testUngetConnectionFailsSelectOne() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        setValues("data");
        queries.setConnection(connection);
        assertFalse(queries.ungetConnectionCalled);
        assertEquals("data", queries.selectOne("criteria"));
        assertTrue(queries.ungetConnectionCalled);
        queries = QueryObjectFactory.createQueryObject(Queries.class);
        setValues("data");
        queries.setConnection(connection);
        queries.ungetConnectionFails = true;
        assertFalse(queries.ungetConnectionCalled);
        try {
            queries.selectOne("criteria");
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Bang! ungetConnection failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,criteria)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testUngetConnectionFailsSelectMany() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        setValues("data1", "data2");
        queries.setConnection(connection);
        assertFalse(queries.ungetConnectionCalled);
        List<String> list = queries.selectMany("criteria");
        assertEquals(2, list.size());
        assertTrue(queries.ungetConnectionCalled);
        queries = QueryObjectFactory.createQueryObject(Queries.class);
        setValues("data1", "data2");
        queries.setConnection(connection);
        queries.ungetConnectionFails = true;
        assertFalse(queries.ungetConnectionCalled);
        try {
            queries.selectMany("criteria");
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Bang! ungetConnection failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(11, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(100)", log.get(i++));
        assertEquals("setString(1,criteria)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testPrepareStatementFailsSelectPaging() throws SQLException {
        QueryObjectFactory.setSQLDialect(new OracleDialect());
        QueriesPaging queries = QueryObjectFactory.createQueryObject(QueriesPaging.class);
        setValues("data1", "data2");
        queries.setConnection(connection);
        queries.setFirstResult(1);
        queries.setMaxResults(20);
        ((MockConnectionData) connection).setPrepareFailes(true);
        try {
            queries.select("criteria");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("prepareStatement failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(2, log.size());
        int i = 0;
        assertEquals("setPrepareFailes(true)", log.get(i++));
        assertEquals("prepareStatement(select * from ( select qof_row_.*, rownum qof_rownum_ from " +
                        "( select value from test where id = ? ) qof_row_ where rownum <= ?) where qof_rownum_ > ?)",
                log.get(i++));
    }

    public void testExecuteFailsSelectPaging() throws SQLException {
        QueryObjectFactory.setSQLDialect(new OracleDialect());
        QueriesPaging queries = QueryObjectFactory.createQueryObject(QueriesPaging.class);
        setValues("data1", "data2");
        queries.setConnection(connection);
        queries.setFirstResult(1);
        queries.setMaxResults(20);
        ((MockConnectionData) connection).setExecuteFailes(true);
        try {
            queries.select("criteria");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("executeQuery failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(8, log.size());
        int i = 0;
        assertEquals("setExecuteFailes(true)", log.get(i++));
        assertEquals("prepareStatement(select * from ( select qof_row_.*, rownum qof_rownum_ from " +
                        "( select value from test where id = ? ) qof_row_ where rownum <= ?) where qof_rownum_ > ?)",
                log.get(i++));
        assertEquals("setInt(2,21)", log.get(i++));
        assertEquals("setInt(3,1)", log.get(i++));
        assertEquals("setFetchSize(100)", log.get(i++));
        assertEquals("setString(1,criteria)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testUngetConnectionFailsSelectPaging() throws SQLException {
        QueryObjectFactory.setSQLDialect(new OracleDialect());
        QueriesPaging queries = QueryObjectFactory.createQueryObject(QueriesPaging.class);
        setValues("data1", "data2");
        queries.setConnection(connection);
        queries.setFirstResult(1);
        queries.setMaxResults(20);
        queries.ungetConnectionFails = true;
        assertFalse(queries.ungetConnectionCalled);
        try {
            queries.select("criteria");
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Bang! ungetConnection failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(13, log.size());
        int i = 0;
        assertEquals("prepareStatement(select * from ( select qof_row_.*, rownum qof_rownum_ from " +
                "( select value from test where id = ? ) qof_row_ where rownum <= ?) where qof_rownum_ > ?)", log.get(i++));
        assertEquals("setInt(2,21)", log.get(i++));
        assertEquals("setInt(3,1)", log.get(i++));
        assertEquals("setFetchSize(100)", log.get(i++));
        assertEquals("setString(1,criteria)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }


    public void testPrepareStatementFailsInsertOne() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        ((MockConnectionData) connection).setPrepareFailes(true);
        try {
            queries.insertOne("data");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("prepareStatement failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(2, log.size());
        int i = 0;
        assertEquals("setPrepareFailes(true)", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? ) )", log.get(i++));
    }

    public void testPrepareStatementFailsInsertMany() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        ((MockConnectionData) connection).setPrepareFailes(true);
        try {
            List<String> list = new ArrayList<String>();
            list.add("data1");
            list.add("data2");
            queries.insertMany(list);
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("prepareStatement failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(2, log.size());
        int i = 0;
        assertEquals("setPrepareFailes(true)", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? ) )", log.get(i++));
    }

    public void testExecuteFailsInsertOne() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        ((MockConnectionData) connection).setExecuteFailes(true);
        try {
            queries.insertOne("data");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("execute failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(5, log.size());
        int i = 0;
        assertEquals("setExecuteFailes(true)", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? ) )", log.get(i++));
        assertEquals("setString(1,data)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testExecuteFailsInsertMany() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        ((MockConnectionData) connection).setExecuteFailes(true);
        try {
            List<String> list = new ArrayList<String>();
            list.add("data1");
            list.add("data2");
            queries.insertMany(list);
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("execute failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(8, log.size());
        int i = 0;
        assertEquals("setExecuteFailes(true)", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? ) )", log.get(i++));
        assertEquals("setString(1,data1)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setString(1,data2)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testUngetConnectionFailsInsertOne() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        queries.ungetConnectionFails = true;
        assertFalse(queries.ungetConnectionCalled);
        try {
            queries.insertOne("data");
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Bang! ungetConnection failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(4, log.size());
        int i = 0;
        assertEquals("prepareStatement(insert into test values ( ? ) )", log.get(i++));
        assertEquals("setString(1,data)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testUngetConnectionFailsInsertMany() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        queries.ungetConnectionFails = true;
        assertFalse(queries.ungetConnectionCalled);
        try {
            List<String> list = new ArrayList<String>();
            list.add("data1");
            list.add("data2");
            queries.insertMany(list);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Bang! ungetConnection failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(7, log.size());
        int i = 0;
        assertEquals("prepareStatement(insert into test values ( ? ) )", log.get(i++));
        assertEquals("setString(1,data1)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setString(1,data2)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testPrepareStatementFailsCallOne() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        ((MockConnectionData) connection).setPrepareFailes(true);
        try {
            queries.callOne("data");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("prepareCall failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(2, log.size());
        int i = 0;
        assertEquals("setPrepareFailes(true)", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
    }

    public void testPrepareStatementFailsCallMany() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        ((MockConnectionData) connection).setPrepareFailes(true);
        try {
            List<String> list = new ArrayList<String>();
            list.add("data1");
            list.add("data2");
            queries.callMany(list);
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("prepareCall failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(2, log.size());
        int i = 0;
        assertEquals("setPrepareFailes(true)", log.get(i++));
        assertEquals("prepareCall({  call func ( ? )  })", log.get(i++));
    }

    public void testExecuteFailsCallOne() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        ((MockConnectionData) connection).setExecuteFailes(true);
        try {
            queries.callOne("data");
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("execute failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(6, log.size());
        int i = 0;
        assertEquals("setExecuteFailes(true)", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setString(2,data)", log.get(i++));
        assertEquals("registerOutParameter(1,12)", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testExecuteFailsCallMany() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        ((MockConnectionData) connection).setExecuteFailes(true);
        try {
            List<String> list = new ArrayList<String>();
            list.add("data1");
            list.add("data2");
            queries.callMany(list);
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("execute failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(8, log.size());
        int i = 0;
        assertEquals("setExecuteFailes(true)", log.get(i++));
        assertEquals("prepareCall({  call func ( ? )  })", log.get(i++));
        assertEquals("setString(1,data1)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setString(1,data2)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testUngetConnectionFailsCallOne() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        queries.ungetConnectionFails = true;
        assertFalse(queries.ungetConnectionCalled);
        try {
            queries.callOne("data");
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Bang! ungetConnection failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(6, log.size());
        int i = 0;
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setString(2,data)", log.get(i++));
        assertEquals("registerOutParameter(1,12)", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getString(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testUngetConnectionFailsCallMany() throws SQLException {
        Queries queries = QueryObjectFactory.createQueryObject(Queries.class);
        queries.setConnection(connection);
        queries.ungetConnectionFails = true;
        assertFalse(queries.ungetConnectionCalled);
        try {
            List<String> list = new ArrayList<String>();
            list.add("data1");
            list.add("data2");
            queries.callMany(list);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Bang! ungetConnection failed", e.getMessage());
        }
        assertTrue(queries.ungetConnectionCalled);
        assertEquals(7, log.size());
        int i = 0;
        assertEquals("prepareCall({  call func ( ? )  })", log.get(i++));
        assertEquals("setString(1,data1)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setString(1,data2)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }
}
