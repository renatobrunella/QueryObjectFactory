package uk.co.brunella.qof.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class DelegatorFactoryTest {

    private static boolean initializerCalled;

    @Test
    public void testDelegatorFactory() {
        initializerCalled = false;
        Person person = DelegatorFactory.create(Person.class, PersonFactory.class, "John", "Smith");
        assertFalse(initializerCalled);
        assertEquals("John Smith", person.toString());
        assertTrue(initializerCalled);
    }

    @Test
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
            String thisShouldFail = person3.toString();
            fail("exception expected " + thisShouldFail);
        } catch (RuntimeException e) {
            assertEquals("wrong id", e.getMessage());
        }
    }

    public static class Person {
        private String firstName;
        private String lastName;

        public String toString() {
            return firstName + " " + lastName;
        }
    }

    public static class PersonFactory {
        @SuppressWarnings("unused")
        public static void initialize(Person person, String firstName, String lastName) {
            person.firstName = firstName;
            person.lastName = lastName;
            DelegatorFactoryTest.initializerCalled = true;
        }
    }

    public static class PersonFactory2 {
        @SuppressWarnings("unused")
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
}
