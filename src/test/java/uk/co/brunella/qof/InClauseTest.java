package uk.co.brunella.qof;

import junit.framework.TestCase;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import uk.co.brunella.qof.adapter.DynamicMappingAdapter;
import uk.co.brunella.qof.adapter.GeneratorMappingAdapter;
import uk.co.brunella.qof.mapping.ParameterMapping;
import uk.co.brunella.qof.mapping.ResultMapping;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.*;
import java.util.*;

import static uk.co.brunella.qof.codegen.Constants.*;

public class InClauseTest extends TestCase {

    private Connection connection;
    private SelectQueries selectQueries;
    private UpdateQueries updateQueries;
    private DeleteQueries deleteQueries;
    private List<String> log;

    public void setUp() {
        QueryObjectFactory.registerMapper("dyn", new InClauseDynamicMappingAdapter());
        QueryObjectFactory.registerMapper("gen", new InClauseGeneratorMappingAdapter());
        selectQueries = QueryObjectFactory.createQueryObject(SelectQueries.class);
        QueryObjectFactory.unregisterMapper("dyn");
        QueryObjectFactory.unregisterMapper("gen");
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        selectQueries.setConnection(connection);
        selectQueries.setFetchSize(99);
        updateQueries = QueryObjectFactory.createQueryObject(UpdateQueries.class);
        updateQueries.setConnection(connection);
        deleteQueries = QueryObjectFactory.createQueryObject(DeleteQueries.class);
        deleteQueries.setConnection(connection);
    }

