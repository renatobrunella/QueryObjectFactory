package sf.qof.session;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.sql.DataSource;

import junit.framework.TestCase;
import sf.qof.testtools.LoggingDelegationProxy;
import sf.qof.testtools.MockConnectionFactory;

public class JndiSessionContextTest extends TestCase {

    public void setUp() {
        MockInitialContextFactory.register();
        try {
            MockContext.getInstance().bind("datasource", new MockDataSource());
        } catch (NamingException e) {
        }
    }

    public void tearDown() {
        try {
            MockContext.getInstance().unbind("datasource");
        } catch (NamingException e) {
        }
    }

    public void testGetConnection() throws SystemException {
        SessionContextFactory.setJndiDataSource("JndiSessionContextTest.testGetConnection", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctx = SessionContextFactory.getContext("JndiSessionContextTest.testGetConnection");
        try {
            assertNull(ctx.getConnection());
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Session is not running in thread for context JndiSessionContextTest.testGetConnection", e.getMessage());
        }
        ctx.startSession();
        assertNotNull(ctx.getConnection());
        ctx.stopSession();
        try {
            assertNull(ctx.getConnection());
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Session is not running in thread for context JndiSessionContextTest.testGetConnection", e.getMessage());
        }
    }

    public void testGetConnectionTwoContexts() throws SystemException {
        SessionContextFactory.setJndiDataSource("JndiSessionContextTest.A", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctxA = SessionContextFactory.getContext("JndiSessionContextTest.A");
        SessionContextFactory.setJndiDataSource("JndiSessionContextTest.B", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctxB = SessionContextFactory.getContext("JndiSessionContextTest.B");
        ctxA.startSession();
        ctxB.startSession();
        Connection connectionA = ctxA.getConnection();
        assertNotNull(connectionA);
        Connection connectionB = ctxB.getConnection();
        assertNotNull(connectionB);
        assertTrue(connectionA == connectionB);
        ctxA.stopSession();
        ctxB.stopSession();
    }

    public void testConnectionClosed() throws SQLException, SystemException {
        SessionContextFactory.setJndiDataSource("JndiSessionContextTest.xyz", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctx = SessionContextFactory.getContext("JndiSessionContextTest.xyz");
        ctx.startSession();
        Connection connection = ctx.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        ctx.stopSession();
        assertTrue(connection.isClosed());
    }

    public void testStartingTwice() throws SystemException {
        SessionContextFactory.setJndiDataSource("JndiSessionContextTest.testStartingTwice", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctx = SessionContextFactory.getContext("JndiSessionContextTest.testStartingTwice");

        ctx.startSession();
        try {
            ctx.startSession();
            fail("Should raise exception");
        } catch (IllegalStateException e) {
            assertEquals("Session already running in thread for context JndiSessionContextTest.testStartingTwice and session policy requires to start new session", e.getMessage());
        }
        ctx.stopSession();
    }

    public void testStoppedWithoutStarted() throws SystemException {
        SessionContextFactory.setJndiDataSource("JndiSessionContextTest.testStoppedWithoutStarted", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctx = SessionContextFactory.getContext("JndiSessionContextTest.testStoppedWithoutStarted");

        try {
            ctx.stopSession();
            fail("Should raise exception");
        } catch (IllegalStateException e) {
            assertEquals("Session is not running in thread for context JndiSessionContextTest.testStoppedWithoutStarted", e.getMessage());
        }
    }

    public void testGetUserTransaction() throws SystemException {
        SessionContextFactory.setJndiDataSource("JndiSessionContextTest.testGetUserTransaction", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctx = SessionContextFactory.getContext("JndiSessionContextTest.testGetUserTransaction");
        try {
            ctx.getUserTransaction();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Session is not running in thread for context JndiSessionContextTest.testGetUserTransaction", e.getMessage());
        }
        ctx.startSession();
        assertNotNull(ctx.getUserTransaction());
        ctx.stopSession();
    }

    public void testIsRollbackOnly() throws SystemException {
        SessionContextFactory.setJndiDataSource("JndiSessionContextTest.testIsRollbackOnly", "datasource", null, TransactionManagementType.valueOf("CONTAINER"));
        SessionContext ctx = SessionContextFactory.getContext("JndiSessionContextTest.testIsRollbackOnly");
        try {
            ctx.getUserTransaction().isRollbackOnly();
            fail("Should raise exception");
        } catch (IllegalStateException e) {
            assertEquals("Session is not running in thread for context JndiSessionContextTest.testIsRollbackOnly", e.getMessage());
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

    public void testSetAutoCommitPolicyTrueBeanManaged() throws SystemException, NamingException {
        SessionContextFactory.removeContext("testSetAutoCommitPolicyTrue");
        SessionContextFactory.setJndiDataSource("testSetAutoCommitPolicyTrue", "datasource", null, TransactionManagementType.BEAN);
        SessionContextFactory.setAutoCommitPolicy("testSetAutoCommitPolicyTrue", true);
        SessionContext ctx = SessionContextFactory.getContext("testSetAutoCommitPolicyTrue");
        List<String> log = ((LoggingDelegationProxy) ((MockDataSource) MockContext.getInstance().lookup("datasource")).connection).getLog();
        assertEquals(0, log.size());
        ctx.startSession();
        assertEquals(1, log.size());
        assertEquals("setAutoCommit(false)", log.get(0));
        ctx.stopSession();
        assertEquals(2, log.size());
        assertEquals("close()", log.get(1));
    }

    public void testSetAutoCommitPolicyTrueContainerManaged() throws SystemException, NamingException {
        SessionContextFactory.removeContext("testSetAutoCommitPolicyTrue");
        SessionContextFactory.setJndiDataSource("testSetAutoCommitPolicyTrue", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContextFactory.setAutoCommitPolicy("testSetAutoCommitPolicyTrue", true);
        SessionContext ctx = SessionContextFactory.getContext("testSetAutoCommitPolicyTrue");
        List<String> log = ((LoggingDelegationProxy) ((MockDataSource) MockContext.getInstance().lookup("datasource")).connection).getLog();
        assertEquals(0, log.size());
        ctx.startSession();
        assertEquals(0, log.size());
        ctx.stopSession();
        assertEquals(1, log.size());
        assertEquals("close()", log.get(0));
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

        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }

        public void setLogWriter(PrintWriter arg0) throws SQLException {
        }

        public void setLoginTimeout(int arg0) throws SQLException {
        }

        @SuppressWarnings("unused")
        public boolean isWrapperFor(Class<?> arg0) throws SQLException {
            return false;
        }

        @SuppressWarnings("unused")
        public <T> T unwrap(Class<T> arg0) throws SQLException {
            return null;
        }

    }
}
