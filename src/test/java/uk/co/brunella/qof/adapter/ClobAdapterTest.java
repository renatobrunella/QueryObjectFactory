package uk.co.brunella.qof.adapter;

import org.junit.Before;
import org.junit.Test;
import uk.co.brunella.qof.BaseQuery;
import uk.co.brunella.qof.Call;
import uk.co.brunella.qof.Query;
import uk.co.brunella.qof.QueryObjectFactory;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ClobAdapterTest {

    private Connection connection;
    private SelectQueries selectQueries;
    private List<String> log;

    @Before
    public void setUp() {
        selectQueries = QueryObjectFactory.createQueryObject(SelectQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        selectQueries.setConnection(connection);
        selectQueries.setFetchSize(99);
    }

    @Test
    public void testSelect() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        results.add(data);
        data.put("clob", "ABC");
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals("ABC", selectQueries.select("XYZ"));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select clob from test where clob = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setCharacterStream(1,java.io.StringReader", log.get(i++).substring(0, "setCharacterStream(1,java.io.StringReader".length()));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getClob(clob)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCall() throws SQLException {
        List<Object> results = new ArrayList<>();
        results.add("ABC");
        ((MockConnectionData) connection).setResultData(results);
        assertEquals("ABC", selectQueries.call("XYZ"));
        assertEquals(6, log.size());
        int i = 0;
        assertEquals("prepareCall({  ? = call proc( ? )  })", log.get(i++));
        assertEquals("setCharacterStream(2,java.io.StringReader", log.get(i++).substring(0, "setCharacterStream(2,java.io.StringReader".length()));
        assertEquals("registerOutParameter(1,2005)", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getClob(1)", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testRegister() {
        ClobAdapter.register("ClobAdapter");
        assertTrue(QueryObjectFactory.isMapperRegistered("ClobAdapter"));
        QueryObjectFactory.unregisterMapper("ClobAdapter");
    }

    @Test
    public void testGetNumberOfColumns() {
        assertEquals(1, new ClobAdapter().getNumberOfColumns());
    }

    @Test
    public void testClobReader() {
        ClobReader clobReader = new ClobReader();
        assertNotNull(clobReader);
    }

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select clob {clob%%} from test where clob = {clob%1}")
        String select(String clob) throws SQLException;

        @Call(sql = "{ {clob%%} = call proc({clob%1}) }")
        String call(String clob) throws SQLException;
    }
}
