package uk.co.brunella.qof.session;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

public class RetrySessionRunnerTest {

    private DataSource createDataSource() {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:retrySessionRunnerTest");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Before
    public void setUp() throws SQLException {
        try (Statement stmt = createDataSource().getConnection().createStatement()) {
            try {
                stmt.execute("drop table test");
            } catch (Exception ignore) {
            }
            stmt.execute("create table test (id integer, name varchar(40))");
        }
        SessionContextFactory.removeContext();
        SessionContextFactory.removeContext("RetrySessionRunnerTest");
        SessionContextFactory.setDataSource(createDataSource());
        SessionContextFactory.setDataSource("RetrySessionRunnerTest", createDataSource());
    }

    @After
    public void tearDown() throws SQLException {
        try (Statement stmt = createDataSource().getConnection().createStatement()) {
            stmt.execute("drop table test");
        }
    }

    @Test
    public void testSuccessDefaultContext() throws SystemException, SQLException {
        new RetrySessionRunner<>((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
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

    @Test
    public void testSuccessOneRetryDefaultContext() throws SystemException, SQLException {
        new RetrySessionRunner<>(new TransactionRunnable<Void>() {
            int runs = 0;

            public Void run(Connection connection, Object... arguments) throws SQLException {
                if (runs++ == 0) {
                    throw new SQLException("retry");
                }
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("insert into test values (1, 'John')");
                    stmt.execute("insert into test values (2, 'John')");
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

    @Test
    public void testSuccessOneRetryNamedContext() throws SystemException, SQLException {
        new RetrySessionRunner<>(new TransactionRunnable<Void>() {
            int runs = 0;

            public Void run(Connection connection, Object... arguments) throws SQLException {
                if (runs++ == 0) {
                    throw new SQLException("retry");
                }
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("insert into test values (1, 'John')");
                    stmt.execute("insert into test values (2, 'John')");
                }
                return null;
            }

        }, "RetrySessionRunnerTest", 1).execute();
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        assertTrue(rs.next());
        assertEquals(2, rs.getInt(1));
        assertFalse(rs.next());
        connection.close();
    }

    @Test
    public void testFailedOneRetryDefaultContext() throws SQLException {
        try {
            new RetrySessionRunner<>((TransactionRunnable<Void>) (connection, arguments) -> {
                throw new SQLException("retry");
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

    @Test
    public void testSuccessOneRetryDelayDefaultContext() throws SystemException, SQLException {
        new RetrySessionRunner<>(new TransactionRunnable<Void>() {
            int runs = 0;

            public Void run(Connection connection, Object... arguments) throws SQLException {
                if (runs++ == 0) {
                    throw new SQLException("retry");
                }
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("insert into test values (1, 'John')");
                    stmt.execute("insert into test values (2, 'John')");
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

    @Test
    public void testSuccessOneRetryDelayNamedContext() throws SystemException, SQLException {
        new RetrySessionRunner<>(new TransactionRunnable<Void>() {
            int runs = 0;

            public Void run(Connection connection, Object... arguments) throws SQLException {
                if (runs++ == 0) {
                    throw new SQLException("retry");
                }
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("insert into test values (1, 'John')");
                    stmt.execute("insert into test values (2, 'John')");
                }
                return null;
            }

        }, "RetrySessionRunnerTest", 1, 100).execute();
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
