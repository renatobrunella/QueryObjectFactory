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

public class DefaultSessionRunnerSessionPolicyTest {

    private DataSource createDataSource() {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:aname");
        ds.setUser("sa");
        ds.setPassword("");
        return new DataSourceWrapper(ds);
    }

    @Before
    public void setUp() throws Exception {
        MockInitialContextFactory.register();
        MockContext.getInstance().bind("datasource", createDataSource());
        try (Statement stmt = createDataSource().getConnection().createStatement()) {
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
    public void testCanJoinJoins() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(createDataSource());
        DefaultSessionRunner.execute(
                createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.CAN_JOIN_EXISTING_SESSION),
                SessionPolicy.MUST_START_NEW_SESSION);
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
    public void testCanJoinCreatesNew() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(createDataSource());
        DefaultSessionRunner.execute(
                createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.MUST_JOIN_EXISTING_SESSION),
                SessionPolicy.CAN_JOIN_EXISTING_SESSION);
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
    public void testMustJoin() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(createDataSource());
        DefaultSessionRunner.execute(
                createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.MUST_JOIN_EXISTING_SESSION),
                SessionPolicy.MUST_START_NEW_SESSION);
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
    public void testMustJoinFails() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(createDataSource());
        try {
            DefaultSessionRunner.execute(
                    createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.MUST_JOIN_EXISTING_SESSION),
                    SessionPolicy.MUST_JOIN_EXISTING_SESSION);
            fail("must throw exception");
        } catch (IllegalStateException e) {
            assertEquals("Session is not running in thread for context DEFAULT_CONTEXT and session policy requires to join session", e.getMessage());
        }
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    @Test
    public void testMustStartFails() throws SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(createDataSource());
        try {
            DefaultSessionRunner.execute(
                    createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.MUST_START_NEW_SESSION),
                    SessionPolicy.MUST_START_NEW_SESSION);
            fail("must throw exception");
        } catch (SystemException e) {
            assertEquals("java.lang.IllegalStateException: Session already running in thread for context DEFAULT_CONTEXT and session policy requires to start new session", e.getMessage());
        }
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    @Test
    public void testCanJoinJoinsBeanJndi() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.execute(
                createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.CAN_JOIN_EXISTING_SESSION),
                SessionPolicy.MUST_START_NEW_SESSION);
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
    public void testCanJoinCreatesNewBeanJndi() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.execute(
                createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.MUST_JOIN_EXISTING_SESSION),
                SessionPolicy.CAN_JOIN_EXISTING_SESSION);
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
    public void testMustJoinBeanJndi() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        DefaultSessionRunner.execute(
                createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.MUST_JOIN_EXISTING_SESSION),
                SessionPolicy.MUST_START_NEW_SESSION);
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
    public void testMustJoinFailsBeanJndi() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        try {
            DefaultSessionRunner.execute(
                    createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.MUST_JOIN_EXISTING_SESSION),
                    SessionPolicy.MUST_JOIN_EXISTING_SESSION);
            fail("must throw exception");
        } catch (IllegalStateException e) {
            assertEquals("Session is not running in thread for context DEFAULT_CONTEXT and session policy requires to join session", e.getMessage());
        }
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    @Test
    public void testMustStartFailsBeanJndi() throws SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        try {
            DefaultSessionRunner.execute(
                    createTransactionRunnable("insert into test values (1, 'John')", "insert into test values (2, 'John')", SessionPolicy.MUST_START_NEW_SESSION),
                    SessionPolicy.MUST_START_NEW_SESSION);
            fail("must throw exception");
        } catch (SystemException e) {
            assertEquals("java.lang.IllegalStateException: Session already running in thread for context DEFAULT_CONTEXT and session policy requires to start new session", e.getMessage());
        }
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    @Test
    public void testSessionPolicy() {
        assertEquals(SessionPolicy.MUST_START_NEW_SESSION, SessionPolicy.valueOf(SessionPolicy.MUST_START_NEW_SESSION.name()));
    }

    private TransactionRunnable<Void> createTransactionRunnable(final String statement1,
                                                                final String statement2, final SessionPolicy sessionPolicy2) {
        return (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(statement1);
                DefaultSessionRunner.execute((TransactionRunnable<Void>) (connection1, arguments1) -> {
                    try (Statement stmt1 = connection1.createStatement()) {
                        stmt1.execute(statement2);
                    }
                    return null;
                }, sessionPolicy2);
            } catch (SystemException e) {
                throw new SQLException(e.getMessage());
            }
            return null;
        };
    }


    @Test
    public void testJoinedFailsBeanJndi() throws SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);
        try {
            DefaultSessionRunner.execute(
                    createTransactionRunnableThatFails("insert into test values (1, 'John')", SessionPolicy.CAN_JOIN_EXISTING_SESSION),
                    SessionPolicy.MUST_START_NEW_SESSION);
            fail("must throw exception");
        } catch (SystemException e) {
            assertEquals("java.sql.SQLException: java.sql.SQLException: forced rollback", e.getMessage());
        }
        Connection connection = createDataSource().getConnection();
        ResultSet rs = connection.createStatement().executeQuery("select * from test");
        assertFalse(rs.next());
        connection.close();
    }

    private TransactionRunnable<Void> createTransactionRunnableThatFails(final String statement1,
                                                                         final SessionPolicy sessionPolicy2) {
        return (connection, arguments) -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(statement1);
                DefaultSessionRunner.execute((TransactionRunnable<Void>) (connection1, arguments1) -> {
                    throw new SQLException("forced rollback");
                }, sessionPolicy2);
            } catch (SystemException e) {
                throw new SQLException(e.getMessage());
            }
            return null;
        };
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
