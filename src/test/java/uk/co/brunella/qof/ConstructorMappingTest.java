package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.util.*;

public class ConstructorMappingTest extends TestCase {

    Connection connection;
    PersonQueries personQueries;
    List<String> log;

    public void setUp() {
        personQueries = QueryObjectFactory.createQueryObject(PersonQueries.class);
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
        personQueries.setConnection(connection);
    }

    public static class Person {
        private int id;
        private String firstName;
        private String lastName;

        public Person(int id) {
            this.id = id;
        }

        public Person(int id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String toString() {
            return id + " " + firstName + " " + lastName;
        }
    }

    public interface PersonQueries extends BaseQuery {
        @Query(sql = "select id {int%%1}, first_name {string%%2}, last_name {string%%3} from person where id = {%1}")
        Person getPerson(int id);

        @Query(sql = "select id {int%%1}, first_name {string%%2}, last_name {string%%3} from person")
        List<Person> getPersons();

        @Call(sql = "{ {int%%1} = call get_person( {%1} ) }")
        Person getPersonCall(int id);

        @Call(sql = "{ {char%%1} = call get_char( ) }")
        Character getChar();

        @Call(sql = "{ {date%%1} = call get_date( ) }")
        MyDate getDate();
    }

    public static class MyDate extends Date {
        private static final long serialVersionUID = 1L;

        public MyDate(Date date) {
            super(date.getTime());
        }
    }

    public void testSelect() {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(55));
        data.put("first_name", "John");
        data.put("last_name", "Smith");
        ((MockConnectionData) connection).setResultSetData(results);
        Person person = personQueries.getPerson(55);
        assertNotNull(person);
        assertEquals("55 John Smith", person.toString());
        assertEquals("prepareStatement(select id , first_name , last_name from person where id = ? )", log.get(0));
    }

    public void testSelectCollection() {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        Map<String, Object> data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(55));
        data.put("first_name", "John");
        data.put("last_name", "Smith");
        data = new HashMap<String, Object>();
        results.add(data);
        data.put("id", new Integer(56));
        data.put("first_name", "Peter");
        data.put("last_name", "Smithers");
        ((MockConnectionData) connection).setResultSetData(results);
        List<Person> persons = personQueries.getPersons();
        assertNotNull(persons);
        assertEquals(2, persons.size());
        assertEquals("55 John Smith", persons.get(0).toString());
        assertEquals("56 Peter Smithers", persons.get(1).toString());
        assertEquals("prepareStatement(select id , first_name , last_name from person )", log.get(0));
    }

    public void testCall() {
        List<Object> data = new ArrayList<Object>();
        data.add(new Integer(55));
        ((MockConnectionData) connection).setResultData(data);
        Person person = personQueries.getPersonCall(55);
        assertNotNull(person);
        assertEquals("55 null null", person.toString());
        assertEquals("prepareCall({  ? = call get_person( ? )  })", log.get(0));
    }

    public void testCallChar() {
        List<Object> data = new ArrayList<Object>();
        data.add("x");
        ((MockConnectionData) connection).setResultData(data);
        Character c = personQueries.getChar();
        assertNotNull(c);
        assertEquals('x', c.charValue());
        assertEquals("prepareCall({  ? = call get_char( )  })", log.get(0));
    }

    public void testCallDate() {
        List<Object> data = new ArrayList<Object>();
        data.add(new java.sql.Date(1000000));
        ((MockConnectionData) connection).setResultData(data);
        MyDate date = personQueries.getDate();
        assertNotNull(date);
        assertEquals("Thu Jan 01 01:16:40 GMT 1970", date.toString());
        assertEquals("prepareCall({  ? = call get_date( )  })", log.get(0));
    }
}
