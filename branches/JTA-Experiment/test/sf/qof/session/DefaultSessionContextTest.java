package sf.qof.session;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.hsqldb.jdbc.jdbcDataSource;

import sf.qof.testtools.LoggingDelegationProxy;
import sf.qof.testtools.MockConnectionFactory;

public class DefaultSessionContextTest extends TestCase {

  private DataSource createDataSource() {
    jdbcDataSource ds = new jdbcDataSource();
    ds.setDatabase("jdbc:hsqldb:mem:aname");
    ds.setUser("sa");
    ds.setPassword("");
    return ds;
  }

  public void testGetConnection() throws SystemException {
    SessionContextFactory.setDataSource(createDataSource());
    SessionContext ctx = SessionContextFactory.getContext(); 
    try {
      assertNull(ctx.getConnection());
      fail("Should throw exception");
    } catch (RuntimeException e) {
      assertEquals("Session is not running in thread for context DEFAULT_CONTEXT", e.getMessage());
    }
    ctx.startSession();
    assertNotNull(ctx.getConnection());
    ctx.stopSession();
    try {
      assertNull(ctx.getConnection());
      fail("Should throw exception");
    } catch (RuntimeException e) {
      assertEquals("Session is not running in thread for context DEFAULT_CONTEXT", e.getMessage());
    }
  }

  public void testGetConnectionTwoContexts() throws SystemException {
    SessionContextFactory.setDataSource("A", createDataSource());
    SessionContext ctxA = SessionContextFactory.getContext("A"); 
    SessionContextFactory.setDataSource("B", createDataSource());
    SessionContext ctxB = SessionContextFactory.getContext("B"); 
    ctxA.startSession();
    ctxB.startSession();
    Connection connectionA = ctxA.getConnection();
    assertNotNull(connectionA);
    Connection connectionB = ctxB.getConnection();
    assertNotNull(connectionB);
    assertFalse(connectionA == connectionB);
    ctxA.stopSession();
    ctxB.stopSession();
  }

  public void testConnectionClosed() throws SQLException, SystemException {
    SessionContextFactory.setDataSource("xyz", createDataSource());
    SessionContext ctx = SessionContextFactory.getContext("xyz"); 
    ctx.startSession();
    Connection connection = ctx.getConnection();
    assertNotNull(connection);
    assertFalse(connection.isClosed());
    ctx.stopSession();
    assertTrue(connection.isClosed());
  }
  
  public void testFailsNoDataSource() throws SystemException {
    try {
      SessionContextFactory.setDataSource(null);
      SessionContextFactory.getContext().startSession();
      fail("Should throw exception");
    } catch (SystemException e) {
      assertEquals("No data source defined for context DEFAULT_CONTEXT", e.getMessage());
    }
    try {
      SessionContextFactory.getContext("My context").startSession();
      fail("Should throw exception");
    } catch (SystemException e) {
      assertEquals("No data source defined for context My context", e.getMessage());
    }
  }

  private Connection c1;
  private Connection c2;
  
  public void testGetConnectionTwoThreads() throws SystemException {
    SessionContextFactory.setDataSource("testGetConnectionTwoThreads", createDataSource());
    
    Runnable r1 = new Runnable() {
      public void run() {
        SessionContext ctx = SessionContextFactory.getContext("testGetConnectionTwoThreads");
        try {
          ctx.startSession();
          c1 = ctx.getConnection();
          ctx.stopSession();
        } catch (SystemException e) {
        }
      }
    };
    
    Runnable r2 = new Runnable() {
      public void run() {
        SessionContext ctx = SessionContextFactory.getContext("testGetConnectionTwoThreads");
        try {
          ctx.startSession();
          c2 = ctx.getConnection();
          ctx.stopSession();
        } catch (SystemException e) {
        }
      }
    };
    
    Thread t1 = new Thread(r1);
    t1.run();
    Thread t2 = new Thread(r2);
    t2.run();
    try {
      t1.join();
      t2.join();
    } catch (InterruptedException e) {
    }
    
    assertNotNull(c1);
    assertNotNull(c2);
    assertFalse("The two connection must not be the same", c1 == c2);
  }
  
