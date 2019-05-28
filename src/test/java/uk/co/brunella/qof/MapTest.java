package uk.co.brunella.qof;

import org.junit.Before;
import org.junit.Test;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MapTest {

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
    public void testMap() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        results.add(data);
        data.put("id", 1);
        data.put("name", "John");
        data = new HashMap<>();
        results.add(data);
        data.put("id", 2);
        data.put("name", "Peter");
        ((MockConnectionData) connection).setResultSetData(results);
        Map<Integer, String> resultMap = selectQueries.selectMap();
        assertNotNull(resultMap);
        assertEquals(2, resultMap.size());
        assertEquals("John", resultMap.get(1));
        assertEquals("Peter", resultMap.get(2));
        int i = 0;
        assertEquals(14, log.size());
        assertEquals("prepareStatement(select id , name from test )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getInt(id)", log.get(i++));
        assertEquals("wasNull()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select id {%%*}, name {%%} from test")
        Map<Integer, String> selectMap() throws SQLException;
    }

}
