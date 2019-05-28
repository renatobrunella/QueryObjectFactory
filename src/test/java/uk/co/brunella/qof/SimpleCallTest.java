package uk.co.brunella.qof;

import org.junit.Before;
import org.junit.Test;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimpleCallTest {

    private Connection connection;
    private CallQueries callQueries;
    List<String> log;

    @Before
    public void setUp() {
        callQueries = QueryObjectFactory.createQueryObject(CallQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        callQueries.setConnection(connection);
        callQueries.setFetchSize(99);
    }

    @Test
    public void testCallBoolean() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add(Boolean.TRUE);
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(true, callQueries.callBoolean1(true, Boolean.TRUE));
        assertEquals(Boolean.TRUE, callQueries.callBoolean2(false, null));
        int i = 0;
        assertEquals(15, log.size());
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setBoolean(2,true)", log.get(i++));
        assertEquals("setBoolean(3,true)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.BOOLEAN + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getBoolean(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setBoolean(2,false)", log.get(i++));
        assertEquals("setNull(3," + java.sql.Types.BOOLEAN + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.BOOLEAN + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getBoolean(1)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallByte() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add((byte) 55);
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(55, callQueries.callByte1((byte) 11, (byte) 22));
        assertEquals(55, callQueries.callByte2((byte) 33, null).byteValue());
        int i = 0;
        assertEquals(15, log.size());
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setByte(2,11)", log.get(i++));
        assertEquals("setByte(3,22)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.TINYINT + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getByte(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setByte(2,33)", log.get(i++));
        assertEquals("setNull(3," + java.sql.Types.TINYINT + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.TINYINT + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getByte(1)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallChar() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add("A");
        ((MockConnectionData) connection).setResultData(result);
        assertEquals('A', callQueries.callChar1('a', 'b'));
        assertEquals('A', callQueries.callChar2('c', null).charValue());
        int i = 0;
        assertEquals(14, log.size());
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setString(2,a)", log.get(i++));
        assertEquals("setString(3,b)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.VARCHAR + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getString(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setString(2,c)", log.get(i++));
        assertEquals("setString(3,null)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.VARCHAR + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getString(1)", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallShort() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add((short) 55);
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(55, callQueries.callShort1((short) 11, (short) 22));
        assertEquals(55, callQueries.callShort2((short) 33, null).shortValue());
        int i = 0;
        assertEquals(15, log.size());
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setShort(2,11)", log.get(i++));
        assertEquals("setShort(3,22)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.SMALLINT + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getShort(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setShort(2,33)", log.get(i++));
        assertEquals("setNull(3," + java.sql.Types.SMALLINT + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.SMALLINT + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getShort(1)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallInteger() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add((int) (short) 55);
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(55, callQueries.callInteger1((short) 11, (int) (short) 22));
        assertEquals(55, callQueries.callInteger2((short) 33, null).shortValue());
        int i = 0;
        assertEquals(15, log.size());
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setInt(2,11)", log.get(i++));
        assertEquals("setInt(3,22)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getInt(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setInt(2,33)", log.get(i++));
        assertEquals("setNull(3," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getInt(1)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallLong() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add((long) 55);
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(55, callQueries.callLong1((long) 11, (long) 22));
        assertEquals(55, callQueries.callLong2((long) 33, null).longValue());
        int i = 0;
        assertEquals(15, log.size());
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setLong(2,11)", log.get(i++));
        assertEquals("setLong(3,22)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.BIGINT + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getLong(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setLong(2,33)", log.get(i++));
        assertEquals("setNull(3," + java.sql.Types.BIGINT + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.BIGINT + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getLong(1)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallFloat() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add(55.5f);
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(55.5f, callQueries.callFloat1(11.1f, 22.2f), 0.00001);
        assertEquals(55.5f, callQueries.callFloat2(33.3f, null), 0.00001);
        int i = 0;
        assertEquals(15, log.size());
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setFloat(2,11.1)", log.get(i++));
        assertEquals("setFloat(3,22.2)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.REAL + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getFloat(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setFloat(2,33.3)", log.get(i++));
        assertEquals("setNull(3," + java.sql.Types.REAL + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.REAL + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getFloat(1)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallDouble() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add(55.5);
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(55.5, callQueries.callDouble1(11.1, 22.2), 0.00001);
        assertEquals(55.5, callQueries.callDouble2(33.3, null), 0.00001);
        int i = 0;
        assertEquals(15, log.size());
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setDouble(2,11.1)", log.get(i++));
        assertEquals("setDouble(3,22.2)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.DOUBLE + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getDouble(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? , ? )  })", log.get(i++));
        assertEquals("setDouble(2,33.3)", log.get(i++));
        assertEquals("setNull(3," + java.sql.Types.DOUBLE + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.DOUBLE + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getDouble(1)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallString() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add("abc");
        ((MockConnectionData) connection).setResultData(result);
        assertEquals("abc", callQueries.callString("xyz"));
        assertEquals("abc", callQueries.callString(null));
        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setString(2,xyz)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.VARCHAR + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getString(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setString(2,null)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.VARCHAR + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getString(1)", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallDate() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add(new java.sql.Date(0));
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(new java.util.Date(0).getTime(), callQueries.callDate(new java.util.Date(0)).getTime());
        assertEquals(new java.util.Date(0).getTime(), callQueries.callDate(null).getTime());
        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getDate(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getDate(1)", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallTime() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add(new java.sql.Time(0));
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(new java.util.Date(0).getTime(), callQueries.callTime(new java.util.Date(0)).getTime());
        assertEquals(new java.util.Date(0).getTime(), callQueries.callTime(null).getTime());
        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setTime(2,01:00:00)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.TIME + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getTime(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.TIME + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.TIME + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getTime(1)", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCallTimestamp() throws SQLException {
        List<Object> result = new ArrayList<>();
        result.add(new java.sql.Timestamp(0));
        ((MockConnectionData) connection).setResultData(result);
        assertEquals(new java.util.Date(0).getTime(), callQueries.callTimestamp(new java.util.Date(0)).getTime());
        assertEquals(new java.util.Date(0).getTime(), callQueries.callTimestamp(null).getTime());
        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setTimestamp(2,1970-01-01 01:00:00.0)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.TIMESTAMP + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getTimestamp(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareCall({  ? = call func ( ? )  })", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.TIMESTAMP + ")", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.TIMESTAMP + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getTimestamp(1)", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    public interface CallQueries extends BaseQuery {
        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        boolean callBoolean1(boolean a, Boolean b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        Boolean callBoolean2(boolean a, Boolean b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        byte callByte1(byte a, Byte b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        Byte callByte2(byte a, Byte b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        char callChar1(char a, Character b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        Character callChar2(char a, Character b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        short callShort1(short a, Short b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        Short callShort2(short a, Short b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        int callInteger1(int a, Integer b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        Integer callInteger2(int a, Integer b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        long callLong1(long a, Long b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        Long callLong2(long a, Long b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        float callFloat1(float a, Float b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        Float callFloat2(float a, Float b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        double callDouble1(double a, Double b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}, {%2}) }")
        Double callDouble2(double a, Double b) throws SQLException;

        @Call(sql = "{ {%%} = call func ({%1}) }")
        String callString(String a) throws SQLException;

        @Call(sql = "{ {date %%} = call func ({date %1}) }")
        java.util.Date callDate(java.util.Date a) throws SQLException;

        @Call(sql = "{ {time %%} = call func ({time %1}) }")
        java.util.Date callTime(java.util.Date a) throws SQLException;

        @Call(sql = "{ {timestamp %%} = call func ({timestamp %1}) }")
        java.util.Date callTimestamp(java.util.Date a) throws SQLException;
    }
}
