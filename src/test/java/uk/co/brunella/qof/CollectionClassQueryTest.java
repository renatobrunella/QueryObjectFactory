package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.exception.ValidationException;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class CollectionClassQueryTest extends TestCase {

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

    public void testFailsNoCollectionReturned() {
        try {
            QueryObjectFactory.createQueryObject(SelectQueriesFailsNoCollectionReturned.class);
            fail("Should throw exception");
        } catch (ValidationException e) {
            assertEquals("Return type of method must be a collection if collectionClass is defined", e.getMessage());
        }
    }

    public void testFailsNotAssignable() {
        try {
            QueryObjectFactory.createQueryObject(SelectQueriesFailsNotAssignable.class);
            fail("Should throw exception");
        } catch (ValidationException e) {
            assertEquals("Cannot assign java.util.TreeSet to return type java.util.List", e.getMessage());
        }
    }

    public void testFailsIsInterface() {
        try {
            QueryObjectFactory.createQueryObject(SelectQueriesFailsIsInterface.class);
            fail("Should throw exception");
        } catch (ValidationException e) {
            assertEquals("collectionClass cannot be an interface java.util.List", e.getMessage());
        }
    }

    public void testFailsIsAbstract() {
        try {
            QueryObjectFactory.createQueryObject(SelectQueriesFailsIsAbstract.class);
            fail("Should throw exception");
        } catch (ValidationException e) {
            assertEquals("collectionClass cannot be an abstract class java.util.AbstractList", e.getMessage());
        }
    }

    public void testFailsNoConstructorForInitialCapacity() {
        try {
            QueryObjectFactory.createQueryObject(SelectQueriesFailsNoConstructorForInitialCapacity.class);
            fail("Should throw exception");
        } catch (ValidationException e) {
            assertEquals("Type java.util.LinkedList does not have constructor to set initial capacity", e.getMessage());
        }
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
        assertEquals(LinkedList.class, beanList.getClass());
        assertEquals(1, beanList.size());
        TestBean bean = beanList.get(0);
        assertEquals(11, bean.getId());
        assertEquals(22, bean.getNum().intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new Date(0), bean.getDate());

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
        assertEquals(LinkedList.class, beanList.getClass());
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
        assertEquals(22, bean.getNum().intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new Date(0), bean.getDate());
        bean = beanList.get(1);
        assertEquals(12, bean.getId());
        assertEquals(23, bean.getNum().intValue());
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
        assertEquals(TreeMap.class, beanMap.getClass());
        assertEquals(1, beanMap.size());
        TestBean bean = beanMap.get(Integer.valueOf(11));
        assertEquals(11, bean.getId());
        assertEquals(22, bean.getNum().intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new Date(0), bean.getDate());

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
        assertEquals(22, bean.getNum().intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new Date(0), bean.getDate());
        bean = beanMap.get(Integer.valueOf(12));
        assertEquals(12, bean.getId());
        assertEquals(23, bean.getNum().intValue());
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

    public void testSelectListInitialCapacity() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(11));
        data.put("num", new Integer(22));
        data.put("name", "abc");
        data.put("date", new java.sql.Date(0));

        ((MockConnectionData) connection).setResultSetData(results);
        List<TestBean> beanList = selectQueries.selectList(11);
        assertNotNull(beanList);
        assertEquals(TestableArrayList.class, beanList.getClass());
        TestableArrayList<TestBean> list = (TestableArrayList<TestBean>) beanList;
        assertEquals(100, list.getInitialCapacity());
        assertEquals(1, beanList.size());
        TestBean bean = beanList.get(0);
        assertEquals(11, bean.getId());
        assertEquals(22, bean.getNum().intValue());
        assertEquals("abc", bean.getName());
        assertEquals(new Date(0), bean.getDate());

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

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}",
                collectionClass = LinkedList.class)
        List<TestBean> select(int id) throws SQLException;

        @Query(sql = "select id {%%} from test",
                collectionClass = TreeSet.class)
        Set<Integer> selectSet() throws SQLException;

        @Query(sql = "select id {%%.id,%%*}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}",
                collectionClass = TreeMap.class)
        Map<Integer, TestBean> selectMapInteger(int id) throws SQLException;

        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}",
                collectionClass = TestableArrayList.class, collectionInitialCapacity = 100)
        List<TestBean> selectList(int id) throws SQLException;
    }

    public interface SelectQueriesFailsNoCollectionReturned extends BaseQuery {
        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}",
                collectionClass = LinkedList.class)
        TestBean select(int id) throws SQLException;
    }

    public interface SelectQueriesFailsNotAssignable extends BaseQuery {
        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}",
                collectionClass = TreeSet.class)
        List<TestBean> select(int id) throws SQLException;
    }


    public interface SelectQueriesFailsIsInterface extends BaseQuery {
        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}",
                collectionClass = List.class)
        List<TestBean> select(int id) throws SQLException;
    }

    public interface SelectQueriesFailsIsAbstract extends BaseQuery {
        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}",
                collectionClass = AbstractList.class)
        List<TestBean> select(int id) throws SQLException;
    }

    public interface SelectQueriesFailsNoConstructorForInitialCapacity extends BaseQuery {
        @Query(sql = "select id {%%.id}, num {%%.num}, name {%%.name}, date {%%.date} from test where id = {%1}",
                collectionClass = LinkedList.class, collectionInitialCapacity = 100)
        List<TestBean> select(int id) throws SQLException;
    }

    @SuppressWarnings("serial")
    public static class TestableArrayList<E> extends ArrayList<E> {

        private int initialCapacity;

        public TestableArrayList(int initialCapacity) {
            super(initialCapacity);
            this.initialCapacity = initialCapacity;
        }

        public int getInitialCapacity() {
            return initialCapacity;
        }
    }

}
