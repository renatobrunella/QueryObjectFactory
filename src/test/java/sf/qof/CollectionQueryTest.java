package sf.qof;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sf.qof.BaseQuery;
import sf.qof.QueryObjectFactory;
import sf.qof.testtools.MockConnectionData;
import sf.qof.testtools.MockConnectionFactory;

import junit.framework.TestCase;

public class CollectionQueryTest extends TestCase {

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}")
        List<TestBean> select(int id) throws SQLException;

        @Query(sql = "select id {%%} from test")
        Set<Integer> selectSet() throws SQLException;

        @Query(sql = "select id {%%.id,%%*}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}")
        Map<Integer, TestBean> selectMapInteger(int id) throws SQLException;

        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name,%%*}, date {%%.date} from test where id = {%1}")
        Map<String, TestBean> selectMapString(int id) throws SQLException;

        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date,%%*} from test where id = {%1}")
        Map<java.util.Date, TestBean> selectMapDate(int id) throws SQLException;
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
        List<TestBean> beanList = selectQueries.select(11);
        assertNotNull(beanList);
        assertEquals(1, beanList.size());
        TestBean bean = beanList.get(0);
        assertEquals(11, bean.getId());
        assertEquals(22, ((Integer) bean.getNum()).intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new java.util.Date(0), bean.getDate());

        int i = 0;
        assertEquals(13, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
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
        List<TestBean> beanList = selectQueries.select(11);
        assertNotNull(beanList);
        assertEquals(0, beanList.size());

        int i = 0;
        assertEquals(7, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
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
        data.put("date", null);

        ((MockConnectionData) connection).setResultSetData(results);
        List<TestBean> beanList = selectQueries.select(11);
        assertNotNull(beanList);
        assertEquals(2, beanList.size());

        TestBean bean = beanList.get(0);
        assertEquals(11, bean.getId());
        assertEquals(22, ((Integer) bean.getNum()).intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new java.util.Date(0), bean.getDate());
        bean = beanList.get(1);
        assertEquals(12, bean.getId());
        assertEquals(23, ((Integer) bean.getNum()).intValue());
        assertEquals("abc", bean.getName());
        assertNull(bean.getDate());

        int i = 0;
        assertEquals(19, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
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


    public void testSelectSet() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(11));
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(22));
        ((MockConnectionData) connection).setResultSetData(results);
        Set<Integer> set = selectQueries.selectSet();
        assertNotNull(set);
        assertEquals(2, set.size());
        assertTrue(set.contains(11));
        assertTrue(set.contains(22));

        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareStatement(select id from test )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectMapOneResult() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(11));
        data.put("num", new Integer(22));
        data.put("name", "abc");
        data.put("date", new java.sql.Date(0));

        ((MockConnectionData) connection).setResultSetData(results);
        Map<Integer, TestBean> beanMap = selectQueries.selectMapInteger(11);
        assertNotNull(beanMap);
        assertEquals(1, beanMap.size());
        TestBean bean = beanMap.get(Integer.valueOf(11));
        assertEquals(11, bean.getId());
        assertEquals(22, ((Integer) bean.getNum()).intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new java.util.Date(0), bean.getDate());

        int i = 0;
        assertEquals(15, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectMapTwoResults() throws SQLException {
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
        data.put("name", "xyz");
        data.put("date", null);

        ((MockConnectionData) connection).setResultSetData(results);
        Map<Integer, TestBean> beanMap = selectQueries.selectMapInteger(11);
        assertNotNull(beanMap);
        assertEquals(2, beanMap.size());
        TestBean bean = beanMap.get(Integer.valueOf(11));
        assertEquals(11, bean.getId());
        assertEquals(22, ((Integer) bean.getNum()).intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new java.util.Date(0), bean.getDate());
        bean = beanMap.get(Integer.valueOf(12));
        assertEquals(12, bean.getId());
        assertEquals(23, ((Integer) bean.getNum()).intValue());
        assertEquals("xyz", bean.getName());
        assertNull(bean.getDate());

        int i = 0;
        assertEquals(23, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectMapTwoResultsString() throws SQLException {
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
        data.put("name", "xyz");
        data.put("date", null);

        ((MockConnectionData) connection).setResultSetData(results);
        Map<String, TestBean> beanMap = selectQueries.selectMapString(11);
        assertNotNull(beanMap);
        assertEquals(2, beanMap.size());
        TestBean bean = beanMap.get("abc");
        assertEquals(11, bean.getId());
        assertEquals(22, ((Integer) bean.getNum()).intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new java.util.Date(0), bean.getDate());
        bean = beanMap.get("xyz");
        assertEquals(12, bean.getId());
        assertEquals(23, ((Integer) bean.getNum()).intValue());
        assertEquals("xyz", bean.getName());
        assertNull(bean.getDate());

        int i = 0;
        assertEquals(21, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectMapTwoResultsDate() throws SQLException {
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
        data.put("name", "xyz");
        data.put("date", null);

        ((MockConnectionData) connection).setResultSetData(results);
        Map<java.util.Date, TestBean> beanMap = selectQueries.selectMapDate(11);
        assertNotNull(beanMap);
        assertEquals(2, beanMap.size());
        TestBean bean = beanMap.get(new java.util.Date(0));
        assertEquals(11, bean.getId());
        assertEquals(22, ((Integer) bean.getNum()).intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new java.util.Date(0), bean.getDate());
        bean = beanMap.get(null);
        assertEquals(12, bean.getId());
        assertEquals(23, ((Integer) bean.getNum()).intValue());
        assertEquals("xyz", bean.getName());
        assertNull(bean.getDate());

        int i = 0;
        assertEquals(21, log.size());
        assertEquals("prepareStatement(select id , num , name , date from test where id = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("getInt(num)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("getDate(date)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }
}
