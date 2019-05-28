package uk.co.brunella.qof.session;

import org.junit.Test;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BaseSessionRunnerTest {

    private static List<String> log;

    @Test
    public void testSuccessDefaultContext() throws SystemException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(new MockDataSource());
        String result = new BaseSessionRunner<String>() {
            @Override
            protected String run(Connection connection, Object... arguments) {
                assertEquals(1, arguments.length);
                return "TEST1";
            }
        }.execute("TEST1");
        assertEquals("TEST1", result);
        assertEquals(3, log.size());
        assertEquals("setAutoCommit(false)", log.get(0));
        assertEquals("commit()", log.get(1));
        assertEquals("close()", log.get(2));
    }

    @Test
    public void testSuccessNamedContext() throws SystemException {
        SessionContextFactory.removeContext("MY_CONTEXT");
        SessionContextFactory.setDataSource("MY_CONTEXT", new MockDataSource());
        String result = new BaseSessionRunner<String>("MY_CONTEXT") {
            @Override
            protected String run(Connection connection, Object... arguments) {
                assertEquals(1, arguments.length);
                return "TEST2";
            }
        }.execute("TEST2");
        assertEquals("TEST2", result);
        assertEquals(3, log.size());
        assertEquals("setAutoCommit(false)", log.get(0));
        assertEquals("commit()", log.get(1));
        assertEquals("close()", log.get(2));
    }

    @Test
    public void testFailureDefaultContext() {
        SessionContextFactory.removeContext();
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

    @Test
    public void testForcedRollbackDefaultContext() throws SystemException {
        SessionContextFactory.removeContext();
        new SessionContextFactory();
        SessionContextFactory.setDataSource(new MockDataSource());
        new BaseSessionRunner<Void>() {
            @Override
            protected Void run(Connection connection, Object... arguments) {
                try {
                    SessionContextFactory.getContext().getUserTransaction()
                            .setRollbackOnly();
                } catch (SystemException ignored) {
                }
                return null;
            }
        }.execute();
        assertEquals(3, log.size());
        assertEquals("setAutoCommit(false)", log.get(0));
        assertEquals("rollback()", log.get(1));
        assertEquals("close()", log.get(2));
    }

    private class MockDataSource implements DataSource {

        public Connection getConnection() {
            Connection connection = MockConnectionFactory.getConnection();
            log = ((MockConnectionData) connection).getLog();
            return connection;
        }

        public Connection getConnection(String username, String password) {
            return null;
        }

        public PrintWriter getLogWriter() {
            return null;
        }

        public void setLogWriter(PrintWriter out) {
        }

        public int getLoginTimeout() {
            return 0;
        }

        public void setLoginTimeout(int seconds) {
        }

        public Logger getParentLogger() {
            return null;
        }

        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        public <T> T unwrap(Class<T> iface) {
            return null;
        }

    }
}
