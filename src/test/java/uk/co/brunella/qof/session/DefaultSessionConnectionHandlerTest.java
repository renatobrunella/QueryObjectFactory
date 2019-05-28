package uk.co.brunella.qof.session;

import org.junit.Test;
import uk.co.brunella.qof.testtools.LoggingDelegationProxy;
import uk.co.brunella.qof.testtools.LoggingDelegationProxyFactory;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class DefaultSessionConnectionHandlerTest {

    @Test
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

    @Test
    public void testGetConnectionWithoutAutoCommit() throws SystemException {
        MockDataSource mockDataSource = new MockDataSource();
        DataSource dataSource = (DataSource) LoggingDelegationProxyFactory.createProxy(mockDataSource, DataSource.class);
        DefaultSessionConnectionHandler handler = new DefaultSessionConnectionHandler(false);
        Connection connection = handler.getConnection(dataSource);
        assertSame(mockDataSource.connection, connection);
        List<String> log = ((LoggingDelegationProxy) connection).getLog();
        assertEquals(0, log.size());
    }

    @Test
    public void testGetConnectionReturnsNull() {
        DataSource dataSource = (DataSource) LoggingDelegationProxyFactory.createProxy(new NullDataSource(), DataSource.class);
        DefaultSessionConnectionHandler handler = new DefaultSessionConnectionHandler(true);
        try {
            handler.getConnection(dataSource);
            fail("should throw exception");
        } catch (SystemException e) {
            assertEquals("java.sql.SQLException: DataSource returned null connection", e.getMessage());
        }
    }

    @Test
    public void testCloseConnectionThrowsException() {
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
        public Connection getConnection() {
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

        MockDataSource() {
            connection = MockConnectionFactory.getConnection();
        }

        public Connection getConnection() {
            return connection;
        }
    }
}