  public void testGetConnectionThreadsRunTwice() {
    SessionContextFactory.setDataSource("testGetConnectionThreadsRunTwice", createDataSource());
    
    Runnable r1 = new Runnable() {
      boolean firstRun = true;
      public void run() {
        doIt();
        c2 = c1;
        c1 = null;
        doIt();
      }

      public void doIt() {
        SessionContext ctx = SessionContextFactory.getContext("testGetConnectionThreadsRunTwice");
        if (firstRun) {
          try {
            ctx.startSession();
          } catch (SystemException e) {
          }
        }
        c1 = ctx.getConnection();
        if (!firstRun) {
          try {
            ctx.stopSession();
          } catch (SystemException e) {
          }
        }
        firstRun = false;
      }
    };
    
    Thread t1 = new Thread(r1);
    t1.start();
    try {
      t1.join();
    } catch (InterruptedException e) {
    }

    assertNotNull(c1);
    assertTrue("The two connection must be the same", c1 == c2);
  }
  
  private SessionContext ctx;
  private boolean passed;
  
  public void testReusingInvalidContext() throws SystemException {
    SessionContextFactory.setDataSource("testReusingInvalidContext", createDataSource());
    
    ctx = SessionContextFactory.getContext("testReusingInvalidContext");
    ctx.startSession();
    ctx.getConnection();
    
    Runnable r = new Runnable() {
      public void run() {
        try {
          ctx.getConnection();
          fail("Should raise exception");
          passed = false;
        } catch (RuntimeException e) {
          assertEquals("Session is not running in thread for context testReusingInvalidContext", e.getMessage());
          passed = true;
        }
      }
    };
    
    Thread t = new Thread(r);
    t.start();
    try {
      t.join();
    } catch (InterruptedException e) {
    }

    ctx.stopSession();
    assertTrue(passed);
  }

  public void testStartingTwice() throws SystemException {
    SessionContextFactory.setDataSource("testStartingTwice", createDataSource());
    SessionContext ctx = SessionContextFactory.getContext("testStartingTwice");
    
    ctx.startSession();
    try {
      ctx.startSession();
      fail("Should raise exception");
    } catch (IllegalStateException e) {
      assertEquals("Session already running in thread for context testStartingTwice", e.getMessage());
    }
    ctx.stopSession();
  }

  public void testStoppedWithoutStarted() throws SystemException {
    SessionContextFactory.setDataSource("testStoppedWithoutStarted", createDataSource());
    SessionContext ctx = SessionContextFactory.getContext("testStoppedWithoutStarted");
    
    try {
      ctx.stopSession();
      fail("Should raise exception");
    } catch (IllegalStateException e) {
      assertEquals("Session is not running in thread for context testStoppedWithoutStarted", e.getMessage());
    }
  }

  public void testGetUserTransaction() throws SystemException {
    SessionContextFactory.setDataSource("testGetUserTransaction", createDataSource());
    SessionContext ctx = SessionContextFactory.getContext("testGetUserTransaction");
    try {
      ctx.getUserTransaction();
      fail("Should raise exception");
    } catch (RuntimeException e) {
      assertEquals("Session is not running in thread for context testGetUserTransaction", e.getMessage());
    }
    ctx.startSession();
    assertNotNull(ctx.getUserTransaction());
    ctx.stopSession();
  }

  public void testIsRollbackOnly() throws SystemException {
    SessionContextFactory.setDataSource("testIsRollbackOnly", createDataSource());
    SessionContext ctx = SessionContextFactory.getContext("testIsRollbackOnly");
    try {
      ctx.getUserTransaction().isRollbackOnly();
      fail("Should raise exception");
    } catch (IllegalStateException e) {
      assertEquals("Session is not running in thread for context testIsRollbackOnly", e.getMessage());
    }
    ctx.startSession();
    try {
      ctx.getUserTransaction().isRollbackOnly();
      fail("Should raise exception");
    } catch (IllegalStateException e) {
      assertEquals("Invalid state: Transaction is NEW", e.getMessage());
    }
    ctx.getUserTransaction().begin();
        assertFalse(ctx.getUserTransaction().isRollbackOnly());
    ctx.getUserTransaction().setRollbackOnly();
    assertTrue(ctx.getUserTransaction().isRollbackOnly());
    ctx.stopSession();
  }
  
