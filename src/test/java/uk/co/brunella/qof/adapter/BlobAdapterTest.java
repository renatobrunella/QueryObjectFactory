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

public class BlobAdapterTest {

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
        data.put("blob", new byte[]{1, 2, 3});
        ((MockConnectionData) connection).setResultSetData(results);
        byte[] ba = selectQueries.select();
        assertNotNull(ba);
        assertEquals(1, ba[0]);
        assertEquals(2, ba[1]);
        assertEquals(3, ba[2]);
        assertEquals(8, log.size());
        int i = 0;
        assertEquals("prepareStatement(select blob from test )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getBlob(blob)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testSelect2() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        results.add(data);
        data.put("blob", new byte[]{1, 2, 3});
        ((MockConnectionData) connection).setResultSetData(results);
        byte[] ba = selectQueries.select(new byte[]{5, 6});
        assertNotNull(ba);
        assertEquals(1, ba[0]);
        assertEquals(2, ba[1]);
        assertEquals(3, ba[2]);
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select blob from test where blob = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setBinaryStream(1,java.io.ByteArrayInputStream", log.get(i++).substring(0, "setBinaryStream(1,java.io.ByteArrayInputStream".length()));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getBlob(blob)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testCall() throws SQLException {
        List<Object> results = new ArrayList<>();
        results.add(new byte[]{1, 2, 3});
        ((MockConnectionData) connection).setResultData(results);
        byte[] ba = selectQueries.call(new byte[]{5, 6});
        assertNotNull(ba);
        assertEquals(1, ba[0]);
        assertEquals(2, ba[1]);
        assertEquals(3, ba[2]);
        assertEquals(6, log.size());
        int i = 0;
        assertEquals("prepareCall({  ? = call ( ? )  })", log.get(i++));
        assertEquals("setBinaryStream(2,java.io.ByteArrayInputStream",
                log.get(i++).substring(0, "setBinaryStream(2,java.io.ByteArrayInputStream".length()));
        assertEquals("registerOutParameter(1,2004)", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getBlob(1)", log.get(i++));
        assertEquals("close()", log.get(i));
    }

    @Test
    public void testRegister() {
        BlobAdapter.register("BlobAdapter");
        assertTrue(QueryObjectFactory.isMapperRegistered("BlobAdapter"));
        QueryObjectFactory.unregisterMapper("BlobAdapter");
    }

    @Test
    public void testGetNumberOfColumns() {
        assertEquals(1, new BlobAdapter().getNumberOfColumns());
    }

    @Test
    public void testBlobReader() {
        BlobReader blobReader = new BlobReader();
        assertNotNull(blobReader);
    }

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select blob {blob%%} from test")
        byte[] select() throws SQLException;

        @Query(sql = "select blob {blob%%} from test where blob = {blob%1}")
        byte[] select(byte[] blob) throws SQLException;

        @Call(sql = "{ {blob%%} = call ( {blob%1} )}")
        byte[] call(byte[] blob) throws SQLException;
    }
}
