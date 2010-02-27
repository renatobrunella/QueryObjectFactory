package sf.qof.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.hsqldb.jdbc.jdbcDataSource;

public class DefaultSessionRunnerTest extends TestCase {

  private DataSource createDataSource() {
    jdbcDataSource ds = new jdbcDataSource();
    ds.setDatabase("jdbc:hsqldb:mem:aname");
    ds.setUser("sa");
    ds.setPassword("");
    return ds;
  }

  public void setUp() throws SQLException {
    Statement stmt = createDataSource().getConnection().createStatement();
    try {
      stmt.execute("create table test (id integer, name varchar(40))");
    } finally {
      stmt.close();
    }
    SessionContextFactory.setDataSource(createDataSource());
    SessionContextFactory.setDataSource("TEST", createDataSource());
  }

  public void tearDown() throws SQLException {
    Statement stmt = createDataSource().getConnection().createStatement();
    try {
      stmt.execute("drop table test");
    } finally {
      stmt.close();
    }
  }
  
  public void testDefaultContext() throws SystemException, SQLException {
    new DefaultSessionRunner<Void>(new TransactionRunnable<Void>() {
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
    }).execute();
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
  
  public void testNamedContext() throws SystemException, SQLException {
    new DefaultSessionRunner<Void>(new TransactionRunnable<Void>() {
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
    }, "TEST").execute();
    Connection connection = createDataSource().getConnection();
    ResultSet rs = connection.createStatement().executeQuery("select * from test");
    assertTrue(rs.next());
    assertEquals(1, rs.getInt(1));
    assertTrue(rs.next());
    assertEquals(2, rs.getInt(1));
    assertFalse(rs.next());
    connection.close();
  }
}
