package sf.qof.session;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hsqldb.jdbc.jdbcDataSource;

import sf.qof.session.SessionContext;
import sf.qof.session.SessionContextFactory;
import sf.qof.session.SystemException;

import junit.framework.TestCase;

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
    SessionContextFactory.setDataSource("testGetUserTransaction", createDataSource());
    SessionContext ctx = SessionContextFactory.getContext("testGetUserTransaction");
    try {
      ctx.getUserTransaction().isRollbackOnly();
      fail("Should raise exception");
    } catch (IllegalStateException e) {
      assertEquals("Session is not running in thread for context testGetUserTransaction", e.getMessage());
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
}