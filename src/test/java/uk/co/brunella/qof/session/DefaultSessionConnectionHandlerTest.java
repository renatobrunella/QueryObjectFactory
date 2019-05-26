package uk.co.brunella.qof.session;

import junit.framework.TestCase;
import uk.co.brunella.qof.testtools.LoggingDelegationProxy;
import uk.co.brunella.qof.testtools.LoggingDelegationProxyFactory;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DefaultSessionConnectionHandlerTest extends TestCase {

    public void testGetConnectionWithAutoCommit() throws SystemException {
        MockDataSource mockDataSource = new MockDataSource();
        DataSource dataSource = (DataSource) LoggingDelegationProxyFactory.createProxy(mockDataSource, DataSource.class);
        DefaultSessionConnectionHandler handler = new DefaultSessionConnectionHandler(true);
        Connection connection = handler.getConnection(dataSource);
        assertSame(mockDataSource.connection, connection);
        List<String> log = ((LoggingDelegationProxy) connection).getLog();
        assertEquals(1, log.size());
        assertEquals("setAutoCommit(false)", log.get(0));
    }

    public void testGetConnectionWithoutAutoCommit() throws SystemException {
        MockDataSource mockDataSource = new MockDataSource();
        DataSource dataSource = (DataSource) LoggingDelegationProxyFactory.createProxy(mockDataSource, DataSource.class);
        DefaultSessionConnectionHandler handler = new DefaultSessionConnectionHandler(false);
        Connection connection = handler.getConnection(dataSource);
        assertSame(mockDataSource.connection, connection);
        List<String> log = ((LoggingDelegationProxy) connection).getLog();
        assertEquals(0, log.size());
    }

    public void testGetConnectionReturnsNull() throws SystemException {
        DataSource dataSource = (DataSource) LoggingDelegationProxyFactory.createProxy(new NullDataSource(), DataSource.class);
        DefaultSessionConnectionHandler handler = new DefaultSessionConnectionHandler(true);
        try {
            handler.getConnection(dataSource);
            fail("should throw exception");
        } catch (SystemException e) {
            assertEquals("java.sql.SQLException: DataSource returned null connection", e.getMessage());
        }
    }

    public void testCloseConnectionThrowsException() throws SystemException {
        Connection connection = (Connection) LoggingDelegationProxyFactory.createProxy(new ConnectionThrowsException(), Connection.class);
        DefaultSessionConnectionHandler handler = new DefaultSessionConnectionHandler(true);
        try {
            handler.closeConnection(connection);
            fail("should throw exception");
        } catch (SystemException e) {
            assertEquals("java.sql.SQLException: test throw", e.getMessage());
        }
    }

    public static class NullDataSource {
        public Connection getConnection() throws SQLException {
            return null;
        }
    }

    public static class ConnectionThrowsException {
        public void close() throws SQLException {
            throw new SQLException("test throw");
        }
    }

    public static class MockDataSource {
        public Connection connection;

        public MockDataSource() {
            connection = MockConnectionFactory.getConnection();
        }

        public Connection getConnection() throws SQLException {
            return connection;
        }
    }
}
