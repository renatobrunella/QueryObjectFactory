package sf.qof;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import sf.qof.testtools.MockConnectionData;
import sf.qof.testtools.MockConnectionFactory;

public class FactoryMethodTest extends TestCase {

  public interface SelectQueries extends BaseQuery {

    @Query(sql = "select id {int%%1} from person",
        factoryClass = FactoryMethodTest.class, factoryMethod = "personFactory")
    Person getPerson1() throws SQLException;

    @Query(sql = "select id {int%%1}, name {string%%2} from person",
        factoryClass = FactoryMethodTest.class, factoryMethod = "personFactory")
    Person getPerson2() throws SQLException;
    
    @Query(sql = "select id {int%%1}, name {%%.name} from person",
        factoryClass = FactoryMethodTest.class, factoryMethod = "personFactory")
    Person getPerson3() throws SQLException;
    
    @Query(sql = "select id {int%%1} from person",
        factoryClass = FactoryMethodTest.class, factoryMethod = "personFactory")
    List<Person> getPersons1() throws SQLException;
    
    @Query(sql = "select id {int%%1}, name {string%%2} from person",
        factoryClass = FactoryMethodTest.class, factoryMethod = "personFactory")
    List<Person> getPersons2() throws SQLException;
    
    @Query(sql = "select id {int%%1}, name {%%.name} from person",
        factoryClass = FactoryMethodTest.class, factoryMethod = "personFactory")
    List<Person> getPersons3() throws SQLException;
    
    @Call(sql = "{ {int%%1} = call func () }",
        factoryClass = FactoryMethodTest.class, factoryMethod = "personFactory")
    Person callPerson1() throws SQLException;
    
    @Call(sql = "{ call func ({int%%1}, {string%%2}) }",
        factoryClass = FactoryMethodTest.class, factoryMethod = "personFactory")
    Person callPerson2() throws SQLException;
    
    @Call(sql = "{ call func ({int%%1}, {%%.name}) }",
        factoryClass = FactoryMethodTest.class, factoryMethod = "personFactory")
    Person callPerson3() throws SQLException;
    
  }
  
  public void testGetPerson1() throws SQLException {
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", 1);
    ((MockConnectionData)connection).setResultSetData(results);

    assertFalse(factoryCalled1);
    Person person = selectQueries.getPerson1();
    assertNotNull(person);
    assertEquals(1, person.getId());
    assertTrue(factoryCalled1);
  }
  
  public void testGetPerson2() throws SQLException {
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", 1);
    data.put("name", "tester");
    ((MockConnectionData)connection).setResultSetData(results);
    
    assertFalse(factoryCalled2);
    Person person = selectQueries.getPerson2();
    assertNotNull(person);
    assertEquals(1, person.getId());
    assertEquals("tester", person.getName());
    assertTrue(factoryCalled2);
  }
  
  public void testGetPerson3() throws SQLException {
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", 1);
    data.put("name", "tester");
    ((MockConnectionData)connection).setResultSetData(results);
    
    assertFalse(factoryCalled1);
    Person person = selectQueries.getPerson3();
    assertNotNull(person);
    assertEquals(1, person.getId());
    assertEquals("tester", person.getName());
    assertTrue(factoryCalled1);
  }
  
  public void testGetPersons1() throws SQLException {
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", 1);
    data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", 2);
    ((MockConnectionData)connection).setResultSetData(results);
    
    assertFalse(factoryCalled1);
    List<Person> list = selectQueries.getPersons1();
    assertNotNull(list);
    assertEquals(2, list.size());
    assertEquals(1, list.get(0).getId());
    assertEquals(2, list.get(1).getId());
    assertTrue(factoryCalled1);
  }
  
  public void testGetPersons2() throws SQLException {
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", 1);
    data.put("name", "tester1");
    data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", 2);
    data.put("name", "tester2");
    ((MockConnectionData)connection).setResultSetData(results);
    
    assertFalse(factoryCalled2);
    List<Person> list = selectQueries.getPersons2();
    assertNotNull(list);
    assertEquals(2, list.size());
    assertEquals(1, list.get(0).getId());
    assertEquals("tester1", list.get(0).getName());
    assertEquals(2, list.get(1).getId());
    assertEquals("tester2", list.get(1).getName());
    assertTrue(factoryCalled2);
  }
  
  public void testGetPersons3() throws SQLException {
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", 1);
    data.put("name", "tester1");
    data = new HashMap<String, Object>();
    results.add(data);
    data.put("id", 2);
    data.put("name", "tester2");
    ((MockConnectionData)connection).setResultSetData(results);
    
    assertFalse(factoryCalled1);
    List<Person> list = selectQueries.getPersons3();
    assertNotNull(list);
    assertEquals(2, list.size());
    assertEquals(1, list.get(0).getId());
    assertEquals("tester1", list.get(0).getName());
    assertEquals(2, list.get(1).getId());
    assertEquals("tester2", list.get(1).getName());
    assertTrue(factoryCalled1);
  }
  
  public void testCallPerson1() throws SQLException {
    List<Object> result = new ArrayList<Object>();
    result.add(new Integer(1));
    ((MockConnectionData)connection).setResultData(result);
    assertFalse(factoryCalled1);
    Person person = selectQueries.callPerson1();
    assertNotNull(person);
    assertEquals(1, person.getId());
    assertTrue(factoryCalled1);
  }
  
  public void testCallPerson2() throws SQLException {
    List<Object> result = new ArrayList<Object>();
    result.add(new Integer(1));
    result.add("tester");
    ((MockConnectionData)connection).setResultData(result);
    
    assertFalse(factoryCalled2);
    Person person = selectQueries.callPerson2();
    assertNotNull(person);
    assertEquals(1, person.getId());
    assertEquals("tester", person.getName());
    assertTrue(factoryCalled2);
  }
  
  public void testCallPerson3() throws SQLException {
    List<Object> result = new ArrayList<Object>();
    result.add(new Integer(1));
    result.add("tester");
    ((MockConnectionData)connection).setResultData(result);
    
    assertFalse(factoryCalled1);
    Person person = selectQueries.callPerson3();
    assertNotNull(person);
    assertEquals(1, person.getId());
    assertEquals("tester", person.getName());
    assertTrue(factoryCalled1);
  }

  private Connection connection;
  private SelectQueries selectQueries;

  public void setUp() {
    selectQueries = QueryObjectFactory.createQueryObject(SelectQueries.class);
    connection = MockConnectionFactory.getConnection();
    selectQueries.setConnection(connection);
    selectQueries.setFetchSize(99);
    factoryCalled1 = false;
    factoryCalled2 = false;
  }
  
  static boolean factoryCalled1;
  static boolean factoryCalled2;
  
  public static Person personFactory(int id) {
    factoryCalled1 = true;
    return new Person(id);
  }
  
  public static Person personFactory(int id, String name) {
    factoryCalled2 = true;
    return new Person(id, name);
  }
  
  public static class Person {
    private final int id;
    private String name;
    
    public Person(int id) {
      this.id = id;
    }
    
    public Person(int id, String name) {
      this.name = name;
      this.id = id;
    }
    
    public String getName() {
      return name;
    }
    
    public void setName(String name) {
      this.name = name;
    }
     
    public int getId() {
      return id;
    }
  }
}
