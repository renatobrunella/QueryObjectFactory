package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.adapter.DynamicMappingAdapter;
import uk.co.brunella.qof.util.TestClassLoader;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ClassLoaderSeparationTest extends TestCase {

    private static final String ADAPTER_NAME = "testadapter";

    public void test() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        // create two separate class loaders
        ClassLoader cl1 = TestClassLoader.createClassLoader(null);
        ClassLoader cl2 = TestClassLoader.createClassLoader(null);

        // load the factory classes
        Class<?> dao1Class = cl1.loadClass(Dao1Factory.class.getName());
        Class<?> dao2Class = cl2.loadClass(Dao2Factory.class.getName());

        // create the objects - both use the same adapter name but have their own class loader
        assertNotNull(dao1Class.newInstance());
        assertNotNull(dao2Class.newInstance());
    }

    public static class Dao1Factory {
        public Dao1Factory() {
            QueryObjectFactory.registerMapper(ADAPTER_NAME, new TestAdapter());
            QueryObjectFactory.createQueryObject(Dao.class);
        }

        public interface Dao extends BaseQuery {
            @Query(sql = "select {" + ADAPTER_NAME + "%%} from test where s = {" + ADAPTER_NAME + "%1}")
            public abstract String getSomething(String s) throws SQLException;
        }
    }

    public static class Dao2Factory {
        public Dao2Factory() {
            QueryObjectFactory.registerMapper(ADAPTER_NAME, new TestAdapter());
            QueryObjectFactory.createQueryObject(Dao.class);
        }

        public interface Dao extends BaseQuery {
            @Query(sql = "select {" + ADAPTER_NAME + "%%} from test where s = {" + ADAPTER_NAME + "%1}")
            public abstract String getSomething(String s) throws SQLException;
        }
    }

    public static class TestAdapter implements DynamicMappingAdapter {

        public Object get(ResultSet rs, int[] indexes) throws SQLException {
            return null;
        }

        public Object get(ResultSet rs, String[] columns) throws SQLException {
            return null;
        }

        public Object get(CallableStatement cs, int[] indexes) throws SQLException {
            return null;
        }

        public void registerOutputParameter(CallableStatement cs, int[] indexes) throws SQLException {
        }

        public void set(PreparedStatement ps, Object value, int[] indexes) throws SQLException {
        }

        public int getNumberOfColumns() {
            return 1;
        }

        public Set<Class<?>> getTypes() {
            Set<Class<?>> set = new HashSet<Class<?>>();
            set.add(String.class);
            return set;
        }

    }
}
