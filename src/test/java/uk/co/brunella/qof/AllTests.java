package uk.co.brunella.qof;

import junit.framework.TestSuite;
import junit.runner.ClassPathTestCollector;

import java.lang.reflect.Modifier;
import java.util.Enumeration;

public class AllTests extends TestSuite {
    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws ClassNotFoundException {
        TestSuite suite = new TestSuite();
        Enumeration e = new ClassPathTestCollector() {
            public boolean isTestClass(String name) {
                String separator = System.getProperty("file.separator");
                return name.endsWith("Test.class") && name.startsWith(separator + "uk" + separator + "co" + separator
                        + "brunella" + separator + "qof");
            }
        }.collectTests();
        while (e.hasMoreElements()) {
            Class<?> c = Class.forName((String) e.nextElement());
            if (c != null && (c.getModifiers() & Modifier.ABSTRACT) == 0)
                suite.addTestSuite(c);
        }
        return suite;
    }
}
