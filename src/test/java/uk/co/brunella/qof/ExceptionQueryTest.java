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

public class ExceptionQueryTest extends TestCase {

    Connection connection;
    SelectQueries selectQueries;
    List<String> log;

    public void setUp() {
        selectQueries = QueryObjectFactory.createQueryObject(SelectQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        selectQueries.setConnection(connection);
        selectQueries.setFetchSize(99);
    }

    public void testSelectNoResultThrowException() {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        ((MockConnectionData) connection).setResultSetData(results);
        try {
            selectQueries.selectInt(0);
            fail("Expected SQLException");
        } catch (SQLException e) {
            assertEquals("Empty result set returned", e.getMessage());
        }
    }

    public void testSelectNoResultNull() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        ((MockConnectionData) connection).setResultSetData(results);
        assertNull(selectQueries.selectInteger(0));
    }

    public void testSelectTooManyResults() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new Integer(11));
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new Integer(22));
        ((MockConnectionData) connection).setResultSetData(results);
        try {
            selectQueries.selectInt(0);
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("More than one result in result set", e.getMessage());
        }
        ((MockConnectionData) connection).setResultSetData(results);
        try {
            selectQueries.selectInteger(0);
            fail("Should throw exception");
        } catch (SQLException e) {
            assertEquals("More than one result in result set", e.getMessage());
        }
    }

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select value {%%} from test where id1 = {%1})")
        int selectInt(int a) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1})")
        Integer selectInteger(int a) throws SQLException;
    }
}
