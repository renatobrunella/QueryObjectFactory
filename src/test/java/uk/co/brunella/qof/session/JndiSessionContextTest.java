package uk.co.brunella.qof.session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.co.brunella.qof.testtools.LoggingDelegationProxy;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class JndiSessionContextTest {

    @Before
    public void setUp() {
        MockInitialContextFactory.register();
        MockContext.getInstance().bind("datasource", new MockDataSource());
    }

    @After
    public void tearDown() {
        MockContext.getInstance().unbind("datasource");
    }

    @Test
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

    @Test
    public void testGetConnectionTwoContexts() throws SystemException {
        SessionContextFactory.setJndiDataSource(this.getClass().getName() + ".A", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctxA = SessionContextFactory.getContext(this.getClass().getName() + ".A");
        SessionContextFactory.setJndiDataSource(this.getClass().getName() + ".B", "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctxB = SessionContextFactory.getContext(this.getClass().getName() + ".B");
        ctxA.startSession();
        ctxB.startSession();
        Connection connectionA = ctxA.getConnection();
        assertNotNull(connectionA);
        Connection connectionB = ctxB.getConnection();
        assertNotNull(connectionB);
        assertSame(connectionA, connectionB);
        ctxA.stopSession();
        ctxB.stopSession();
    }

    @Test
    public void testConnectionClosed() throws SQLException, SystemException {
        String contextName = this.getClass().getName() + ".xyz";
        SessionContextFactory.setJndiDataSource(contextName, "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctx = SessionContextFactory.getContext(contextName);
        ctx.startSession();
        Connection connection = ctx.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        ctx.stopSession();
        assertTrue(connection.isClosed());
    }

    @Test
    public void testStartingTwice() throws SystemException {
        String contextName = this.getClass().getName() + ".testStartingTwice";
        SessionContextFactory.setJndiDataSource(contextName, "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctx = SessionContextFactory.getContext(contextName);

        ctx.startSession();
        try {
            ctx.startSession();
            fail("Should raise exception");
        } catch (IllegalStateException e) {
            assertEquals("Session already running in thread for context " + JndiSessionContextTest.class.getName() + ".testStartingTwice and session policy requires to start new session", e.getMessage());
        }
        ctx.stopSession();
    }

    @Test
    public void testStoppedWithoutStarted() throws SystemException {
        String contextName = this.getClass().getName() + ".testStoppedWithoutStarted";
        SessionContextFactory.setJndiDataSource(contextName, "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctx = SessionContextFactory.getContext(contextName);

        try {
            ctx.stopSession();
            fail("Should raise exception");
        } catch (IllegalStateException e) {
            assertEquals("Session is not running in thread for context " + JndiSessionContextTest.class.getName() + ".testStoppedWithoutStarted", e.getMessage());
        }
    }

    @Test
    public void testGetUserTransaction() throws SystemException {
        String contextName = this.getClass().getName() + ".testGetUserTransaction";
        SessionContextFactory.setJndiDataSource(contextName, "datasource", null, TransactionManagementType.CONTAINER);
        SessionContext ctx = SessionContextFactory.getContext(contextName);
        try {
            ctx.getUserTransaction();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Session is not running in thread for context " + JndiSessionContextTest.class.getName() + ".testGetUserTransaction", e.getMessage());
        }
        ctx.startSession();
        assertNotNull(ctx.getUserTransaction());
        ctx.stopSession();
    }

    @Test
    public void testIsRollbackOnly() throws SystemException {
        String contextName = this.getClass().getName() + ".testIsRollbackOnly";
        SessionContextFactory.setJndiDataSource(contextName, "datasource", null, TransactionManagementType.valueOf("CONTAINER"));
        SessionContext ctx = SessionContextFactory.getContext(contextName);
        try {
            ctx.getUserTransaction().isRollbackOnly();
            fail("Should raise exception");
        } catch (IllegalStateException e) {
            assertEquals("Session is not running in thread for context " + JndiSessionContextTest.class.getName() + ".testIsRollbackOnly", e.getMessage());
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

    @Test
    public void testSetAutoCommitPolicyTrueBeanManaged() throws SystemException, NamingException {
        String contextName = this.getClass().getName() + ".testSetAutoCommitPolicyTrue";
        SessionContextFactory.removeContext(contextName);
        SessionContextFactory.setJndiDataSource(contextName, "datasource", null, TransactionManagementType.BEAN);
        SessionContextFactory.setAutoCommitPolicy(contextName, true);
        SessionContext ctx = SessionContextFactory.getContext(contextName);
        List<String> log = ((LoggingDelegationProxy) ((MockDataSource) MockContext.getInstance().lookup("datasource")).connection).getLog();
        assertEquals(0, log.size());
        ctx.startSession();
        assertEquals(1, log.size());
        assertEquals("setAutoCommit(false)", log.get(0));
        ctx.stopSession();
        assertEquals(2, log.size());
        assertEquals("close()", log.get(1));
    }

    @Test
    public void testSetAutoCommitPolicyTrueContainerManaged() throws SystemException, NamingException {
        String contextName = this.getClass().getName() + ".testSetAutoCommitPolicyTrue";
        SessionContextFactory.removeContext(contextName);
        SessionContextFactory.setJndiDataSource(contextName, "datasource", null, TransactionManagementType.CONTAINER);
        SessionContextFactory.setAutoCommitPolicy(contextName, true);
        SessionContext ctx = SessionContextFactory.getContext(contextName);
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

        MockDataSource() {
            connection = MockConnectionFactory.getConnection();
        }

        public Connection getConnection() {
            return connection;
        }

        public Connection getConnection(String username, String password) {
            return null;
        }

        public PrintWriter getLogWriter() {
            return null;
        }

        public void setLogWriter(PrintWriter arg0) {
        }

        public int getLoginTimeout() {
            return 0;
        }

        public void setLoginTimeout(int arg0) {
        }

        public Logger getParentLogger() {
            return null;
        }

        public boolean isWrapperFor(Class<?> arg0) {
            return false;
        }

        public <T> T unwrap(Class<T> arg0) {
            return null;
        }

    }
}
