package sf.qof.util;

import junit.framework.TestCase;

public class DelegatorFactoryTest extends TestCase {

    public static class Person {
        private String firstName;
        private String lastName;

        public String toString() {
            return firstName + " " + lastName;
        }
    }

    public static boolean initializerCalled;

    public static class PersonFactory {
        public static void initialize(Person person, String firstName, String lastName) {
            person.firstName = firstName;
            person.lastName = lastName;
            DelegatorFactoryTest.initializerCalled = true;
        }
    }

    public static class PersonFactory2 {
        public static void initialize(Person person, Integer id) {
            if (id == 1) {
                person.firstName = "John";
                person.lastName = "Smith";
            } else if (id == 2) {
                person.firstName = "Peter";
                person.lastName = "Smithers";
            } else {
                throw new RuntimeException("wrong id");
            }
            DelegatorFactoryTest.initializerCalled = true;
        }
    }

    public void testDelegatorFactory() {
        initializerCalled = false;
        Person person = DelegatorFactory.create(Person.class, PersonFactory.class, "John", "Smith");
        assertFalse(initializerCalled);
        person.getClass();
        assertFalse(initializerCalled);
        assertEquals("John Smith", person.toString());
        assertTrue(initializerCalled);
    }

    public void testDelegatorFactory2() {
        initializerCalled = false;
        Person person1 = DelegatorFactory.create(Person.class, PersonFactory2.class, 1);
        Person person2 = DelegatorFactory.create(Person.class, PersonFactory2.class, 2);
        Person person3 = DelegatorFactory.create(Person.class, PersonFactory2.class, 3);
        assertFalse(initializerCalled);
        assertEquals("John Smith", person1.toString());
        assertTrue(initializerCalled);
        assertEquals("Peter Smithers", person2.toString());
        try {
            person3.toString();
            fail("exception expected");
        } catch (RuntimeException e) {
            assertEquals("wrong id", e.getMessage());
        }
    }
}
