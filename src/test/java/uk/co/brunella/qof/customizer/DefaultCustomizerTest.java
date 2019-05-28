package uk.co.brunella.qof.customizer;

import org.junit.Test;
import org.objectweb.asm.Type;
import uk.co.brunella.qof.BaseQuery;
import uk.co.brunella.qof.Query;
import uk.co.brunella.qof.session.UseSessionContext;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefaultCustomizerTest {


    @Test
    public void testGetListType() {
        Type type = new DefaultCustomizer().getListType();
        assertEquals("java.util.ArrayList", type.getClassName());
    }

    @Test
    public void testGetSetType() {
        Type type = new DefaultCustomizer().getSetType();
        assertEquals("java.util.HashSet", type.getClassName());
    }

    @Test
    public void testGetMapType() {
        Type type = new DefaultCustomizer().getMapType();
        assertEquals("java.util.HashMap", type.getClassName());
    }

    @Test
    public void testGetClassName() {
        String className = new DefaultCustomizer().getClassName(TestInterface1.class);
        assertEquals(getClass().getName() + "$TestInterface1$Impl", className);
    }

    @Test
    public void testGetConnectionFactoryCustomizer() {
        ConnectionFactoryCustomizer customizer = new DefaultCustomizer().getConnectionFactoryCustomizer(TestInterface1.class);
        assertTrue(customizer instanceof DefaultConnectionFactoryCustomizer);
        customizer = new DefaultCustomizer().getConnectionFactoryCustomizer(TestInterface2.class);
        assertTrue(customizer instanceof SessionContextConnectionFactoryCustomizer);
    }

    public interface TestInterface1 extends BaseQuery {
        @Query(sql = "select id {%%} from test")
        List<Integer> selectList() throws SQLException;
    }

    @UseSessionContext()
    public interface TestInterface2 extends BaseQuery {
        @Query(sql = "select id {%%} from test")
        List<Integer> selectList() throws SQLException;
    }
}
