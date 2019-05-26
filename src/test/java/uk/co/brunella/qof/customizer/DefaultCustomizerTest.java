package uk.co.brunella.qof.customizer;

import junit.framework.TestCase;
import org.objectweb.asm.Type;
import uk.co.brunella.qof.BaseQuery;
import uk.co.brunella.qof.Query;
import uk.co.brunella.qof.session.UseSessionContext;

import java.sql.SQLException;
import java.util.List;

public class DefaultCustomizerTest extends TestCase {


    public interface TestInterface1 extends BaseQuery {
        @Query(sql = "select id {%%} from test")
        List<Integer> selectList() throws SQLException;
    }

    @UseSessionContext()
    public interface TestInterface2 extends BaseQuery {
        @Query(sql = "select id {%%} from test")
        List<Integer> selectList() throws SQLException;
    }

    public void testGetListType() {
        Type type = new DefaultCustomizer().getListType();
        assertEquals("java.util.ArrayList", type.getClassName());
    }

    public void testGetSetType() {
        Type type = new DefaultCustomizer().getSetType();
        assertEquals("java.util.HashSet", type.getClassName());
    }

    public void testGetMapType() {
        Type type = new DefaultCustomizer().getMapType();
        assertEquals("java.util.HashMap", type.getClassName());
    }

    public void testGetClassName() {
        String className = new DefaultCustomizer().getClassName(TestInterface1.class);
        assertEquals(getClass().getName() + "$TestInterface1$Impl", className);
    }

    public void testGetConnectionFactoryCustomizer() {
        ConnectionFactoryCustomizer customizer = new DefaultCustomizer().getConnectionFactoryCustomizer(TestInterface1.class);
        assertTrue(customizer instanceof DefaultConnectionFactoryCustomizer);
        customizer = new DefaultCustomizer().getConnectionFactoryCustomizer(TestInterface2.class);
        assertTrue(customizer instanceof SessionContextConnectionFactoryCustomizer);
    }
}
