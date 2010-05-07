package sf.qof;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import sf.qof.BaseQuery;
import sf.qof.QueryObjectFactory;
import sf.qof.testtools.MockConnectionData;
import sf.qof.testtools.MockConnectionFactory;

import junit.framework.TestCase;

public class SimpleUpdateTest extends TestCase {

  public interface UpdateQueries extends BaseQuery {
	@Update(sql = "update test set column = {%2} where column = {%1}")
	void updateByte(byte b1, Byte b2) throws SQLException;

	@Update(sql = "update test set column = {%2} where column = {%1}")
	void updateShort(short s1, Short s2) throws SQLException;

	@Update(sql = "update test set column = {%2} where column = {%1}")
	void updateInt(int i1, Integer i2) throws SQLException;

	@Update(sql = "update test set column = {%2} where column = {%1}")
	void updateLong(long l1, Long l2) throws SQLException;

	@Update(sql = "update test set column = {%2} where column = {%1}")
	void updateFloat(float f1, Float f2) throws SQLException;

	@Update(sql = "update test set column = {%2} where column = {%1}")
	void updateDouble(double d1, Double d2) throws SQLException;

	@Update(sql = "update test values ({%1}")
	void updateString(String s1) throws SQLException;

	@Update(sql = "update test set column = {%2} where column = {%1}")
	void updateChar(char c1, Character c2) throws SQLException;

	@Update(sql = "update test set column = {%1}")
	void updateDate0(java.util.Date d) throws SQLException;

	@Update(sql = "update test set column = {%1}")
	void updateDate1(java.util.Date d) throws SQLException;

	@Update(sql = "update test set column = {time%1}")
	void updateDate2(java.util.Date d) throws SQLException;

	@Update(sql = "update test set column = {timestamp%1}")
	void updateDate3(java.util.Date d) throws SQLException;

	@Update(sql = "update test set column = {%1}")
	int updateIntReturnUpdateCount(int i1) throws SQLException;
  }

  Connection connection;
  UpdateQueries updateQueries;
  List<String> log;

  public void setUp() {
	updateQueries = QueryObjectFactory.createQueryObject(UpdateQueries.class);
	connection = MockConnectionFactory.getConnection();
	log = ((MockConnectionData)connection).getLog();
	updateQueries.setConnection(connection);
  }

