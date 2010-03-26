package sf.qof.session;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.hsqldb.jdbc.jdbcDataSource;

public class DefaultSessionRunnerTest extends TestCase {

  private DataSource createDataSource() {
    jdbcDataSource ds = new jdbcDataSource();
    ds.setDatabase("jdbc:hsqldb:mem:aname");
    ds.setUser("sa");
    ds.setPassword("");
    return new DataSourceWrapper(ds);
  }

  public void setUp() throws Exception {
    MockInitialContextFactory.register();
    MockContext.getInstance().bind("datasource", createDataSource());
    Statement stmt = createDataSource().getConnection().createStatement();
    try {
      stmt.execute("create table test (id integer, name varchar(40))");
    } finally {
      stmt.close();
    }
  }

  public void tearDown() throws Exception {
    MockContext.getInstance().unbind("datasource");
    Statement stmt = createDataSource().getConnection().createStatement();
    try {
      stmt.execute("drop table test");
    } finally {
      stmt.close();
    }
  }
  
  public void testDefaultContext() throws SystemException, SQLException {
    SessionContextFactory.removeContext();
    SessionContextFactory.setDataSource(createDataSource());
    DefaultSessionRunner.execute(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    });
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testDefaultContextBeanManaged() throws SystemException, SQLException, NamingException {
    SessionContextFactory.removeContext();
    SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
    DefaultSessionRunner.executeBeanManaged(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    });
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testDefaultContextContainerManaged() throws SystemException, SQLException, NamingException {
    SessionContextFactory.removeContext();
    SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
    DefaultSessionRunner.executeContainerManaged(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    });
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testDefaultContextPolicy() throws SystemException, SQLException {
    SessionContextFactory.removeContext();
    SessionContextFactory.setDataSource(createDataSource());
    DefaultSessionRunner.execute(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    }, SessionPolicy.CAN_JOIN_EXISTING_SESSION);
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testDefaultContextBeanManagedPolicy() throws SystemException, SQLException, NamingException {
    SessionContextFactory.removeContext();
    SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
    DefaultSessionRunner.executeBeanManaged(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    }, SessionPolicy.CAN_JOIN_EXISTING_SESSION);
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testDefaultContextContainerManagedPolicy() throws SystemException, SQLException, NamingException {
    SessionContextFactory.removeContext();
    SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
    DefaultSessionRunner.executeContainerManaged(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    }, SessionPolicy.CAN_JOIN_EXISTING_SESSION);
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testNamedContext() throws SystemException, SQLException {
    SessionContextFactory.removeContext("CONTEXT");
    SessionContextFactory.setDataSource("CONTEXT", createDataSource());
    DefaultSessionRunner.execute(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    }, "CONTEXT");
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testNamedContextBeanManaged() throws SystemException, SQLException, NamingException {
    SessionContextFactory.removeContext("CONTEXT");
    SessionContextFactory.setJndiDataSource("CONTEXT", "datasource", null, TransactionManagementType.BEAN);
    DefaultSessionRunner.executeBeanManaged(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    }, "CONTEXT");
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testNamedContextContainerManaged() throws SystemException, SQLException, NamingException {
    SessionContextFactory.removeContext("CONTEXT");
    SessionContextFactory.setJndiDataSource("CONTEXT", "datasource", null, TransactionManagementType.BEAN);
    DefaultSessionRunner.executeContainerManaged(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    }, "CONTEXT");
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertFalse(rs.next());
    connection.close();
  }

  public void testNamedContextPolicy() throws SystemException, SQLException {
    SessionContextFactory.removeContext("CONTEXT");
    SessionContextFactory.setDataSource("CONTEXT", createDataSource());
    DefaultSessionRunner.execute(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    }, "CONTEXT", SessionPolicy.CAN_JOIN_EXISTING_SESSION);
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testNamedContextBeanManagedPolicy() throws SystemException, SQLException, NamingException {
    SessionContextFactory.removeContext("CONTEXT");
    SessionContextFactory.setJndiDataSource("CONTEXT", "datasource", null, TransactionManagementType.BEAN);
    DefaultSessionRunner.executeBeanManaged(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    }, "CONTEXT", SessionPolicy.CAN_JOIN_EXISTING_SESSION);
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testNamedContextContainerManagedPolicy() throws SystemException, SQLException, NamingException {
    SessionContextFactory.removeContext("CONTEXT");
    SessionContextFactory.setJndiDataSource("CONTEXT", "datasource", null, TransactionManagementType.BEAN);
    DefaultSessionRunner.executeContainerManaged(new TransactionRunnable<Void>() {
      public Void run(Connection connection, Object... arguments) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
          stmt.execute("insert into test values (1, 'John')");
          stmt.execute("insert into test values (2, 'John')");
        } finally {
          stmt.close();
        }
        return null;
      };
    }, "CONTEXT", SessionPolicy.CAN_JOIN_EXISTING_SESSION);
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertFalse(rs.next());
    connection.close();
  }

  public static class DataSourceWrapper implements DataSource {
    DataSource dataSource;
    
    public Connection getConnection() throws SQLException {
      Connection connection = dataSource.getConnection();
      connection.setAutoCommit(false);
      return connection;
    }

    public Connection getConnection(String username, String password) throws SQLException {
      Connection connection = dataSource.getConnection(username, password);
      connection.setAutoCommit(false);
      return connection;
    }

    public int getLoginTimeout() throws SQLException {
      return dataSource.getLoginTimeout();
    }

    public PrintWriter getLogWriter() throws SQLException {
      return dataSource.getLogWriter();
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return dataSource.isWrapperFor(iface);
    }

    public void setLoginTimeout(int seconds) throws SQLException {
      dataSource.setLoginTimeout(seconds);
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
      dataSource.setLogWriter(out);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
      return dataSource.unwrap(iface);
    }

    public DataSourceWrapper(DataSource dataSource) {
      this.dataSource = dataSource;
    }
  }
}
