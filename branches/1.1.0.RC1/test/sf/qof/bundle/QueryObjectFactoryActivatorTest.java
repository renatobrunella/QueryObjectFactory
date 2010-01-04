package sf.qof.bundle;

import java.sql.SQLException;

import sf.qof.BaseQuery;
import sf.qof.Query;
import sf.qof.QueryObjectFactoryService;
import sf.qof.adapter.ClobAdapter;
import sf.qof.adapter.CommonAdapterRegistrar;
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

  public void testUseService() throws Exception {
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
  }

  private class TestableQueryObjectFactoryActivator extends QueryObjectFactoryActivator { }
  
  private interface Dao extends BaseQuery {
    @Query(sql = "select clob {myclob%%} from test where s = {%1}")
    String test(String s) throws SQLException;
  }
}
