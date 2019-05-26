package uk.co.brunella.qof.adapter;

import junit.framework.TestCase;
import uk.co.brunella.qof.BaseQuery;
import uk.co.brunella.qof.Call;
import uk.co.brunella.qof.Query;
import uk.co.brunella.qof.QueryObjectFactory;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooleanAdapterTest extends TestCase {

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select yesno {yesno%%} from test where xyz = {yesno%1}")
        Boolean selectBoolean(Boolean xyz) throws SQLException;

        @Query(sql = "select yesno {yesno%%} from test where xyz = {yesno%1}")
        boolean selectBool(boolean xyz) throws SQLException;

        @Query(sql = "select yesno {tf%%} from test where xyz = {tf%1}")
        Boolean selectBoolean2(Boolean xyz) throws SQLException;

        @Query(sql = "select yesno {tf%%} from test where xyz = {tf%1}")
        boolean selectBool2(boolean xyz) throws SQLException;

        @Query(sql = "select yesno {t-null%%} from test where xyz = {t-null%1}")
        Boolean selectBoolean3(Boolean xyz) throws SQLException;

        @Query(sql = "select yesno {t-null%%} from test where xyz = {t-null%1}")
        boolean selectBool3(boolean xyz) throws SQLException;

        @Query(sql = "select yesno {t2-null%%} from test where xyz = {t2-null%1}")
        Boolean selectBoolean4(Boolean xyz) throws SQLException;

        @Query(sql = "select yesno {t2-null%%} from test where xyz = {t2-null%1}")
        boolean selectBool4(boolean xyz) throws SQLException;

        @Call(sql = "{ {yesno%%} = call({yesno%1}) }")
        boolean call(boolean xyz) throws SQLException;
    }

    private Connection connection;
    private SelectQueries selectQueries;
    private List<String> log;

    public void setUp() {
        //BooleanAdapter.register("yesno", "Y", "F", false, true);
        BooleanAdapter.register("tf", "true", "false", true, false);
        BooleanAdapter.register("t-null", "true", null, true, true);
        BooleanAdapter.register("t2-null", "true", null, false, false);
        selectQueries = QueryObjectFactory.createQueryObject(SelectQueries.class);
        QueryObjectFactory.unregisterMapper("tf");
        QueryObjectFactory.unregisterMapper("t-null");
        QueryObjectFactory.unregisterMapper("t2-null");
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        selectQueries.setConnection(connection);
        selectQueries.setFetchSize(99);
    }

    public void testSelectBooleanTrue() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "y");
        ((MockConnectionData) connection).setResultSetData(results);
        assertTrue(selectQueries.selectBoolean(true));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,Y)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBooleanFalse() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "n");
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBoolean(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,N)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBooleanNull1() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", null);
        ((MockConnectionData) connection).setResultSetData(results);
        assertNull(selectQueries.selectBoolean(null));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1,12)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBooleanNull2() throws SQLException {
        assertNull(selectQueries.selectBoolean(null));
        assertEquals(7, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1,12)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolTrue() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "y");
        ((MockConnectionData) connection).setResultSetData(results);
        assertTrue(selectQueries.selectBool(true));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,Y)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolFalse() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "n");
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBool(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,N)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolNull1() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", null);
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBool(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,N)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolNull2() throws SQLException {
        try {
            assertFalse(selectQueries.selectBool(false));
            fail("Exception expected");
        } catch (SQLException e) {
            assertEquals("Empty result set returned", e.getMessage());
        }
    }


    public void testSelectBooleanTrue2() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "true");
        ((MockConnectionData) connection).setResultSetData(results);
        assertTrue(selectQueries.selectBoolean2(true));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,true)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBooleanFalse2() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "false");
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBoolean2(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,false)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBooleanNull1_2() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", null);
        ((MockConnectionData) connection).setResultSetData(results);
        try {
            selectQueries.selectBoolean2(true);
            fail("Exception expected");
        } catch (SQLException e) {
            assertEquals("null value not allowed for mapper \"tf\"", e.getMessage());
        }
    }

    public void testSelectBooleanNull2_2() throws SQLException {
        assertNull(selectQueries.selectBoolean2(true));
    }

    public void testSelectBooleanNull3_2() throws SQLException {
        try {
            selectQueries.selectBoolean2(null);
            fail("Exception expected");
        } catch (SQLException e) {
            assertEquals("null value not allowed for mapper \"tf\"", e.getMessage());
        }
    }

    public void testSelectBoolTrue3() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "true");
        ((MockConnectionData) connection).setResultSetData(results);
        assertTrue(selectQueries.selectBool3(true));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,true)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolFalse_3() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "false");
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBool3(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1,12)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolNull1_3() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", null);
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBool3(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1,12)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolNull2_3() throws SQLException {
        try {
            assertFalse(selectQueries.selectBool3(false));
            fail("Exception expected");
        } catch (SQLException e) {
            assertEquals("Empty result set returned", e.getMessage());
        }
    }

    public void testSelectBooleanTrue3() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "true");
        ((MockConnectionData) connection).setResultSetData(results);
        assertTrue(selectQueries.selectBoolean3(true));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,true)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBooleanFalse3() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "false");
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBoolean3(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1,12)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBooleanNull1_3() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", null);
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBoolean3(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1,12)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBooleanNull2_3() throws SQLException {
        assertNull(selectQueries.selectBoolean3(true));
    }

    public void testSelectBooleanNull3_3() throws SQLException {
        assertNull(selectQueries.selectBoolean3(false));
        assertEquals(7, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1,12)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolTrue4() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "true");
        ((MockConnectionData) connection).setResultSetData(results);
        assertTrue(selectQueries.selectBool3(true));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,true)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolFalse_4() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", "false");
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBool3(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1,12)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolNull1_4() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("yesno", null);
        ((MockConnectionData) connection).setResultSetData(results);
        assertFalse(selectQueries.selectBool3(false));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select yesno from test where xyz = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1,12)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(yesno)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolNull2_4() throws SQLException {
        try {
            assertFalse(selectQueries.selectBool3(false));
            fail("Exception expected");
        } catch (SQLException e) {
            assertEquals("Empty result set returned", e.getMessage());
        }
    }


    public void testCall() throws SQLException {
        List<Object> results = new ArrayList<Object>();
        results.add("y");
        ((MockConnectionData) connection).setResultData(results);
        assertTrue(selectQueries.call(false));
        assertEquals(6, log.size());
        int i = 0;
        assertEquals("prepareCall({  ? = call( ? )  })", log.get(i++));
        assertEquals("setString(2,N)", log.get(i++));
        assertEquals("registerOutParameter(1,12)", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getString(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

}