    public void testSelectStrings1() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "B");
        ((MockConnectionData) connection).setResultSetData(results);
        List<String> list = selectQueries.selectStrings1(new String[]{"a", "b"}, "c");
        assertEquals(2, list.size());
        assertEquals(13, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where name in ( ?,? ) and x = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("setString(2,b)", log.get(i++));
        assertEquals("setString(3,c)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectStrings2() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "B");
        ((MockConnectionData) connection).setResultSetData(results);
        List<String> list = selectQueries.selectStrings2(new String[]{"a", "b"}, new String[]{"x", "y", "z"});
        assertEquals(2, list.size());
        assertEquals(15, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where name in ( ?,? ) and category in ( ?,?,? )) )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("setString(2,b)", log.get(i++));
        assertEquals("setString(3,x)", log.get(i++));
        assertEquals("setString(4,y)", log.get(i++));
        assertEquals("setString(5,z)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectStrings3() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "B");
        ((MockConnectionData) connection).setResultSetData(results);
        List<String> list = selectQueries.selectStrings3(11, new String[]{"a", "b"}, 22, new String[]{"x", "y", "z"}, 33);
        assertEquals(2, list.size());
        assertEquals(18, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where topic_id = ? and " +
                "name in ( ?,? ) and x = ? and category in ( ?,?,? ) and y = ? ) )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setString(2,a)", log.get(i++));
        assertEquals("setString(3,b)", log.get(i++));
        assertEquals("setInt(4,22)", log.get(i++));
        assertEquals("setString(5,x)", log.get(i++));
        assertEquals("setString(6,y)", log.get(i++));
        assertEquals("setString(7,z)", log.get(i++));
        assertEquals("setInt(8,33)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectChar() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "B");
        ((MockConnectionData) connection).setResultSetData(results);
        List<String> list = selectQueries.selectChar(new char[]{'a', 'b'}, new Character[]{'x', 'y', 'z'}, 'u', 'v');
        assertEquals(2, list.size());
        assertEquals(17, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where x in ( ?,? ) and y in ( ?,?,? ) and z1 = ? and z2 = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("setString(2,b)", log.get(i++));
        assertEquals("setString(3,x)", log.get(i++));
        assertEquals("setString(4,y)", log.get(i++));
        assertEquals("setString(5,z)", log.get(i++));
        assertEquals("setString(6,u)", log.get(i++));
        assertEquals("setString(7,v)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectInteger() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "B");
        ((MockConnectionData) connection).setResultSetData(results);
        List<String> list = selectQueries.selectInteger(new int[]{11, 22}, new Integer[]{33, 44, 55}, 66, 77);
        assertEquals(2, list.size());
        assertEquals(17, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where x in ( ?,? ) and y in ( ?,?,? ) and z1 = ? and z2 = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,11)", log.get(i++));
        assertEquals("setInt(2,22)", log.get(i++));
        assertEquals("setInt(3,33)", log.get(i++));
        assertEquals("setInt(4,44)", log.get(i++));
        assertEquals("setInt(5,55)", log.get(i++));
        assertEquals("setInt(6,66)", log.get(i++));
        assertEquals("setInt(7,77)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectDate() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "B");
        ((MockConnectionData) connection).setResultSetData(results);
        List<String> list = selectQueries.selectDate(new java.util.Date[]{new java.util.Date(0), new java.util.Date(1000 * 84600)});
        assertEquals(2, list.size());
        assertEquals(12, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where x in ( ?,? ) )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setDate(1,1970-01-01)", log.get(i++));
        assertEquals("setDate(2,1970-01-02)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectDyn() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "B");
        ((MockConnectionData) connection).setResultSetData(results);
        List<String> list = selectQueries.selectDyn(new String[]{"a", "b"}, "c");
        assertEquals(2, list.size());
        assertEquals(13, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where x in ( ?,? ) and y = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("setString(2,b)", log.get(i++));
        assertEquals("setString(3,c)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectGen() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "B");
        ((MockConnectionData) connection).setResultSetData(results);
        List<String> list = selectQueries.selectGen(new String[]{"a", "b"}, "c");
        assertEquals(2, list.size());
        assertEquals(13, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where x in ( ?,? ) and y = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("setString(2,b)", log.get(i++));
        assertEquals("setString(3,c)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectWithSeparator() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "A");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("value", "B");
        ((MockConnectionData) connection).setResultSetData(results);
        List<String> list = selectQueries.selectWithSeparator(new String[]{"a", "b"}, "c");
        assertEquals(2, list.size());
        assertEquals(13, log.size());
        int i = 0;
        assertEquals("prepareStatement(select value from test where (x like ?  or x like  ? ) and y = ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setString(1,a)", log.get(i++));
        assertEquals("setString(2,b)", log.get(i++));
        assertEquals("setString(3,c)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(value)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testUpdate() throws SQLException {
        updateQueries.update("VALUE", new int[]{1, 2});
        int i = 0;
        assertEquals("prepareStatement(update test set value = ? where id in ( ?,? ) )", log.get(i++));
        assertEquals("setString(1,VALUE)", log.get(i++));
        assertEquals("setInt(2,1)", log.get(i++));
        assertEquals("setInt(3,2)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testUpdateCollection() throws SQLException {
        List<String> values = new ArrayList<String>();
        values.add("AAA");
        values.add("BBB");
        updateQueries.updateCollection(values, new int[]{1, 2});
        int i = 0;
        assertEquals("prepareStatement(update test set value = ? where id in ( ?,? ) )", log.get(i++));
        assertEquals("setString(1,AAA)", log.get(i++));
        assertEquals("setInt(2,1)", log.get(i++));
        assertEquals("setInt(3,2)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setString(1,BBB)", log.get(i++));
        assertEquals("setInt(2,1)", log.get(i++));
        assertEquals("setInt(3,2)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDelete() throws SQLException {
        deleteQueries.delete(new String[]{"AAA", "BBB", "CCC"});
        int i = 0;
        assertEquals("prepareStatement(delete from test where key in ( ?,?,? ) )", log.get(i++));
        assertEquals("setString(1,AAA)", log.get(i++));
        assertEquals("setString(2,BBB)", log.get(i++));
        assertEquals("setString(3,CCC)", log.get(i++));
        assertEquals("executeUpdate()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testDeleteCollection() throws SQLException {
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(1);
        ids.add(2);
        deleteQueries.deleteCollection(ids, new String[]{"AAA", "BBB"});
        int i = 0;
        assertEquals("prepareStatement(delete from test where id = ? key in ( ?,? ) )", log.get(i++));
        assertEquals("setInt(1,1)", log.get(i++));
        assertEquals("setString(2,AAA)", log.get(i++));
        assertEquals("setString(3,BBB)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("setInt(1,2)", log.get(i++));
        assertEquals("setString(2,AAA)", log.get(i++));
        assertEquals("setString(3,BBB)", log.get(i++));
        assertEquals("addBatch()", log.get(i++));
        assertEquals("executeBatch()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testInsert() {
        try {
            QueryObjectFactory.createQueryObject(InsertQueries.class);
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Array parameters are not allowed for insert statements", e.getCause().getMessage());
        }
    }

    public void testCall() {
        try {
            QueryObjectFactory.createQueryObject(CallQueries.class);
            fail("Exception expected");
        } catch (RuntimeException e) {
            assertEquals("Array parameters are not allowed for call statements", e.getCause().getMessage());
        }
    }

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select value {%%} from test where name in ({%1}) and x = {%2}")
        List<String> selectStrings1(String[] names, String x) throws SQLException;

        @Query(sql = "select value {%%} from test where name in ({%1}) and category in ({%2}))")
        List<String> selectStrings2(String[] names, String[] category) throws SQLException;

        @Query(sql = "select value {%%} from test where topic_id = {%1} and name in ({%2}) " +
                "and x = {%3} and category in ({%4}) and y = {%5})")
        List<String> selectStrings3(int topicId, String[] names, int x, String[] category, int y) throws SQLException;

        @Query(sql = "select value {%%} from test where x in ({%1}) and y in ({%2}) and z1 = {%3} and z2 = {%4}")
        List<String> selectChar(char[] x, Character[] y, char z1, Character z2) throws SQLException;

        @Query(sql = "select value {%%} from test where x in ({%1}) and y in ({%2}) and z1 = {%3} and z2 = {%4}")
        List<String> selectInteger(int[] x, Integer[] y, int z1, Integer z2) throws SQLException;

        @Query(sql = "select value {%%} from test where x in ({%1})")
        List<String> selectDate(java.util.Date[] x) throws SQLException;

        @Query(sql = "select value {%%} from test where x in ({dyn%1}) and y = {dyn%2}")
        List<String> selectDyn(String[] x, String y) throws SQLException;

        @Query(sql = "select value {%%} from test where x in ({gen%1}) and y = {gen%2}")
        List<String> selectGen(String[] x, String y) throws SQLException;

        @Query(sql = "select value {%%} from test where (x like {%1# or x like #}) and y = {gen%2}")
        List<String> selectWithSeparator(String[] x, String y) throws SQLException;
    }

    public interface UpdateQueries extends BaseQuery {
        @Update(sql = "update test set value = {%1} where id in ({%2})")
        void update(String value, int[] ids) throws SQLException;

        @Update(sql = "update test set value = {%1} where id in ({%2})")
        void updateCollection(List<String> values, int[] ids) throws SQLException;
    }

    public interface DeleteQueries extends BaseQuery {
        @Delete(sql = "delete from test where key in ({%1})")
        void delete(String[] keys) throws SQLException;

        @Delete(sql = "delete from test where id = {%1} key in ({%2})")
        void deleteCollection(List<Integer> ids, String[] keys) throws SQLException;
    }

    public interface InsertQueries extends BaseQuery {
        @Insert(sql = "insert into test values ({%1})")
        void insert(String[] x) throws SQLException;
    }

    public interface CallQueries extends BaseQuery {
        @Call(sql = "{ call test({%1}) }")
        void call(String[] x) throws SQLException;
    }

    public static class InClauseDynamicMappingAdapter implements DynamicMappingAdapter {

        public Object get(ResultSet rs, int[] indexes) throws SQLException {
            return null;
        }

        public Object get(ResultSet rs, String[] columns) throws SQLException {
            return null;
        }

        public Object get(CallableStatement cs, int[] indexes) throws SQLException {
            return null;
        }

        public void set(PreparedStatement ps, Object value, int[] indexes) throws SQLException {
            ps.setString(indexes[0], (String) value);
        }

        public void registerOutputParameter(CallableStatement cs, int[] indexes) throws SQLException {
            cs.registerOutParameter(indexes[0], java.sql.Types.VARCHAR);
        }

        public int getNumberOfColumns() {
            return 1;
        }

        public Set<Class<?>> getTypes() {
            Set<Class<?>> set = new HashSet<Class<?>>();
            set.add(String.class);
            return set;
        }

        public int[] preferredSqlTypes() {
            return new int[]{java.sql.Types.VARCHAR};
        }
    }

    public static class InClauseGeneratorMappingAdapter implements GeneratorMappingAdapter {

        public void generateFromResult(ResultMapping resultMapping, CodeEmitter co, Local resultSet, int[] indexes) {
        }

        public void generateFromResultSet(ResultMapping resultMapping, CodeEmitter co, Local resultSet, String[] columns) {
        }

        public void generateToPreparedStatement(ParameterMapping parameterMapping, CodeEmitter co, Local preparedStatement, int[] indexes, Local indexOffset) {
            // value is on the stack
            Local value = co.make_local(TYPE_String);
            co.store_local(value);

            co.load_local(preparedStatement);
            co.push(indexes[0]);
            if (indexOffset != null) {
                co.load_local(indexOffset);
                co.math(CodeEmitter.ADD, TYPE_int);
            }
            co.load_local(value);
            co.invoke_interface(TYPE_PreparedStatement, SIG_setString);
        }

        public void generateRegisterOutputParameters(ResultMapping resultMapping, CodeEmitter co, Local callableStatement,
                                                     int[] indexes) {
            throw new RuntimeException("Not implemented");
        }

        public int getNumberOfColumns() {
            return 1;
        }

        public Set<Class<?>> getTypes() {
            Set<Class<?>> set = new HashSet<Class<?>>();
            set.add(String.class);
            return set;
        }

        public int[] preferredSqlTypes() {
            return new int[]{java.sql.Types.VARCHAR};
        }
    }
}
