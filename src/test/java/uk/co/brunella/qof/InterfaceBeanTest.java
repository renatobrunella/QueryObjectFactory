package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.exception.ValidationException;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfaceBeanTest extends TestCase {

    public interface SelectQueries extends BaseQuery {
        @Query(sql = "select name {string%%1} where name = {%1.name})")
        Person select1(IPerson person) throws SQLException;

        @Query(sql = "select name {string%%1} where name = {%1.name})",
                factoryClass = InterfaceBeanTest.class, factoryMethod = "newPerson")
        IPerson select2(IPerson person) throws SQLException;
    }

    public interface SelectQueriesMissingType1 extends BaseQuery {
        @Query(sql = "select name {%%1} where name = {%1.name})")
        Person select1(IPerson person) throws SQLException;
    }

    public interface SelectQueriesMissingType2 extends BaseQuery {
        @Query(sql = "select name {%%1} where name = {%1.name})",
                factoryClass = InterfaceBeanTest.class, factoryMethod = "newPerson")
        IPerson select2(IPerson person) throws SQLException;
    }

    public static IPerson newPerson(String name) {
        return new Person(name);
    }

    Connection connection;
    SelectQueries selectQueries;
    List<String> log;

    public void setUp() {
        selectQueries = QueryObjectFactory.createQueryObject(SelectQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        selectQueries.setConnection(connection);
        selectQueries.setFetchSize(99);
    }

    public void testSelect1() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("name", "Tester");

        ((MockConnectionData) connection).setResultSetData(results);
        IPerson criteria = new Person("Tester");
        Person person = selectQueries.select1(criteria);
        assertNotNull(person);
        assertEquals("Tester", person.getName());

        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select name where name = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,Tester)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelect2() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("name", "Tester");

        ((MockConnectionData) connection).setResultSetData(results);
        IPerson criteria = new Person("Tester");
        IPerson person = selectQueries.select2(criteria);
        assertNotNull(person);
        assertEquals("Tester", person.getName());

        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select name where name = ? ) )", log.get(i++));
        assertEquals("setFetchSize(2)", log.get(i++));
        assertEquals("setString(1,Tester)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("getString(name)", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSelectMissingType1() {
        try {
            QueryObjectFactory.createQueryObject(SelectQueriesMissingType1.class);
            fail();
        } catch (ValidationException e) {
            assertEquals("Constructor parameters must have a type definition: {%%1} in method " +
                    InterfaceBeanTest.class.getName() + "$SelectQueriesMissingType1.select1", e.getMessage());
        }
    }

    public void testSelectMissingType2() {
        try {
            QueryObjectFactory.createQueryObject(SelectQueriesMissingType2.class);
            fail();
        } catch (ValidationException e) {
            assertEquals("Static factory method parameters must have a type definition: {%%1} in method " +
                    InterfaceBeanTest.class.getName() + "$SelectQueriesMissingType2.select2", e.getMessage());
        }
    }

    public static class Person implements IPerson {
        private String name;

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public interface IPerson {
        public abstract String getName();
    }
}
