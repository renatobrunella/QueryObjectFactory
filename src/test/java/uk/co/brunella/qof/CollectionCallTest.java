package uk.co.brunella.qof;

import org.junit.Before;
import org.junit.Test;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class CollectionCallTest {

    private Connection connection;
    private CallQueries callQueries;
    private List<String> log;

    @Before
    public void setUp() {
        callQueries = QueryObjectFactory.createQueryObject(CallQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        callQueries.setConnection(connection);
    }

    @Test
    public void testCallCollectionFullBatching() throws SQLException {
        List<Integer> list = new ArrayList<>();
        list.add(11);
        list.add(22);
        list.add(33);
        callQueries.setBatchSize(10);
        callQueries.callInts(list);
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareCall({  call func ( ? )  })", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testCallCollectionSmallBatching() throws SQLException {
        List<Integer> list = new ArrayList<>();
        list.add(11);
        list.add(22);
        list.add(33);
        callQueries.setBatchSize(2);
        callQueries.callInts(list);
        int i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareCall({  call func ( ? )  })", log.get(i++));
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

    @Test
    public void testCallCollectionNoBatching() throws SQLException {
        List<Integer> list = new ArrayList<>();
        list.add(11);
        list.add(22);
        list.add(33);
        callQueries.setBatchSize(0);
        callQueries.callInts(list);
        int i = 0;
        assertEquals(8, log.size());
        assertEquals("prepareCall({  call func ( ? )  })", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testCallSetFullBatching() throws SQLException {
        Set<Integer> set = new TreeSet<>();
        set.add(11);
        set.add(22);
        set.add(33);
        callQueries.setBatchSize(10);
        callQueries.callInts(set);
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareCall({  call func ( ? )  })", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,22)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,33)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    @Test
    public void testCallBeanFullBatching() throws SQLException {
        List<TestBean> beanList = new ArrayList<>();
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
        callQueries.setBatchSize(10);
        callQueries.callBean(beanList);
        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareCall({  call func ( ? , ? )  })", log.get(i++));
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

    @Test
    public void testCallTwoCollectionFullBatching() throws SQLException {
        List<Integer> listInt = new ArrayList<>();
        listInt.add(11);
        listInt.add(22);
        listInt.add(33);
        List<String> listStr = new ArrayList<>();
        listStr.add("a11");
        listStr.add(null);
        listStr.add("a33");
        callQueries.setBatchSize(10);
        callQueries.callTwo(listInt, listStr);
        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareCall({  call func ( ? , ? )  })", log.get(i++));
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

    public interface CallQueries extends BaseQuery {
        @Call(sql = "{ call func ({%1}) }")
        void callInts(List<Integer> list) throws SQLException;

        @Call(sql = "{ call func ({%1}) }")
        void callInts(Set<Integer> list) throws SQLException;

        @Call(sql = "{ call func ({%1.id},{%1.name}) }")
        void callBean(List<TestBean> bean) throws SQLException;

        @Call(sql = "{ call func ({%1},{%2}) }")
        void callTwo(List<Integer> listInt, List<String> listString)
                throws SQLException;
    }
}
