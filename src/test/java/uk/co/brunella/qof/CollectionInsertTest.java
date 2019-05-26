package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CollectionInsertTest extends TestCase {

    Connection connection;
    InsertQueries insertQueries;
    List<String> log;

    public void setUp() {
        insertQueries = QueryObjectFactory.createQueryObject(InsertQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        insertQueries.setConnection(connection);
    }

    public void testInsertCollectionFullBatching() throws SQLException {
        List<Integer> list = new ArrayList<Integer>();
        list.add(11);
        list.add(22);
        list.add(33);
        insertQueries.setBatchSize(10);
        insertQueries.insertInts(list);
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsertCollectionSmallBatching() throws SQLException {
        List<Integer> list = new ArrayList<Integer>();
        list.add(11);
        list.add(22);
        list.add(33);
        insertQueries.setBatchSize(2);
        insertQueries.insertInts(list);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsertCollectionNoBatching() throws SQLException {
        List<Integer> list = new ArrayList<Integer>();
        list.add(11);
        list.add(22);
        list.add(33);
        insertQueries.setBatchSize(0);
        insertQueries.insertInts(list);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsertSetFullBatching() throws SQLException {
        Set<Integer> set = new TreeSet<Integer>();
        set.add(11);
        set.add(22);
        set.add(33);
        insertQueries.setBatchSize(10);
        insertQueries.insertInts(set);
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsertCollectionFullBatchingWithResult() throws SQLException {
        List<Integer> list = new ArrayList<Integer>();
        list.add(11);
        list.add(22);
        list.add(33);
        insertQueries.setBatchSize(10);
        int[] result = insertQueries.insertIntsWithReturn(list);
        int i = 0;
        assertEquals(3, result.length);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(3, result[2]);
        assertEquals(9, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsertCollectionFullBatchingWithResult2() throws SQLException {
        List<Integer> list = new ArrayList<Integer>();
        list.add(11);
        list.add(22);
        list.add(33);
        insertQueries.setBatchSize(2);
        int[] result = insertQueries.insertIntsWithReturn(list);
        int i = 0;
        assertEquals(3, result.length);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(1, result[2]);
        assertEquals(10, log.size());
        assertEquals("prepareStatement(insert into test values ( ? ) )", log
                .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsertBeanFullBatching() throws SQLException {
        List<TestBean> beanList = new ArrayList<TestBean>();
        TestBean bean = new TestBean();
        bean.setId(11);
        bean.setName("name");
        beanList.add(bean);
        bean = new TestBean();
        bean.setId(22);
        bean.setName(null);
        beanList.add(bean);
        bean = new TestBean();
        bean.setId(33);
        bean.setName("xyz");
        beanList.add(bean);
        insertQueries.setBatchSize(10);
        insertQueries.insertBean(beanList);
        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setString(2,name)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("setString(2,null)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("setString(2,xyz)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsertTwoCollectionFullBatching() throws SQLException {
        List<Integer> listInt = new ArrayList<Integer>();
        listInt.add(11);
        listInt.add(22);
        listInt.add(33);
        List<String> listStr = new ArrayList<String>();
        listStr.add("a11");
        listStr.add(null);
        listStr.add("a33");
        insertQueries.setBatchSize(10);
        insertQueries.insertTwo(listInt, listStr);
        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareStatement(insert into test values ( ? , ? ) )", log
                .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setString(2,a11)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("setString(2,null)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("setString(2,a33)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public interface InsertQueries extends BaseQuery {
        @Insert(sql = "insert into test values ({%1})")
        void insertInts(List<Integer> list) throws SQLException;

        @Insert(sql = "insert into test values ({%1})")
        void insertInts(Set<Integer> set) throws SQLException;

        @Insert(sql = "insert into test values ({%1})")
        int[] insertIntsWithReturn(List<Integer> list) throws SQLException;

        @Insert(sql = "insert into test values ({%1.id},{%1.name})")
        void insertBean(List<TestBean> bean) throws SQLException;

        @Insert(sql = "insert into test values ({%1},{%2})")
        void insertTwo(List<Integer> listInt, List<String> listString)
                throws SQLException;
    }

}
