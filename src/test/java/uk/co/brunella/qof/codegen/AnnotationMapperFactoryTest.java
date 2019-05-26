package uk.co.brunella.qof.codegen;

import junit.framework.TestCase;
import uk.co.brunella.qof.Query;
import uk.co.brunella.qof.exception.ValidationException;

import java.lang.reflect.Method;

public class AnnotationMapperFactoryTest extends TestCase {

    private String run(Method method) {
        try {
            AnnotationMapperFactory.create(TestInterface.class, method);
            return null;
        } catch (ValidationException e) {
            return e.getMessage();
        }
    }

    public void testInvalidParameterIndex() throws SecurityException, NoSuchMethodException {
        Method method1a = TestInterface.class.getMethod("test1", (Class[]) null);
        Method method1b = TestInterface.class.getMethod("test1", Integer.TYPE);
        assertEquals(
                "Invalid parameter index for method public abstract int " + AnnotationMapperFactoryTest.class.getName() + "$TestInterface.test1()",
                run(method1a));
        assertEquals(
                "Invalid parameter index for method public abstract int " + AnnotationMapperFactoryTest.class.getName() + "$TestInterface.test1(int)",
                run(method1b));
    }

    public void testInvalidParameterNoGetter() throws SecurityException, NoSuchMethodException {
        Method method = TestInterface.class.getMethod("test2", Integer.TYPE);
        assertEquals(
                "Cannot find or access getter for [field] in class int", run(method));
    }

    public void testInvalidResultNoSetter() throws SecurityException, NoSuchMethodException {
        Method method = TestInterface.class.getMethod("test3", Integer.TYPE);
        assertEquals(
                "Cannot find or access setter for field in class int for mapping type auto", run(method));
    }

    public interface TestInterface {
        @Query(sql = "select * from test where id = {%1}")
        int test1();

        @Query(sql = "select * from test where id = {%2}")
        int test1(int i);

        @Query(sql = "select * from test where id = {%1.field}")
        int test2(int i);

        @Query(sql = "select col {%%.field} from test where id = {%1}")
        int test3(int i);
    }
}
