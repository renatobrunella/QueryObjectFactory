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

public class BeanQueryTest extends TestCase {

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1})")
        TestBean select(int id) throws SQLException;

        @Query(sql = "select green {%%.green} from test where green = {%1.green}")
        TestBean selectBoolean1(TestBean bean) throws SQLException;

        @Query(sql = "select red {%%.red} from test where red = {%1.red}")
        TestBean selectBoolean2(TestBean bean) throws SQLException;
    }

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

    public void testSelectOneResult() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(11));
        data.put("num", new Integer(22));
        data.put("name", "abc");
        data.put("date", new java.sql.Date(0));

        ((MockConnectionData) connection).setResultSetData(results);
        TestBean bean = selectQueries.select(11);
        assertNotNull(bean);
        assertEquals(11, bean.getId());
        assertEquals(22, ((Integer) bean.getNum()).intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new java.util.Date(0), bean.getDate());

        int i = 0;
        assertEquals(13, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectNoResult() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

        ((MockConnectionData) connection).setResultSetData(results);
        TestBean bean = selectQueries.select(11);
        assertNull(bean);

        int i = 0;
        assertEquals(7, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectTwoResults() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(11));
        data.put("num", new Integer(22));
        data.put("name", "abc");
        data.put("date", new java.sql.Date(0));
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(12));
        data.put("num", new Integer(23));
        data.put("name", "abc");
        data.put("date", new java.sql.Date(0));

        ((MockConnectionData) connection).setResultSetData(results);
        try {
            selectQueries.select(11);
            fail("Expected exception");
        } catch (SQLException e) {
            assertEquals("More than one result in result set", e.getMessage());
        }

        int i = 0;
        assertEquals(13, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectOneResultBoolean1() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("green", Boolean.TRUE);

        ((MockConnectionData) connection).setResultSetData(results);
        TestBean bean = selectQueries.selectBoolean1(new TestBean());
        assertNotNull(bean);
        assertTrue(bean.getGreen());

        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select green from test where green = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setBoolean(1,false)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getBoolean(green)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectOneResultBoolean2() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("red", Boolean.TRUE);

        ((MockConnectionData) connection).setResultSetData(results);
        TestBean bean = selectQueries.selectBoolean2(new TestBean());
        assertNotNull(bean);
        assertTrue(bean.isRed());

        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select red from test where red = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setBoolean(1,false)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getBoolean(red)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

}
