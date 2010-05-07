package sf.qof.bundle;

import java.sql.SQLException;

import sf.qof.BaseQuery;
import sf.qof.Query;
import sf.qof.QueryObjectFactoryService;
import sf.qof.adapter.ClobAdapter;
import sf.qof.adapter.CommonAdapterRegistrar;
import sf.qof.customizer.DefaultCustomizer;
import sf.qof.dialect.HSQLDbDialect;
import junit.framework.TestCase;

public class QueryObjectFactoryActivatorTest extends TestCase {

  public void testStart() throws Exception {
    TestableQueryObjectFactoryActivator activator = new TestableQueryObjectFactoryActivator();
    MockBundleContext context = new MockBundleContext();
    activator.start(context);
    assertNotNull(activator.logger);
    assertNotNull(activator.delegator);
    assertNotNull(context.getService(null));
  }

  public void testStop() throws Exception {
    TestableQueryObjectFactoryActivator activator = new TestableQueryObjectFactoryActivator();
    MockBundleContext context = new MockBundleContext();
    activator.start(context);
    activator.stop(context);
    assertNotNull(activator.logger);
    assertNull(activator.delegator);
  }

  public void testGetService() throws Exception {
    TestableQueryObjectFactoryActivator activator = new TestableQueryObjectFactoryActivator();
    MockBundleContext context = new MockBundleContext();
    MockBundle bundle = new MockBundle();
    activator.start(context);
    Object service = activator.getService(bundle, null);
    assertNotNull(service);
    assertTrue(service instanceof QueryObjectFactoryService);
    activator.ungetService(bundle, null, service);
    activator.stop(context);
    assertNotNull(activator.logger);
    assertNull(activator.delegator);
  }

  public void testUseServiceCreateQueryObject1() throws Exception {
    TestableQueryObjectFactoryActivator activator = new TestableQueryObjectFactoryActivator();
    MockBundleContext context = new MockBundleContext();
    MockBundle bundle = new MockBundle();
    activator.start(context);
    QueryObjectFactoryService service = (QueryObjectFactoryService) activator.getService(bundle, null);
    service.registerMapper("myclob", new ClobAdapter());
    Dao dao = service.createQueryObject(Dao.class);
    assertNotNull(dao);
    activator.ungetService(bundle, null, service);
    activator.stop(context);
    // because our mock service runs in the same class loader as junit we need to re-register common adapters
    CommonAdapterRegistrar.registerCommonAdapters();
    new CommonAdapterRegistrar();
  }
  
  public void testUseServiceCreateQueryObject2() throws Exception {
    TestableQueryObjectFactoryActivator activator = new TestableQueryObjectFactoryActivator();
    MockBundleContext context = new MockBundleContext();
    MockBundle bundle = new MockBundle();
    activator.start(context);
    QueryObjectFactoryService service = (QueryObjectFactoryService) activator.getService(bundle, null);
    AbstractDao1 dao = service.createQueryObject(AbstractDao1.class, "test");
    assertNotNull(dao);
    assertEquals("test", dao.argument);
    activator.ungetService(bundle, null, service);
    activator.stop(context);
  }
  
  public void testUseServiceCreateQueryObjectFromSuperClass1() throws Exception {
    TestableQueryObjectFactoryActivator activator = new TestableQueryObjectFactoryActivator();
    MockBundleContext context = new MockBundleContext();
    MockBundle bundle = new MockBundle();
    activator.start(context);
    QueryObjectFactoryService service = (QueryObjectFactoryService) activator.getService(bundle, null);
    DaoBase dao = service.createQueryObjectFromSuperClass(DaoBase.class, Base.class);
    assertNotNull(dao);
    assertEquals("base", ((Base) dao).base);
    activator.ungetService(bundle, null, service);
    activator.stop(context);
  }
  
  public void testUseServiceCreateQueryObjectFromSuperClass2() throws Exception {
    TestableQueryObjectFactoryActivator activator = new TestableQueryObjectFactoryActivator();
    MockBundleContext context = new MockBundleContext();
    MockBundle bundle = new MockBundle();
    activator.start(context);
    QueryObjectFactoryService service = (QueryObjectFactoryService) activator.getService(bundle, null);
    DaoBase dao = service.createQueryObjectFromSuperClass(DaoBase.class, Base.class, "argument");
    assertNotNull(dao);
    assertEquals("argument", ((Base) dao).base);
    activator.ungetService(bundle, null, service);
    activator.stop(context);
  }
  
  public void testUseServiceMapper2() throws Exception {
    TestableQueryObjectFactoryActivator activator = new TestableQueryObjectFactoryActivator();
    MockBundleContext context = new MockBundleContext();
    MockBundle bundle = new MockBundle();
    activator.start(context);
    QueryObjectFactoryService service = (QueryObjectFactoryService) activator.getService(bundle, null);
    assertFalse(service.isMapperRegistered("myclob"));
    service.registerMapper("myclob", new ClobAdapter());
    assertTrue(service.isMapperRegistered("myclob"));
    service.unregisterMapper("myclob");
    assertFalse(service.isMapperRegistered("myclob"));
    activator.ungetService(bundle, null, service);
    activator.stop(context);
  }
  
  public void testCustomizers() throws Exception {
    TestableQueryObjectFactoryActivator activator = new TestableQueryObjectFactoryActivator();
    MockBundleContext context = new MockBundleContext();
    MockBundle bundle = new MockBundle();
    activator.start(context);
    QueryObjectFactoryService service = (QueryObjectFactoryService) activator.getService(bundle, null);
    try {
      service.setCustomizer(new DefaultCustomizer());
      service.setDefaultCustomizer();
      service.setSQLDialect(new HSQLDbDialect());
    } catch (Exception e) {
      fail("failed");
    }
    activator.ungetService(bundle, null, service);
    activator.stop(context);
  }

  private class TestableQueryObjectFactoryActivator extends QueryObjectFactoryActivator { }
  
  private interface Dao extends BaseQuery {
    @Query(sql = "select clob {myclob%%} from test where s = {%1}")
    String test(String s) throws SQLException;
  }
  
  private static abstract class AbstractDao1 implements BaseQuery {
    public String argument;

    @SuppressWarnings("unused")
    public AbstractDao1(String argument) {
      this.argument = argument;
    }
    
    @Query(sql = "select s {%%} from test where s = {%1}")
    public abstract String test(String s) throws SQLException;
  }
  
  private interface DaoBase extends BaseQuery {
    @Query(sql = "select s {%%} from test where s = {%1}")
    String test(String s) throws SQLException;
  }

  public static class Base {
    public String base;
    public Base() {
      base = "base";
    }
    
    public Base(String argument) {
      base = argument;
    }
  }
}
