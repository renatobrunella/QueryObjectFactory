package uk.co.brunella.qof.session;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class DefaultSessionRunnerTest {

    private DataSource createDataSource() {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:defaultSessionRunnerTest");
        ds.setUser("sa");
        ds.setPassword("");
        return new DataSourceWrapper(ds);
    }

    @Before
    public void setUp() throws Exception {
        MockInitialContextFactory.register();
        MockContext.getInstance().bind("datasource", createDataSource());
        try (Statement stmt = createDataSource().getConnection().createStatement()) {
            try {
                stmt.execute("drop table test");
            } catch (Exception ignore) {
            }
            stmt.execute("create table test (id integer, name varchar(40))");
        }
    }

    @After
    public void tearDown() throws Exception {
        MockContext.getInstance().unbind("datasource");
        try (Statement stmt = createDataSource().getConnection().createStatement()) {
            stmt.execute("drop table test");
        }
    }

    @Test
    public void testDefaultContext() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(createDataSource());
        DefaultSessionRunner.execute((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
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

    @Test
    public void testDefaultContextBeanManaged() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.executeBeanManaged((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
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

    @Test
    public void testDefaultContextContainerManaged() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.executeContainerManaged((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
        });
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    @Test
    public void testDefaultContextPolicy() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(createDataSource());
        DefaultSessionRunner.execute((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
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

    @Test
    public void testDefaultContextBeanManagedPolicy() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.executeBeanManaged((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
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

    @Test
    public void testDefaultContextContainerManagedPolicy() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.executeContainerManaged((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
        }, SessionPolicy.CAN_JOIN_EXISTING_SESSION);
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    @Test
    public void testNamedContext() throws SystemException, SQLException {
        SessionContextFactory.removeContext("CONTEXT");
        SessionContextFactory.setDataSource("CONTEXT", createDataSource());
        DefaultSessionRunner.execute((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
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

    @Test
    public void testNamedContextBeanManaged() throws SystemException, SQLException {
        SessionContextFactory.removeContext("CONTEXT");
        SessionContextFactory.setJndiDataSource("CONTEXT", "datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.executeBeanManaged((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
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

    @Test
    public void testNamedContextContainerManaged() throws SystemException, SQLException {
        SessionContextFactory.removeContext("CONTEXT");
        SessionContextFactory.setJndiDataSource("CONTEXT", "datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.executeContainerManaged((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
        }, "CONTEXT");
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    @Test
    public void testNamedContextPolicy() throws SystemException, SQLException {
        SessionContextFactory.removeContext("CONTEXT");
        SessionContextFactory.setDataSource("CONTEXT", createDataSource());
        DefaultSessionRunner.execute((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
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

    @Test
    public void testNamedContextBeanManagedPolicy() throws SystemException, SQLException {
        SessionContextFactory.removeContext("CONTEXT");
        SessionContextFactory.setJndiDataSource("CONTEXT", "datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.executeBeanManaged((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
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

    @Test
    public void testNamedContextContainerManagedPolicy() throws SystemException, SQLException {
        SessionContextFactory.removeContext("CONTEXT");
        SessionContextFactory.setJndiDataSource("CONTEXT", "datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.executeContainerManaged((TransactionRunnable<Void>) (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("insert into test values (1, 'John')");
                stmt.execute("insert into test values (2, 'John')");
            }
            return null;
        }, "CONTEXT", SessionPolicy.CAN_JOIN_EXISTING_SESSION);
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    public static class DataSourceWrapper implements DataSource {
        DataSource dataSource;

        DataSourceWrapper(DataSource dataSource) {
            this.dataSource = dataSource;
        }

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

        public void setLoginTimeout(int seconds) throws SQLException {
            dataSource.setLoginTimeout(seconds);
        }

        public Logger getParentLogger() {
            return null;
        }

        public PrintWriter getLogWriter() throws SQLException {
            return dataSource.getLogWriter();
        }

        public void setLogWriter(PrintWriter out) throws SQLException {
            dataSource.setLogWriter(out);
        }

        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        public <T> T unwrap(Class<T> iface) {
            return null;
        }
    }
}