  public void testUpdateByte() throws SQLException {
	updateQueries.updateByte((byte) 11, new Byte((byte) 22));
	updateQueries.updateByte((byte) 33, null);
	int i = 0;
	assertEquals(10, log.size());
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setByte(1,22)", log.get(i++));
	assertEquals("setByte(2,11)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setNull(1," + java.sql.Types.TINYINT + ")", log.get(i++));
	assertEquals("setByte(2,33)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateShort() throws SQLException {
	updateQueries.updateShort((short) 11, new Short((short) 22));
	updateQueries.updateShort((short) 33, null);
	int i = 0;
	assertEquals(10, log.size());
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setShort(1,22)", log.get(i++));
	assertEquals("setShort(2,11)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setNull(1," + java.sql.Types.SMALLINT + ")", log.get(i++));
	assertEquals("setShort(2,33)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateInt() throws SQLException {
	updateQueries.updateInt((int) 11, new Integer((int) 22));
	updateQueries.updateInt((int) 33, null);
	int i = 0;
	assertEquals(10, log.size());
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setInt(1,22)", log.get(i++));
	assertEquals("setInt(2,11)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setNull(1," + java.sql.Types.INTEGER + ")", log.get(i++));
	assertEquals("setInt(2,33)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateLong() throws SQLException {
	updateQueries.updateLong((long) 11, new Long((long) 22));
	updateQueries.updateLong((long) 33, null);
	int i = 0;
	assertEquals(10, log.size());
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setLong(1,22)", log.get(i++));
	assertEquals("setLong(2,11)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setNull(1," + java.sql.Types.BIGINT + ")", log.get(i++));
	assertEquals("setLong(2,33)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateFloat() throws SQLException {
	updateQueries.updateFloat((float) 11.1, new Float((float) 22.2));
	updateQueries.updateFloat((float) 33.3, null);
	int i = 0;
	assertEquals(10, log.size());
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setFloat(1,22.2)", log.get(i++));
	assertEquals("setFloat(2,11.1)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setNull(1," + java.sql.Types.REAL + ")", log.get(i++));
	assertEquals("setFloat(2,33.3)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateDouble() throws SQLException {
	updateQueries.updateDouble((double) 11.1, new Double((double) 22.2));
	updateQueries.updateDouble((double) 33.3, null);
	int i = 0;
	assertEquals(10, log.size());
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setDouble(1,22.2)", log.get(i++));
	assertEquals("setDouble(2,11.1)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setNull(1," + java.sql.Types.DOUBLE + ")", log.get(i++));
	assertEquals("setDouble(2,33.3)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateDate0() throws SQLException {
	updateQueries.updateDate0(new java.util.Date(0));
	updateQueries.updateDate0(null);
	int i = 0;
	assertEquals(8, log.size());
	assertEquals("prepareStatement(update test set column = ? )", log.get(i++));
	assertEquals("setDate(1,1970-01-01)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals("prepareStatement(update test set column = ? )", log.get(i++));
	assertEquals("setNull(1," + java.sql.Types.DATE + ")", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateDate1() throws SQLException {
	updateQueries.updateDate1(new java.util.Date(0));
	updateQueries.updateDate1(null);
	int i = 0;
	assertEquals(8, log.size());
	assertEquals("prepareStatement(update test set column = ? )", log.get(i++));
	assertEquals("setDate(1,1970-01-01)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals("prepareStatement(update test set column = ? )", log.get(i++));
	assertEquals("setNull(1," + java.sql.Types.DATE + ")", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateDate2() throws SQLException {
	updateQueries.updateDate2(new java.util.Date(0));
	updateQueries.updateDate2(null);
	int i = 0;
	assertEquals(8, log.size());
	assertEquals("prepareStatement(update test set column = ? )", log.get(i++));
	assertEquals("setTime(1,01:00:00)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals("prepareStatement(update test set column = ? )", log.get(i++));
	assertEquals("setNull(1," + java.sql.Types.TIME + ")", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateDate3() throws SQLException {
	updateQueries.updateDate3(new java.util.Date(0));
	updateQueries.updateDate3(null);
	int i = 0;
	assertEquals(8, log.size());
	assertEquals("prepareStatement(update test set column = ? )", log.get(i++));
	assertEquals("setTimestamp(1,1970-01-01 01:00:00.0)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals("prepareStatement(update test set column = ? )", log.get(i++));
	assertEquals("setNull(1," + java.sql.Types.TIMESTAMP + ")", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateString() throws SQLException {
	updateQueries.updateString("abc");
	updateQueries.updateString(null);
	int i = 0;
	assertEquals(8, log.size());
	assertEquals("prepareStatement(update test values ( ? )", log.get(i++));
	assertEquals("setString(1,abc)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals("prepareStatement(update test values ( ? )", log.get(i++));
	assertEquals("setString(1,null)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateChar() throws SQLException {
	updateQueries.updateChar('a', new Character('b'));
	updateQueries.updateChar('c', null);
	int i = 0;
	assertEquals(10, log.size());
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setString(1,b)", log.get(i++));
	assertEquals("setString(2,a)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
	assertEquals(
		"prepareStatement(update test set column = ? where column = ? )", log
			.get(i++));
	assertEquals("setString(1,null)", log.get(i++));
	assertEquals("setString(2,c)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

  public void testUpdateIntReturnUpdateCount() throws SQLException {
	int count = updateQueries.updateIntReturnUpdateCount(10);
	int i = 0;
	assertEquals(1, count);
	assertEquals(4, log.size());
	assertEquals("prepareStatement(update test set column = ? )", log.get(i++));
	assertEquals("setInt(1,10)", log.get(i++));
	assertEquals("executeUpdate()", log.get(i++));
	assertEquals("close()", log.get(i++));
  }

}
