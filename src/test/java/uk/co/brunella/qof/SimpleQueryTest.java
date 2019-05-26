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

public class SimpleQueryTest extends TestCase {

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select value {%%} from test where id1 = {%1} and id2 = {%2})")
        byte selectByte1(byte a, Byte b) throws SQLException;

        @Query(sql = "select value {byte %%} from test where id1 = {%1} and id2 = {%2})")
        Byte selectByte2(byte a, Byte b) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1} and id2 = {%2})")
        boolean selectBoolean1(boolean a, Boolean b) throws SQLException;

        @Query(sql = "select value {boolean %%} from test where id1 = {%1} and id2 = {%2})")
        Boolean selectBoolean2(boolean a, Boolean b) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1} and id2 = {%2})")
        char selectChar1(char a, Character b) throws SQLException;

        @Query(sql = "select value {char %%} from test where id1 = {%1} and id2 = {%2})")
        Character selectChar2(char a, Character b) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1} and id2 = {%2})")
        short selectShort1(short a, Short b) throws SQLException;

        @Query(sql = "select value {short %%} from test where id1 = {%1} and id2 = {%2})")
        Short selectShort2(short a, Short b) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1} and id2 = {%2})")
        int selectInteger1(int a, Integer b) throws SQLException;

        @Query(sql = "select value {int %%} from test where id1 = {%1} and id2 = {%2})")
        Integer selectInteger2(int a, Integer b) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1} and id2 = {%2})")
        long selectLong1(long a, Long b) throws SQLException;

        @Query(sql = "select value {long %%} from test where id1 = {%1} and id2 = {%2})")
        Long selectLong2(long a, Long b) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1} and id2 = {%2})")
        float selectFloat1(float a, Float b) throws SQLException;

        @Query(sql = "select value {float %%} from test where id1 = {%1} and id2 = {%2})")
        Float selectFloat2(float a, Float b) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1} and id2 = {%2})")
        double selectDouble1(double a, Double b) throws SQLException;

        @Query(sql = "select value {double %%} from test where id1 = {%1} and id2 = {%2})")
        Double selectDouble2(double a, Double b) throws SQLException;

        @Query(sql = "select value {%%} from test where id1 = {%1})")
        String selectString(String a) throws SQLException;

        @Query(sql = "select value {date %%} from test where id1 = {date %1})")
        java.util.Date selectDate(java.util.Date a) throws SQLException;

        @Query(sql = "select value {time %%} from test where id1 = {time %1})")
        java.util.Date selectTime(java.util.Date a) throws SQLException;

        @Query(sql = "select value {timestamp %%} from test where id1 = {timestamp %1})")
        java.util.Date selectTimestamp(java.util.Date a) throws SQLException;
    }

    private Connection connection;
    private SelectQueries selectQueries;
    private List<String> log;

    public void setUp() {
        selectQueries = QueryObjectFactory.createQueryObject(SelectQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        selectQueries.setConnection(connection);
        selectQueries.setFetchSize(99);
    }

    public void testSelectByte() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new Byte((byte) 55));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55, selectQueries.selectByte1((byte) 11, new Byte((byte) 22)));
        assertEquals(10, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setByte(1,11)", log.get(i++));
        assertEquals("setByte(2,22)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getByte(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55, ((Byte) selectQueries.selectByte2((byte) 33, null)).byteValue());
        assertEquals(11, log.size());
        i = 0;
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setByte(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.TINYINT + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getByte(value)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectBoolean() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", Boolean.TRUE);
        ((MockConnectionData) connection).setResultSetData(results);
        assertTrue(selectQueries.selectBoolean1(true, Boolean.TRUE));
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setBoolean(1,true)", log.get(i++));
        assertEquals("setBoolean(2,true)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getBoolean(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertTrue(((Boolean) selectQueries.selectBoolean2(false, null)).booleanValue());
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setBoolean(1,false)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.BOOLEAN + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getBoolean(value)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectChar() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals('A', selectQueries.selectChar1('a', new Character('b')));
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("setString(2,b)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals('A', ((Character) selectQueries.selectChar2('c', null)).charValue());
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,c)", log.get(i++));
        assertEquals("setString(2,null)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectShort() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new Short((short) 55));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55, selectQueries.selectShort1((short) 11, new Short((short) 22)));
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setShort(1,11)", log.get(i++));
        assertEquals("setShort(2,22)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getShort(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55, ((Short) selectQueries.selectShort2((short) 33, null)).shortValue());
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setShort(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.SMALLINT + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getShort(value)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectInteger() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new Integer(55));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55, selectQueries.selectInteger1((int) 11, new Integer((int) 22)));
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setInt(2,22)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55, ((Integer) selectQueries.selectInteger2((int) 33, null)).intValue());
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(value)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectLong() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new Long(55));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55, selectQueries.selectLong1((long) 11, new Long((long) 22)));
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setLong(1,11)", log.get(i++));
        assertEquals("setLong(2,22)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getLong(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55, ((Long) selectQueries.selectLong2((long) 33, null)).longValue());
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setLong(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.BIGINT + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getLong(value)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectFloat() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new Float(55.5));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals((float) 55.5, selectQueries.selectFloat1((float) 11.1, new Float((float) 22.2)));
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setFloat(1,11.1)", log.get(i++));
        assertEquals("setFloat(2,22.2)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getFloat(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals((float) 55.5, ((Float) selectQueries.selectFloat2((float) 33.3, null)).floatValue());
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setFloat(1,33.3)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.REAL + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getFloat(value)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectDouble() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new Double(55.5));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55.5, selectQueries.selectDouble1((double) 11.1, new Double((double) 22.2)));
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setDouble(1,11.1)", log.get(i++));
        assertEquals("setDouble(2,22.2)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getDouble(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(55.5, ((Double) selectQueries.selectDouble2((double) 33.3, null)).doubleValue());
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? and id2 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setDouble(1,33.3)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.DOUBLE + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getDouble(value)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectString() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
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
    }

    public void testSelectDate() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new java.sql.Date(0));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(0, selectQueries.selectDate(new java.util.Date(0)).getTime());
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setDate(1,1970-01-01)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getDate(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(0, selectQueries.selectDate(null).getTime());
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getDate(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectTime() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new java.sql.Time(0));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(0, selectQueries.selectTime(new java.util.Date(0)).getTime());
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setTime(1,01:00:00)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getTime(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(0, selectQueries.selectTime(null).getTime());
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1," + java.sql.Types.TIME + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getTime(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectTimestamp() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", new java.sql.Timestamp(0));
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(0, selectQueries.selectTimestamp(new java.util.Date(0)).getTime());
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setTimestamp(1,1970-01-01 01:00:00.0)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getTimestamp(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        i = 0;
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(0, selectQueries.selectTimestamp(null).getTime());
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id1 = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setNull(1," + java.sql.Types.TIMESTAMP + ")", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getTimestamp(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }
}
