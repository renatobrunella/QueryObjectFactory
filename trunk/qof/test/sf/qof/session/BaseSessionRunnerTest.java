package sf.qof.session;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.TestCase;
import sf.qof.testtools.MockConnectionData;
import sf.qof.testtools.MockConnectionFactory;

public class BaseSessionRunnerTest extends TestCase {

  List<String> log;
  
  private class MockDataSource implements DataSource {

    public Connection getConnection() throws SQLException {
      Connection connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData)connection).getLog();
      return connection;
    }

    public Connection getConnection(String username, String password) throws SQLException {
      return null;
    }

    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    public void setLoginTimeout(int seconds) throws SQLException {
    }

    @SuppressWarnings("unused")
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
    }

    @SuppressWarnings("unused")
  public <T> T unwrap(Class<T> iface) throws SQLException {
      return null;
    }
    
  }
  
  public void testSuccessDefaultContext() throws SystemException {
    SessionContextFactory.setDataSource(new MockDataSource());
    String result = new BaseSessionRunner<String>() {
      @Override
      protected String run(Connection connection, Object... arguments) throws SQLException {
        assertEquals(1, arguments.length);
        return "TEST1";
      }}.execute("TEST1");
    assertEquals("TEST1", result);
    assertEquals(3, log.size());
    assertEquals("setAutoCommit(false)", log.get(0));
    assertEquals("commit()", log.get(1));
    assertEquals("close()", log.get(2));
  }
  
  public void testSuccessNamedContext() throws SystemException {
    SessionContextFactory.setDataSource("MY_CONTEXT", new MockDataSource());
    String result = new BaseSessionRunner<String>("MY_CONTEXT") {
      @Override
      protected String run(Connection connection, Object... arguments) throws SQLException {
        assertEquals(1, arguments.length);
        return "TEST2";
      }}.execute("TEST2");
    assertEquals("TEST2", result);
    assertEquals(3, log.size());
    assertEquals("setAutoCommit(false)", log.get(0));
    assertEquals("commit()", log.get(1));
    assertEquals("close()", log.get(2));
  }
  
  public void testFailureDefaultContext() throws SystemException {
    SessionContextFactory.setDataSource(new MockDataSource());
    try {
      new BaseSessionRunner<String>() {
        @Override
        protected String run(Connection connection, Object... arguments)
            throws SQLException {
          throw new SQLException("failed");
        }
      }.execute();
      fail("exception expected");
    } catch (SystemException s) {
      assertEquals("failed", s.getCause().getMessage());
    }
    assertEquals(3, log.size());
    assertEquals("setAutoCommit(false)", log.get(0));
    assertEquals("rollback()", log.get(1));
    assertEquals("close()", log.get(2));
  }
  
  public void testForcedRollbackDefaultContext() throws SystemException {
    new SessionContextFactory();
    SessionContextFactory.setDataSource(new MockDataSource());
    new BaseSessionRunner<Void>() {
      @Override
      protected Void run(Connection connection, Object... arguments)
          throws SQLException {
        try {
          SessionContextFactory.getContext().getUserTransaction()
              .setRollbackOnly();
        } catch (SystemException e) {
        }
        return null;
      }
    }.execute();
    assertEquals(3, log.size());
    assertEquals("setAutoCommit(false)", log.get(0));
    assertEquals("rollback()", log.get(1));
    assertEquals("close()", log.get(2));
  }
}
