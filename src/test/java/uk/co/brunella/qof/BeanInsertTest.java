package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.util.List;

public class BeanInsertTest extends TestCase {

    Connection connection;
    InsertQueries insertQueries;
    List<String> log;

    public void setUp() {
        insertQueries = QueryObjectFactory.createQueryObject(InsertQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        insertQueries.setConnection(connection);
    }

    public void testInsertBean1() {
        TestBean bean = new TestBean();
        bean.setId(11);
        bean.setNum(22);
        bean.setName("name");
        bean.setDate(new java.util.Date(0));
        insertQueries.insertBean(bean);
        int i = 0;
        assertEquals(7, log.size());
        assertEquals(
                "prepareStatement(insert into test values ( ? , ? , ? , ? ) )", log
                        .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setInt(2,22)", log.get(i++));
        assertEquals("setString(3,name)", log.get(i++));
        assertEquals("setDate(4,1970-01-01)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsertBean2() {
        TestBean bean = new TestBean();
        bean.setId(11);
        bean.setNum(null);
        bean.setName(null);
        bean.setDate(null);
        insertQueries.insertBean(bean);
        int i = 0;
        assertEquals(7, log.size());
        assertEquals(
                "prepareStatement(insert into test values ( ? , ? , ? , ? ) )", log
                        .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("setString(3,null)", log.get(i++));
        assertEquals("setNull(4," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsertBean3() {
        TestBean bean = new TestBean();
        bean.setId(11);
        bean.setNum(22);
        bean.setName("name");
        bean.setDate(new java.util.Date(0));
        int num = insertQueries.insertBean2(bean);
        int i = 0;
        assertEquals(1, num);
        assertEquals(7, log.size());
        assertEquals(
                "prepareStatement(insert into test values ( ? , ? , ? , ? ) )", log
                        .get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setInt(2,22)", log.get(i++));
        assertEquals("setString(3,name)", log.get(i++));
        assertEquals("setDate(4,1970-01-01)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public interface InsertQueries extends BaseQuery {
        @Insert(sql = "insert into test values ({%1.id},{%1.num},{%1.name},{%1.date})")
        void insertBean(TestBean bean);

        @Insert(sql = "insert into test values ({%1.id},{%1.num},{%1.name},{%1.date})")
        int insertBean2(TestBean bean);
    }
}
