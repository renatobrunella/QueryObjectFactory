package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BeanCallTest extends TestCase {

    Connection connection;
    CallQueries callQueries;
    List<String> log;

    public void setUp() {
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        callQueries = QueryObjectFactory.createQueryObject(CallQueries.class);
        callQueries.setConnection(connection);
    }

    public void testCallBeanA() throws SQLException {
        TestBean bean = new TestBean();
        bean.setId(11);
        bean.setNum(22);
        bean.setName("name");
        bean.setDate(new java.util.Date(0));
        callQueries.callBean(bean);
        int i = 0;
        assertEquals(7, log.size());
        assertEquals("prepareCall({  call xyz ( ? , ? , ? , ? )  })", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setInt(2,22)", log.get(i++));
        assertEquals("setString(3,name)", log.get(i++));
        assertEquals("setDate(4,1970-01-01)", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testCallBeanB() throws SQLException {
        TestBean bean = new TestBean();
        bean.setId(11);
        bean.setNum(null);
        bean.setName(null);
        bean.setDate(null);
        callQueries.callBean(bean);
        int i = 0;
        assertEquals(7, log.size());
        assertEquals("prepareCall({  call xyz ( ? , ? , ? , ? )  })", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setNull(2," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("setString(3,null)", log.get(i++));
        assertEquals("setNull(4," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testCallBean2() throws SQLException {
        List<Object> result = new ArrayList<Object>();
        result.add(new Integer(11));
        result.add(new Integer(22));
        result.add("Name");
        result.add(null);
        ((MockConnectionData) connection).setResultData(result);
        TestBean bean = callQueries.callBean2();
        assertNotNull(bean);
        assertEquals(11, bean.getId());
        assertEquals(new Integer(22), bean.getNum());
        assertEquals("Name", bean.getName());
        assertNull(bean.getDate());
        int i = 0;
        assertEquals(12, log.size());
        assertEquals("prepareCall({  call xyz ( ? , ? , ? , ? )  })", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("registerOutParameter(2," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("registerOutParameter(3," + java.sql.Types.VARCHAR + ")", log.get(i++));
        assertEquals("registerOutParameter(4," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getInt(1)", log.get(i++));
        assertEquals("getInt(2)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(3)", log.get(i++));
        assertEquals("getDate(4)", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testCallBean3() throws SQLException {
        List<Object> result = new ArrayList<Object>();
        result.add(new Integer(11));
        result.add(new Integer(22));
        result.add("Name");
        result.add(null);
        ((MockConnectionData) connection).setResultData(result);
        TestBean bean = callQueries.callBean3(11);
        assertNotNull(bean);
        assertEquals(11, bean.getId());
        assertEquals(new Integer(22), bean.getNum());
        assertEquals("Name", bean.getName());
        assertNull(bean.getDate());
        int i = 0;
        assertEquals(13, log.size());
        assertEquals("prepareCall({  call xyz ( ? , ? , ? , ? )  })", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("registerOutParameter(1," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("registerOutParameter(2," + java.sql.Types.INTEGER + ")", log.get(i++));
        assertEquals("registerOutParameter(3," + java.sql.Types.VARCHAR + ")", log.get(i++));
        assertEquals("registerOutParameter(4," + java.sql.Types.DATE + ")", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getInt(1)", log.get(i++));
        assertEquals("getInt(2)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(3)", log.get(i++));
        assertEquals("getDate(4)", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public interface CallQueries extends BaseQuery {
        @Call(sql = "{ call xyz ({%1.id},{%1.num},{%1.name},{%1.date}) }")
        void callBean(TestBean bean) throws SQLException;

        @Call(sql = "{ call xyz ({%%.id},{%%.num},{%%.name},{%%.date}) }")
        TestBean callBean2() throws SQLException;

        @Call(sql = "{ call xyz ({%%.id,%1},{%%.num},{%%.name},{%%.date}) }")
        TestBean callBean3(int id) throws SQLException;
    }
}
