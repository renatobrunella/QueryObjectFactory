package uk.co.brunella.qof.session;

import junit.framework.TestCase;
import org.hsqldb.jdbc.JDBCDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RetrySessionRunnerTest extends TestCase {

    private DataSource createDataSource() {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:aname");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    public void setUp() throws SQLException {
        Statement stmt = createDataSource().getConnection().createStatement();
        try {
            try {
                stmt.execute("drop table test");
            } catch (Exception ignore) {}
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

    public void testSuccessDefaultContext() throws SystemException, SQLException {
        new RetrySessionRunner<Void>(new TransactionRunnable<Void>() {
            public Void run(Connection connection, Object... arguments) throws SQLException {
                Statement stmt = connection.createStatement();
                try {
                    stmt.execute("insert into test values (1, 'John')");
                    stmt.execute("insert into test values (2, 'John')");
                } finally {
                    stmt.close();
                }
                return null;
            }

        }, 1).execute();
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        assertFalse(rs.next());
        connection.close();
    }

    public void testSuccessOneRetryDefaultContext() throws SystemException, SQLException {
        new RetrySessionRunner<Void>(new TransactionRunnable<Void>() {
            int runs = 0;

            public Void run(Connection connection, Object... arguments) throws SQLException {
                if (runs++ == 0) {
                    throw new SQLException("retry");
                }
                Statement stmt = connection.createStatement();
                try {
                    stmt.execute("insert into test values (1, 'John')");
                    stmt.execute("insert into test values (2, 'John')");
                } finally {
                    stmt.close();
                }
                return null;
            }

        }, 1).execute();
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        assertFalse(rs.next());
        connection.close();
    }

    public void testSuccessOneRetryNamedContext() throws SystemException, SQLException {
        new RetrySessionRunner<Void>(new TransactionRunnable<Void>() {
            int runs = 0;

            public Void run(Connection connection, Object... arguments) throws SQLException {
                if (runs++ == 0) {
                    throw new SQLException("retry");
                }
                Statement stmt = connection.createStatement();
                try {
                    stmt.execute("insert into test values (1, 'John')");
                    stmt.execute("insert into test values (2, 'John')");
                } finally {
                    stmt.close();
                }
                return null;
            }

        }, "TEST", 1).execute();
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        assertFalse(rs.next());
        connection.close();
    }

    public void testFailedOneRetryDefaultContext() throws SystemException, SQLException {
        try {
            new RetrySessionRunner<Void>(new TransactionRunnable<Void>() {
                public Void run(Connection connection, Object... arguments) throws SQLException {
                    throw new SQLException("retry");
                }

            }, 1).execute();
            fail("exception expected");
        } catch (SystemException e) {
            assertEquals("retry", e.getCause().getMessage());
        }
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    public void testSuccessOneRetryDelayDefaultContext() throws SystemException, SQLException {
        new RetrySessionRunner<Void>(new TransactionRunnable<Void>() {
            int runs = 0;

            public Void run(Connection connection, Object... arguments) throws SQLException {
                if (runs++ == 0) {
                    throw new SQLException("retry");
                }
                Statement stmt = connection.createStatement();
                try {
                    stmt.execute("insert into test values (1, 'John')");
                    stmt.execute("insert into test values (2, 'John')");
                } finally {
                    stmt.close();
                }
                return null;
            }

        }, 1, 100).execute();
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        assertFalse(rs.next());
        connection.close();
    }

    public void testSuccessOneRetryDelayNamedContext() throws SystemException, SQLException {
        new RetrySessionRunner<Void>(new TransactionRunnable<Void>() {
            int runs = 0;

            public Void run(Connection connection, Object... arguments) throws SQLException {
                if (runs++ == 0) {
                    throw new SQLException("retry");
                }
                Statement stmt = connection.createStatement();
                try {
                    stmt.execute("insert into test values (1, 'John')");
                    stmt.execute("insert into test values (2, 'John')");
                } finally {
                    stmt.close();
                }
                return null;
            }

        }, "TEST", 1, 100).execute();
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
