package uk.co.brunella.qof.session;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Test;
import uk.co.brunella.qof.testtools.LoggingDelegationProxy;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class DefaultSessionContextTest {

    private Connection c1;
    private Connection c2;
    private SessionContext ctx;
    private boolean passed;

    private DataSource createDataSource() {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:aname");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Test
    public void testGetConnection() throws SystemException {
        SessionContextFactory.removeContext();
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

    @Test
    public void testGetConnectionTwoContexts() throws SystemException {
        SessionContextFactory.setDataSource(this.getClass().getName() + ".A", createDataSource());
        SessionContext ctxA = SessionContextFactory.getContext(this.getClass().getName() + ".A");
        SessionContextFactory.setDataSource(this.getClass().getName() + ".B", createDataSource());
        SessionContext ctxB = SessionContextFactory.getContext(this.getClass().getName() + ".B");
        ctxA.startSession();
        ctxB.startSession();
        Connection connectionA = ctxA.getConnection();
        assertNotNull(connectionA);
        Connection connectionB = ctxB.getConnection();
        assertNotNull(connectionB);
        assertNotSame(connectionA, connectionB);
        ctxA.stopSession();
        ctxB.stopSession();
    }

    @Test
    public void testConnectionClosed() throws SQLException, SystemException {
        SessionContextFactory.setDataSource(this.getClass().getName() + ".xyz", createDataSource());
        SessionContext ctx = SessionContextFactory.getContext(this.getClass().getName() + ".xyz");
        ctx.startSession();
        Connection connection = ctx.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        ctx.stopSession();
        assertTrue(connection.isClosed());
    }

    @Test
    public void testFailsNoDataSource() {
        try {
            SessionContextFactory.removeContext();
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

    @Test
    public void testGetConnectionTwoThreads() {
        SessionContextFactory.setDataSource("testGetConnectionTwoThreads", createDataSource());

        Runnable r1 = () -> {
            SessionContext ctx = SessionContextFactory.getContext("testGetConnectionTwoThreads");
            try {
                ctx.startSession();
                c1 = ctx.getConnection();
                ctx.stopSession();
            } catch (SystemException ignored) {
            }
        };

        Runnable r2 = () -> {
            SessionContext ctx = SessionContextFactory.getContext("testGetConnectionTwoThreads");
            try {
                ctx.startSession();
                c2 = ctx.getConnection();
                ctx.stopSession();
            } catch (SystemException ignored) {
            }
        };

        Thread t1 = new Thread(r1);
        t1.run();
        Thread t2 = new Thread(r2);
        t2.run();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException ignored) {
        }

        assertNotNull(c1);
        assertNotNull(c2);
        assertNotSame("The two connection must not be the same", c1, c2);
    }

    @Test
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

            void doIt() {
                SessionContext ctx = SessionContextFactory.getContext("testGetConnectionThreadsRunTwice");
                if (firstRun) {
                    try {
                        ctx.startSession();
                    } catch (SystemException ignored) {
                    }
                }
                c1 = ctx.getConnection();
                if (!firstRun) {
                    try {
                        ctx.stopSession();
                    } catch (SystemException ignored) {
                    }
                }
                firstRun = false;
            }
        };

        Thread t1 = new Thread(r1);
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException ignored) {
        }

        assertNotNull(c1);
        assertSame("The two connection must be the same", c1, c2);
    }

    @Test
    public void testReusingInvalidContext() throws SystemException {
        SessionContextFactory.setDataSource("testReusingInvalidContext", createDataSource());

        ctx = SessionContextFactory.getContext("testReusingInvalidContext");
        ctx.startSession();
        ctx.getConnection();

        Runnable r = () -> {
            try {
                ctx.getConnection();
                fail("Should raise exception");
                passed = false;
            } catch (RuntimeException e) {
                assertEquals("Session is not running in thread for context testReusingInvalidContext", e.getMessage());
                passed = true;
            }
        };

        Thread t = new Thread(r);
        t.start();
        try {
            t.join();
        } catch (InterruptedException ignored) {
        }

        ctx.stopSession();
        assertTrue(passed);
    }

    @Test
    public void testStartingTwice() throws SystemException {
        String contextName = this.getClass().getName() + ".testStartingTwice";
        SessionContextFactory.setDataSource(contextName, createDataSource());
        SessionContext ctx = SessionContextFactory.getContext(contextName);

        ctx.startSession();
        try {
            ctx.startSession();
            fail("Should raise exception");
        } catch (IllegalStateException e) {
            assertEquals("Session already running in thread for context " + contextName + " and session policy requires to start new session", e.getMessage());
        }
        ctx.stopSession();
    }

    @Test
    public void testStoppedWithoutStarted() throws SystemException {
        String contextName = this.getClass().getName() + ".contextName";
        SessionContextFactory.setDataSource(contextName, createDataSource());
        SessionContext ctx = SessionContextFactory.getContext(contextName);

        try {
            ctx.stopSession();
            fail("Should raise exception");
        } catch (IllegalStateException e) {
            assertEquals("Session is not running in thread for context " + contextName, e.getMessage());
        }
    }

    @Test
    public void testGetUserTransaction() throws SystemException {
        String contextName = this.getClass().getName() + ".testGetUserTransaction";
        SessionContextFactory.setDataSource(contextName, createDataSource());
        SessionContext ctx = SessionContextFactory.getContext(contextName);
        try {
            ctx.getUserTransaction();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Session is not running in thread for context " + contextName, e.getMessage());
        }
        ctx.startSession();
        assertNotNull(ctx.getUserTransaction());
        ctx.stopSession();
    }

    @Test
    public void testIsRollbackOnly() throws SystemException {
        String contextName = this.getClass().getName() + ".testIsRollbackOnly";
        SessionContextFactory.setDataSource(contextName, createDataSource());
        SessionContext ctx = SessionContextFactory.getContext(contextName);
        try {
            ctx.getUserTransaction().isRollbackOnly();
            fail("Should raise exception");
        } catch (IllegalStateException e) {
            assertEquals("Session is not running in thread for context " + contextName, e.getMessage());
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
    public void testSetSessionConnectionHandler() throws SystemException {
        final boolean[] called = new boolean[2];
        SessionConnectionHandler handler = new SessionConnectionHandler() {
            public void closeConnection(Connection connection) throws SystemException {
                try {
                    called[0] = true;
                    connection.close();
                } catch (SQLException e) {
                    throw new SystemException(e);
                }
            }

            public Connection getConnection(DataSource dataSource) throws SystemException {
                try {
                    called[1] = true;
                    return dataSource.getConnection();
                } catch (SQLException e) {
                    throw new SystemException(e);
                }
            }
        };
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(createDataSource());
        SessionContextFactory.setSessionConnectionHandler(handler);
        SessionContext ctx = SessionContextFactory.getContext();
        assertFalse(called[0]);
        assertFalse(called[1]);
        ctx.startSession();
        assertFalse(called[0]);
        assertTrue(called[1]);
        assertNotNull(ctx.getConnection());
        ctx.stopSession();
        assertTrue(called[0]);
        assertTrue(called[1]);
        // reset to default handler
        SessionContextFactory.setSessionConnectionHandler(null);
    }

    @Test
    public void testSetSessionConnectionHandlerContext() throws SystemException {
        final boolean[] called = new boolean[2];
        SessionConnectionHandler handler = new SessionConnectionHandler() {
            public void closeConnection(Connection connection) throws SystemException {
                try {
                    called[0] = true;
                    connection.close();
                } catch (SQLException e) {
                    throw new SystemException(e);
                }
            }

            public Connection getConnection(DataSource dataSource) throws SystemException {
                try {
                    called[1] = true;
                    return dataSource.getConnection();
                } catch (SQLException e) {
                    throw new SystemException(e);
                }
            }
        };
        String contextName = this.getClass().getName() + ".testSetSessionConnectionHandlerContext";
        SessionContextFactory.setDataSource(contextName, createDataSource());
        SessionContextFactory.setSessionConnectionHandler(contextName, handler);
        SessionContext ctx = SessionContextFactory.getContext(contextName);
        assertFalse(called[0]);
        assertFalse(called[1]);
        ctx.startSession();
        assertFalse(called[0]);
        assertTrue(called[1]);
        assertNotNull(ctx.getConnection());
        ctx.stopSession();
        assertTrue(called[0]);
        assertTrue(called[1]);
    }

    @Test
    public void testSetAutoCommitPolicyFalse() throws SystemException {
        MockDataSource dataSource = new MockDataSource();
        String contextName = this.getClass().getName() + ".testSetAutoCommitPolicyFalse";
        SessionContextFactory.setDataSource(contextName, dataSource);
        SessionContextFactory.setAutoCommitPolicy(contextName, false);
        SessionContext ctx = SessionContextFactory.getContext(contextName);
        List<String> log = ((LoggingDelegationProxy) dataSource.connection).getLog();
        assertEquals(0, log.size());
        ctx.startSession();
        assertEquals(0, log.size());
        ctx.stopSession();
        assertEquals(1, log.size());
        assertEquals("close()", log.get(0));
    }

    @Test
    public void testSetAutoCommitPolicyTrue() throws SystemException {
        MockDataSource dataSource = new MockDataSource();
        String contextName = this.getClass().getName() + ".testSetAutoCommitPolicyTrue";
        SessionContextFactory.setDataSource(contextName, dataSource);
        SessionContextFactory.setAutoCommitPolicy(contextName, true);
        SessionContext ctx = SessionContextFactory.getContext(contextName);
        List<String> log = ((LoggingDelegationProxy) dataSource.connection).getLog();
        assertEquals(0, log.size());
        ctx.startSession();
        assertEquals(1, log.size());
        assertEquals("setAutoCommit(false)", log.get(0));
        ctx.stopSession();
        assertEquals(2, log.size());
        assertEquals("close()", log.get(1));
    }

    @Test
    public void testSetAutoCommitPolicyTrueDefault() throws SystemException {
        SessionContextFactory.removeContext();
        MockDataSource dataSource = new MockDataSource();
        SessionContextFactory.setDataSource(dataSource);
        SessionContextFactory.setAutoCommitPolicy(true);
        SessionContext ctx = SessionContextFactory.getContext();
        SessionContextFactory.removeContext();
        List<String> log = ((LoggingDelegationProxy) dataSource.connection).getLog();
        assertEquals(0, log.size());
        ctx.startSession();
        assertEquals(1, log.size());
        assertEquals("setAutoCommit(false)", log.get(0));
        ctx.stopSession();
        assertEquals(2, log.size());
        assertEquals("close()", log.get(1));
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
