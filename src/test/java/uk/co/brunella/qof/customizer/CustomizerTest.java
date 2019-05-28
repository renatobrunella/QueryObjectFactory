package uk.co.brunella.qof.customizer;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;
import uk.co.brunella.qof.BaseQuery;
import uk.co.brunella.qof.Query;
import uk.co.brunella.qof.QueryObjectFactory;
import uk.co.brunella.qof.session.UseSessionContext;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;


public class CustomizerTest {

    private Connection connection;
    private TestInterface1 testQueries1;
    private TestInterface2 testQueries2;
    private TestInterface3 testQueries3;

    @Before
    public void setUp() {
        QueryObjectFactory.setCustomizer(new TestCustomizer());
        testQueries1 = QueryObjectFactory.createQueryObject(TestInterface1.class);
        testQueries2 = QueryObjectFactory.createQueryObject(TestInterface2.class);
        testQueries3 = QueryObjectFactory.createQueryObject(TestInterface3.class);
        QueryObjectFactory.setDefaultCustomizer();
        connection = MockConnectionFactory.getConnection();
        testQueries1.setConnection(connection);
        testQueries1.setFetchSize(99);
    }

    @Test
    public void testCustomizerClassName() throws SecurityException {
        assertNotNull(testQueries1);
        assertEquals(TestInterface1.class.getName() + "TEST", testQueries1.getClass().getName());
    }

    @Test
    public void testCustomizerListType() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        results.add(data);
        data.put("id", 11);

        ((MockConnectionData) connection).setResultSetData(results);
        List<Integer> resultList = testQueries1.selectList();
        assertNotNull(resultList);
        assertEquals(LinkedList.class, resultList.getClass());
    }

    @Test
    public void testCustomizerSetType() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        results.add(data);
        data.put("id", 11);

        ((MockConnectionData) connection).setResultSetData(results);
        Set<Integer> resultList = testQueries1.selectSet();
        assertNotNull(resultList);
        assertEquals(TreeSet.class, resultList.getClass());
    }

    @Test
    public void testConnectionFactory() {
        assertSame(connection, testQueries1.getConnection());
        try {
            testQueries2.getConnection();
            fail("exception expected");
        } catch (Exception e) {
            assertEquals("Session is not running in thread for context TEST_CONTEXT", e.getMessage());
        }
        try {
            testQueries2.setConnection(null);
            fail("exception expected");
        } catch (Exception e) {
            assertEquals("Connection cannot be set", e.getMessage());
        }
    }

    @Test
    public void testConnectionFactory3() {
        assertSame(connection, testQueries1.getConnection());
        try {
            testQueries3.getConnection();
            fail("exception expected");
        } catch (Exception e) {
            assertEquals("Session is not running in thread for context DEFAULT_CONTEXT", e.getMessage());
        }
        try {
            testQueries3.setConnection(null);
            fail("exception expected");
        } catch (Exception e) {
            assertEquals("Connection cannot be set", e.getMessage());
        }
    }

    @Test
    public void testSetConnectionThrowsException() {
        // class only defines getConnection therefore set connection will throw exception
        TestClassNoSetConnection dao = QueryObjectFactory.createQueryObject(TestClassNoSetConnection.class);
        try {
            dao.setConnection(null);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Connection cannot be set", e.getMessage());
        }
    }

    @Test
    public void testSetConnectionInvalidSignature() {
        TestClassWrongSignature dao = QueryObjectFactory.createQueryObject(TestClassWrongSignature.class);
        try {
            Method method = dao.getClass().getDeclaredMethod("setConnection", Connection.class);
            try {
                method.invoke(dao, new Object[] {null});
                fail("Should throw exception");
            } catch (InvocationTargetException e) {
                assertEquals("Connection cannot be set", e.getTargetException().getMessage());
            }
        } catch (Exception e) {
            fail("fail");
        }

    }

    @Test
    public void testGetConnectionInvalidSignature() {
        TestClassWrongSignature2 dao = QueryObjectFactory.createQueryObject(TestClassWrongSignature2.class);
        try {
            Method method = dao.getClass().getDeclaredMethod("getConnection");
            try {
                method.invoke(dao);
                fail("Should throw exception");
            } catch (InvocationTargetException e) {
                assertEquals("Connection was not set", e.getTargetException().getMessage());
            }
        } catch (Exception e) {
            fail("fail");
        }
    }

    public interface TestInterface1 extends BaseQuery {
        @Query(sql = "select id {%%} from test")
        List<Integer> selectList() throws SQLException;

        @Query(sql = "select id {%%} from test")
        Set<Integer> selectSet() throws SQLException;
    }

    @UseSessionContext(name = "TEST_CONTEXT")
    public interface TestInterface2 extends BaseQuery {
        @Query(sql = "select id {%%} from test")
        List<Integer> selectList() throws SQLException;

        @Query(sql = "select id {%%} from test")
        Set<Integer> selectSet() throws SQLException;
    }

    @UseSessionContext()
    public interface TestInterface3 extends BaseQuery {
        @Query(sql = "select id {%%} from test")
        List<Integer> selectList() throws SQLException;

        @Query(sql = "select id {%%} from test")
        Set<Integer> selectSet() throws SQLException;
    }

    public abstract static class TestClassNoSetConnection implements BaseQuery {
        @Query(sql = "select id {%%} from test")
        public abstract List<Integer> selectList() throws SQLException;

        public Connection getConnection() {
            return null;
        }
    }

    public abstract static class TestClassWrongSignature {
        @Query(sql = "select id {%%} from test")
        public abstract List<Integer> selectList() throws SQLException;

        public int setConnection(Connection connection) {
            return 0;
        }

        public Connection getConnection() {
            return null;
        }
    }

    public abstract static class TestClassWrongSignature2 {
        @Query(sql = "select id {%%} from test")
        public abstract List<Integer> selectList() throws SQLException;

        public String getConnection() {
            return null;
        }

        public void setConnection(Connection connection) {
        }
    }

    public class TestCustomizer implements Customizer {

        public String getClassName(Class<?> queryDefinitionClass) {
            return queryDefinitionClass.getName() + "TEST";
        }

        public Type getListType() {
            return Type.getType("Ljava/util/LinkedList;");
        }

        public Type getMapType() {
            return Type.getType("Ljava/util/TreeMap;");
        }

        public Type getSetType() {
            return Type.getType("Ljava/util/TreeSet;");
        }

        public ConnectionFactoryCustomizer getConnectionFactoryCustomizer(Class<?> queryDefinitionClass) {
            return new DefaultCustomizer().getConnectionFactoryCustomizer(queryDefinitionClass);
        }
    }
}
