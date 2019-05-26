package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SimpleDeleteTest extends TestCase {

    Connection connection;
    DeleteQueries deleteQueries;
    List<String> log;

    public void setUp() {
        deleteQueries = QueryObjectFactory.createQueryObject(DeleteQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        deleteQueries.setConnection(connection);
    }

    public void testDeleteByte() throws SQLException {
        deleteQueries.deleteByte((byte) 11, new Byte((byte) 22));
        deleteQueries.deleteByte((byte) 33, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setByte(1,11)", log.get(i++));
        assertEquals("setByte(2,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setByte(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.TINYINT + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteShort() throws SQLException {
        deleteQueries.deleteShort((short) 11, new Short((short) 22));
        deleteQueries.deleteShort((short) 33, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setShort(1,11)", log.get(i++));
        assertEquals("setShort(2,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setShort(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.SMALLINT + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteInt() throws SQLException {
        deleteQueries.deleteInt(11, new Integer(22));
        deleteQueries.deleteInt(33, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setInt(2,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteLong() throws SQLException {
        deleteQueries.deleteLong((long) 11, new Long((long) 22));
        deleteQueries.deleteLong((long) 33, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setLong(1,11)", log.get(i++));
        assertEquals("setLong(2,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setLong(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.BIGINT + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteFloat() throws SQLException {
        deleteQueries.deleteFloat((float) 11.1, new Float((float) 22.2));
        deleteQueries.deleteFloat((float) 33.3, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setFloat(1,11.1)", log.get(i++));
        assertEquals("setFloat(2,22.2)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setFloat(1,33.3)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.REAL + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteDouble() throws SQLException {
        deleteQueries.deleteDouble(11.1, new Double(22.2));
        deleteQueries.deleteDouble(33.3, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setDouble(1,11.1)", log.get(i++));
        assertEquals("setDouble(2,22.2)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setDouble(1,33.3)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.DOUBLE + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteDate0() throws SQLException {
        deleteQueries.deleteDate0(new java.util.Date(0));
        deleteQueries.deleteDate0(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(delete from test where column = ? )", log
                .get(i++));
        assertEquals("setDate(1,1970-01-01)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(delete from test where column = ? )", log
                .get(i++));
        assertEquals("setNull(1," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteDate1() throws SQLException {
        deleteQueries.deleteDate1(new java.util.Date(0));
        deleteQueries.deleteDate1(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(delete from test where column = ? )", log
                .get(i++));
        assertEquals("setDate(1,1970-01-01)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(delete from test where column = ? )", log
                .get(i++));
        assertEquals("setNull(1," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteDate2() throws SQLException {
        deleteQueries.deleteDate2(new java.util.Date(0));
        deleteQueries.deleteDate2(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(delete from test where column = ? )", log
                .get(i++));
        assertEquals("setTime(1,01:00:00)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(delete from test where column = ? )", log
                .get(i++));
        assertEquals("setNull(1," + java.sql.Types.TIME + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteDate3() throws SQLException {
        deleteQueries.deleteDate3(new java.util.Date(0));
        deleteQueries.deleteDate3(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(delete from test where column = ? )", log
                .get(i++));
        assertEquals("setTimestamp(1,1970-01-01 01:00:00.0)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(delete from test where column = ? )", log
                .get(i++));
        assertEquals("setNull(1," + java.sql.Types.TIMESTAMP + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteString() throws SQLException {
        deleteQueries.deleteString("abc");
        deleteQueries.deleteString(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(delete from test where column = ? ) )", log
                .get(i++));
        assertEquals("setString(1,abc)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(delete from test where column = ? ) )", log
                .get(i++));
        assertEquals("setString(1,null)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteChar() throws SQLException {
        deleteQueries.deleteChar('a', new Character('b'));
        deleteQueries.deleteChar('c', null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("setString(2,b)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals(
                "prepareStatement(delete from test where column = ? or column = ? )",
                log.get(i++));
        assertEquals("setString(1,c)", log.get(i++));
        assertEquals("setString(2,null)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteIntReturnUpdateCount() throws SQLException {
        int count = deleteQueries.deleteIntReturnUpdateCount(10);
        int i = 0;
        assertEquals(1, count);
        assertEquals(4, log.size());
        assertEquals("prepareStatement(delete from test where column = ? )", log
                .get(i++));
        assertEquals("setInt(1,10)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public interface DeleteQueries extends BaseQuery {
        @Delete(sql = "delete from test where column = {%1} or column = {%2}")
        void deleteByte(byte b1, Byte b2) throws SQLException;

        @Delete(sql = "delete from test where column = {%1} or column = {%2}")
        void deleteShort(short s1, Short s2) throws SQLException;

        @Delete(sql = "delete from test where column = {%1} or column = {%2}")
        void deleteInt(int i1, Integer i2) throws SQLException;

        @Delete(sql = "delete from test where column = {%1} or column = {%2}")
        void deleteLong(long l1, Long l2) throws SQLException;

        @Delete(sql = "delete from test where column = {%1} or column = {%2}")
        void deleteFloat(float f1, Float f2) throws SQLException;

        @Delete(sql = "delete from test where column = {%1} or column = {%2}")
        void deleteDouble(double d1, Double d2) throws SQLException;

        @Delete(sql = "delete from test where column = {%1})")
        void deleteString(String s1) throws SQLException;

        @Delete(sql = "delete from test where column = {%1} or column = {%2}")
        void deleteChar(char c1, Character c2) throws SQLException;

        @Delete(sql = "delete from test where column = {%1}")
        void deleteDate0(java.util.Date d) throws SQLException;

        @Delete(sql = "delete from test where column = {date%1}")
        void deleteDate1(java.util.Date d) throws SQLException;

        @Delete(sql = "delete from test where column = {time%1}")
        void deleteDate2(java.util.Date d) throws SQLException;

        @Delete(sql = "delete from test where column = {timestamp%1}")
        void deleteDate3(java.util.Date d) throws SQLException;

        @Delete(sql = "delete from test where column = {%1}")
        int deleteIntReturnUpdateCount(int i1) throws SQLException;
    }

}
