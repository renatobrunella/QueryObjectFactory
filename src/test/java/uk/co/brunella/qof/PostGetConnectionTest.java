package uk.co.brunella.qof;


import org.junit.Before;
import org.junit.Test;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PostGetConnectionTest {

    private Connection connection;
    private List<String> log;

    @Before
    public void setUp() {
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
    }

    @Test
    public void testSelectString() throws SQLException {
        SelectQueries selectQueries = QueryObjectFactory.createQueryObject(SelectQueries.class);
        selectQueries.setConnection(connection);
        selectQueries.setFetchSize(99);

        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        results.add(data);
        data.put("value", "abc");
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals("abc", selectQueries.selectString("xyz"));
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,xyz)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals("abc", selectQueries.selectString(null));
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,null)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));

        assertEquals(connection, selectQueries.postGetConnectionCalled);
    }

    @Test
    public void testSelectString2() throws SQLException {
        SelectQueries2 selectQueries = QueryObjectFactory.createQueryObject(SelectQueries2.class);
        selectQueries.setConnection(connection);
        selectQueries.setFetchSize(99);

        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        results.add(data);
        data.put("value", "abc");
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals("abc", selectQueries.selectString2("xyz"));
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,xyz)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals("abc", selectQueries.selectString(null));
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,null)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));

        assertEquals(connection, selectQueries.postGetConnectionCalled);
    }

    public static abstract class SelectQueries implements BaseQuery {
        public Connection postGetConnectionCalled;

        @Query(sql = "select value {%%} from test where id1 = {%1})")
        public abstract String selectString(String a) throws SQLException;

        protected void postGetConnection(Connection connection) {
            postGetConnectionCalled = connection;
        }
    }

    public static abstract class SelectQueries2 extends SelectQueries {
        @Query(sql = "select value {%%} from test where id1 = {%1})")
        public abstract String selectString2(String a) throws SQLException;

    }
}