  public void testSetSessionConnectionHandler() throws SystemException {
    final boolean[] called = new boolean[2];
    SessionConnectionHandler handler = new SessionConnectionHandler() {
      public void closeConnection(Connection connection) throws SystemException {
        try {
          called[0] = true;
          connection.close();
        } catch (SQLException e) {
          throw new SystemException(e);
        }
      }
      public Connection getConnection(DataSource dataSource) throws SystemException {
        try {
          called[1] = true;
          return dataSource.getConnection();
        } catch (SQLException e) {
          throw new SystemException(e);
        }
      }
    };
    SessionContextFactory.setDataSource(createDataSource());
    SessionContextFactory.setSessionConnectionHandler(handler);
    SessionContext ctx = SessionContextFactory.getContext();
    assertFalse(called[0]);
    assertFalse(called[1]);
    ctx.startSession();
    assertFalse(called[0]);
    assertTrue(called[1]);
    assertNotNull(ctx.getConnection());
    ctx.stopSession();
    assertTrue(called[0]);
    assertTrue(called[1]);
    // reset to default handler
    SessionContextFactory.setSessionConnectionHandler(null);
  }
  
  public void testSetSessionConnectionHandlerContext() throws SystemException {
    final boolean[] called = new boolean[2];
    SessionConnectionHandler handler = new SessionConnectionHandler() {
      public void closeConnection(Connection connection) throws SystemException {
        try {
          called[0] = true;
          connection.close();
        } catch (SQLException e) {
          throw new SystemException(e);
        }
      }
      public Connection getConnection(DataSource dataSource) throws SystemException {
        try {
          called[1] = true;
          return dataSource.getConnection();
        } catch (SQLException e) {
          throw new SystemException(e);
        }
      }
    };
    SessionContextFactory.setDataSource("testSetSessionConnectionHandlerContext", createDataSource());
    SessionContextFactory.setSessionConnectionHandler("testSetSessionConnectionHandlerContext", handler);
    SessionContext ctx = SessionContextFactory.getContext("testSetSessionConnectionHandlerContext");
    assertFalse(called[0]);
    assertFalse(called[1]);
    ctx.startSession();
    assertFalse(called[0]);
    assertTrue(called[1]);
    assertNotNull(ctx.getConnection());
    ctx.stopSession();
    assertTrue(called[0]);
    assertTrue(called[1]);
  }
  
  public void testSetAutoCommitPolicyFalse() throws SystemException {
    MockDataSource dataSource = new MockDataSource();
    SessionContextFactory.setDataSource("testSetAutoCommitPolicyFalse", dataSource);
    SessionContextFactory.setAutoCommitPolicy("testSetAutoCommitPolicyFalse", false);
    SessionContext ctx = SessionContextFactory.getContext("testSetAutoCommitPolicyFalse");
    List<String> log = ((LoggingDelegationProxy) dataSource.connection).getLog();
    assertEquals(0, log.size());
    ctx.startSession();
    assertEquals(0, log.size());
    ctx.stopSession();
    assertEquals(1, log.size());
    assertEquals("close()", log.get(0));
  }
  
  public void testSetAutoCommitPolicyTrue() throws SystemException {
    MockDataSource dataSource = new MockDataSource();
    SessionContextFactory.setDataSource("testSetAutoCommitPolicyTrue", dataSource);
    SessionContextFactory.setAutoCommitPolicy("testSetAutoCommitPolicyTrue", true);
    SessionContext ctx = SessionContextFactory.getContext("testSetAutoCommitPolicyTrue");
    List<String> log = ((LoggingDelegationProxy) dataSource.connection).getLog();
    assertEquals(0, log.size());
    ctx.startSession();
    assertEquals(1, log.size());
    assertEquals("setAutoCommit(false)", log.get(0));
    ctx.stopSession();
    assertEquals(2, log.size());
    assertEquals("close()", log.get(1));
  }
  
  private static class MockDataSource implements DataSource {

    public Connection connection;
    
    public MockDataSource() {
      connection = MockConnectionFactory.getConnection();
    }
    
    public Connection getConnection() throws SQLException {
      return (Connection) connection;
    }

    public Connection getConnection(String username, String password)
        throws SQLException {
      return null;
    }

    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    public void setLogWriter(PrintWriter arg0) throws SQLException {
    }

    public void setLoginTimeout(int arg0) throws SQLException {
    }

    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
      return false;
    }

    public <T> T unwrap(Class<T> arg0) throws SQLException {
      return null;
    }
    
  }
}
