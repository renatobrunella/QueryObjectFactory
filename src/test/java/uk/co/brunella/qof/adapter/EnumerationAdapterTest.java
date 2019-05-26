package uk.co.brunella.qof.adapter;

import junit.framework.TestCase;
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

public class EnumerationAdapterTest extends TestCase {

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select value {enum%%} from test where e = {enum%1}")
        MyEnum selectEnum(MyEnum e) throws SQLException;

        @Query(sql = "select value {enum%%1} from test where e = {enum%1}")
        MyClass selectEnumConstructor(MyEnum e) throws SQLException;

        @Query(sql = "select value {enum%%*,enum%%} from test where e = {enum%1}")
        Map<MyEnum, MyEnum> selectEnumMap(MyEnum e) throws SQLException;

        @Call(sql = "{ {enum%%} = call proc( {enum%1} ) }")
        MyEnum callEnum(MyEnum e) throws SQLException;

        @Query(sql = "select value {my-enum%%} from test where e = {my-enum%1}")
        MyEnum selectEnum2(MyEnum e) throws SQLException;
    }

    public static class MyClass {
        private MyEnum value;

        public MyClass(MyEnum value) {
            this.value = value;
        }

        public MyEnum getValue() {
            return value;
        }
    }

    public enum MyEnum {
        A("a"),
        B("b"),
        C("c");

        private final String value;

        private MyEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static MyEnum getEnum(String value) {
            for (MyEnum e : values()) {
                if (e.getValue().equals(value)) {
                    return e;
                }
            }
            return null;
        }
    }

    private Connection connection;
    private SelectQueries selectQueries;
    private List<String> log;

    public void setUp() {
        EnumerationAdapter.register("my-enum", "getValue", "getEnum");
        selectQueries = QueryObjectFactory.createQueryObject(SelectQueries.class);
        QueryObjectFactory.unregisterMapper("my-enum");
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        selectQueries.setConnection(connection);
        selectQueries.setFetchSize(99);
    }

    public void testSelectEnum() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "C");
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(MyEnum.C, selectQueries.selectEnum(MyEnum.A));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where e = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,A)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectEnumConstructor() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "C");
        ((MockConnectionData) connection).setResultSetData(results);
        MyClass myClass = selectQueries.selectEnumConstructor(MyEnum.A);
        assertNotNull(myClass);
        assertEquals(MyEnum.C, myClass.getValue());
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where e = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,A)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectEnumMap() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "C");
        ((MockConnectionData) connection).setResultSetData(results);
        Map<MyEnum, MyEnum> map = selectQueries.selectEnumMap(MyEnum.A);
        assertNotNull(map);
        assertEquals(MyEnum.C, map.get(MyEnum.C));
        assertEquals(10, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where e = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setString(1,A)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testCallEnum() throws SQLException {
        List<Object> results = new ArrayList<Object>();
        results.add("C");
        ((MockConnectionData) connection).setResultData(results);
        assertEquals(MyEnum.C, selectQueries.callEnum(MyEnum.A));
        assertEquals(6, log.size());
        int i = 0;
        assertEquals("prepareCall({  ? = call proc( ? )  })", log.get(i++));
        assertEquals("setString(2,A)", log.get(i++));
        assertEquals("registerOutParameter(1,12)", log.get(i++));
        assertEquals("execute()", log.get(i++));
        assertEquals("getString(1)", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectEnum2() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "c");
        ((MockConnectionData) connection).setResultSetData(results);
        assertEquals(MyEnum.C, selectQueries.selectEnum2(MyEnum.A));
        assertEquals(9, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where e = ? )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }
}