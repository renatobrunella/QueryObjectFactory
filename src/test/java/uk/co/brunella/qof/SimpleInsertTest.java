package uk.co.brunella.qof;

import org.junit.Before;
import org.junit.Test;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimpleInsertTest {

    private Connection connection;
    private InsertQueries insertQueries;
    private List<String> log;

    @Before
    public void setUp() {
        insertQueries = QueryObjectFactory.createQueryObject(InsertQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        insertQueries.setConnection(connection);
    }

    @Test
    public void testInsertByte() throws SQLException {
        insertQueries.insertByte((byte) 11, (byte) 22);
        insertQueries.insertByte((byte) 33, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setByte(1,11)", log.get(i++));
        assertEquals("setByte(2,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setByte(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.TINYINT + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertShort() throws SQLException {
        insertQueries.insertShort((short) 11, (short) 22);
        insertQueries.insertShort((short) 33, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setShort(1,11)", log.get(i++));
        assertEquals("setShort(2,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setShort(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.SMALLINT + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertInt() throws SQLException {
        insertQueries.insertInt(11, 22);
        insertQueries.insertInt(33, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setInt(2,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertLong() throws SQLException {
        insertQueries.insertLong((long) 11, (long) 22);
        insertQueries.insertLong((long) 33, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setLong(1,11)", log.get(i++));
        assertEquals("setLong(2,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setLong(1,33)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.BIGINT + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertFloat() throws SQLException {
        insertQueries.insertFloat((float) 11.1, (float) 22.2);
        insertQueries.insertFloat((float) 33.3, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setFloat(1,11.1)", log.get(i++));
        assertEquals("setFloat(2,22.2)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setFloat(1,33.3)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.REAL + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertDouble() throws SQLException {
        insertQueries.insertDouble(11.1, 22.2);
        insertQueries.insertDouble(33.3, null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setDouble(1,11.1)", log.get(i++));
        assertEquals("setDouble(2,22.2)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setDouble(1,33.3)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.DOUBLE + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertDate0() throws SQLException {
        insertQueries.insertDate0(new java.util.Date(0));
        insertQueries.insertDate0(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setDate(1,1970-01-01)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setNull(1," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertDate1() throws SQLException {
        insertQueries.insertDate1(new java.util.Date(0));
        insertQueries.insertDate1(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setDate(1,1970-01-01)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setNull(1," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertDate2() throws SQLException {
        insertQueries.insertDate2(new java.util.Date(0));
        insertQueries.insertDate2(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setTime(1," + new java.sql.Time(0) + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setNull(1," + java.sql.Types.TIME + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertDate3() throws SQLException {
        insertQueries.insertDate3(new java.util.Date(0));
        insertQueries.insertDate3(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setTimestamp(1," + new java.sql.Timestamp(0) + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setNull(1," + java.sql.Types.TIMESTAMP + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertString() throws SQLException {
        insertQueries.insertString("abc");
        insertQueries.insertString(null);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setString(1,abc)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setString(1,null)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertChar() throws SQLException {
        insertQueries.insertChar('a', 'b');
        insertQueries.insertChar('c', null);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("setString(2,b)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setString(1,c)", log.get(i++));
        assertEquals("setString(2,null)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testInsertIntReturnUpdateCount() throws SQLException {
        int count = insertQueries.insertIntReturnUpdateCount(10);
        int i = 0;
        assertEquals(1, count);
        assertEquals(4, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setInt(1,10)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public interface InsertQueries extends BaseQuery {
        @Insert(sql = "insert into test values ({%1},{%2})")
        void insertByte(byte b1, Byte b2) throws SQLException;

        @Insert(sql = "insert into test values ({%1},{%2})")
        void insertShort(short s1, Short s2) throws SQLException;

        @Insert(sql = "insert into test values ({%1},{%2})")
        void insertInt(int i1, Integer i2) throws SQLException;

        @Insert(sql = "insert into test values ({%1},{%2})")
        void insertLong(long l1, Long l2) throws SQLException;

        @Insert(sql = "insert into test values ({%1},{%2})")
        void insertFloat(float f1, Float f2) throws SQLException;

        @Insert(sql = "insert into test values ({%1},{%2})")
        void insertDouble(double d1, Double d2) throws SQLException;

        @Insert(sql = "insert into test values ({%1})")
        void insertString(String s1) throws SQLException;

        @Insert(sql = "insert into test values ({%1},{%2})")
        void insertChar(char c1, Character c2) throws SQLException;

        @Insert(sql = "insert into test values ({%1})")
        void insertDate0(java.util.Date d) throws SQLException;

        @Insert(sql = "insert into test values ({date%1})")
        void insertDate1(java.util.Date d) throws SQLException;

        @Insert(sql = "insert into test values ({time%1})")
        void insertDate2(java.util.Date d) throws SQLException;

        @Insert(sql = "insert into test values ({timestamp%1})")
        void insertDate3(java.util.Date d) throws SQLException;

        @Insert(sql = "insert into test values ({%1})")
        int insertIntReturnUpdateCount(int i1) throws SQLException;
    }
}
