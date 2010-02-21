package sf.qof.bundle;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import junit.framework.TestCase;

public class LoggerTest extends TestCase {

  public void testConstructor() {
    TestableLogger logger = new TestableLogger(new MockBundleContext());
    assertNotNull(logger.tracker);
  }

  public void testLoggingAvailable() {
    MockBundleContext context = new MockBundleContext();
    TestableLogger logger = new TestableLogger(context);
    MockServiceTracker serviceTracker = new MockServiceTracker(context, LogService.class.getName(), null, true);
    logger.tracker = serviceTracker;
    logger.open();
    logger.log(Logger.LOG_DEBUG, "test");
    assertEquals(Logger.LOG_DEBUG, serviceTracker._level);
    assertEquals("test", serviceTracker._message);
    Throwable t = new RuntimeException();
    logger.log(Logger.LOG_ERROR, "error", t);
    assertEquals(Logger.LOG_ERROR, serviceTracker._level);
    assertEquals("error", serviceTracker._message);
    assertEquals(t, serviceTracker._exception);
    logger.close();
    assertTrue(serviceTracker.openCalled);
    assertTrue(serviceTracker.closeCalled);
  }

  public void testLoggingNotAvailable() {
    MockBundleContext context = new MockBundleContext();
    TestableLogger logger = new TestableLogger(context);
    MockServiceTracker serviceTracker = new MockServiceTracker(context, LogService.class.getName(), null, false);
    logger.tracker = serviceTracker;
    logger.open();
    logger.log(Logger.LOG_DEBUG, "test");
    assertEquals(-999, serviceTracker._level);
    assertEquals(null, serviceTracker._message);
    Throwable t = new RuntimeException();
    logger.log(Logger.LOG_ERROR, "error", t);
    assertEquals(-999, serviceTracker._level);
    assertEquals(null, serviceTracker._message);
    assertEquals(null, serviceTracker._exception);
    logger.close();
    assertTrue(serviceTracker.openCalled);
    assertTrue(serviceTracker.closeCalled);
  }
  
  private class TestableLogger extends LoggerImpl {

    public TestableLogger(BundleContext context) {
      super(context);
    }
    
  }
  
  private class MockServiceTracker extends ServiceTracker {

    private boolean available;
    private boolean openCalled = false;
    private boolean closeCalled = false;
    private int _level = -999;
    private String _message;
    private Throwable _exception;

    public MockServiceTracker(BundleContext context, String clazz, ServiceTrackerCustomizer customizer, boolean available) {
      super(context, clazz, customizer);
      this.available = available;
    }
    
    public Object getService() {
      if (available) {
        return new LogService() {

          public void log(int level, String message) {
            _level = level;
            _message = message;
          }

          public void log(int level, String message, Throwable exception) {
            _level = level;
            _message = message;
            _exception = exception;
          }

          public void log(ServiceReference sr, int level, String message) {
          }

          public void log(ServiceReference sr, int level, String message, Throwable exception) {
          }
          
        };
      } else {
        return null;
      }
    }
    
    public void open() {
      openCalled = true;
    }
    
    public void close() {
      closeCalled = true;
    }
  }
}
