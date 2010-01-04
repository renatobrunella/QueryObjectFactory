package sf.qof.customizer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.objectweb.asm.Type;

import sf.qof.BaseQuery;
import sf.qof.Query;
import sf.qof.QueryObjectFactory;
import sf.qof.session.UseSessionContext;
import sf.qof.testtools.MockConnectionData;
import sf.qof.testtools.MockConnectionFactory;


public class CustomizerTest extends TestCase {

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

  Connection connection;
  TestInterface1 testQueries1;
  TestInterface2 testQueries2;

  public void setUp() {
    QueryObjectFactory.setCustomizer(new TestCustomizer());
    testQueries1 = QueryObjectFactory.createQueryObject(TestInterface1.class);
    testQueries2 = QueryObjectFactory.createQueryObject(TestInterface2.class);
    QueryObjectFactory.setDefaultCustomizer();
	connection = MockConnectionFactory.getConnection();
    testQueries1.setConnection(connection);
    testQueries1.setFetchSize(99);
  }

  public void testCustomizerClassName() throws SecurityException, NoSuchMethodException {
    assertNotNull(testQueries1);
    assertEquals(TestInterface1.class.getName() + "TEST", testQueries1.getClass().getName());
  }

  public void testCustomizerListType() throws SQLException {
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", new Integer(11));

    ((MockConnectionData)connection).setResultSetData(results);
    List<Integer> resultList = testQueries1.selectList();
    assertNotNull(resultList);
    assertEquals(LinkedList.class, resultList.getClass());
  }

  public void testCustomizerSetType() throws SQLException {
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", new Integer(11));

    ((MockConnectionData)connection).setResultSetData(results);
    Set<Integer> resultList = testQueries1.selectSet();
    assertNotNull(resultList);
    assertEquals(TreeSet.class, resultList.getClass());
  }
  
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

}
