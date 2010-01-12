package sf.qof.session;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

import sf.qof.testtools.MockConnectionFactory;

import junit.framework.TestCase;

public class JndiSessionContextTest extends TestCase {

  public void setUp() {
    System.setProperty("java.naming.factory.initial", MockInitialContextFactory.class.getName());
  }
  
  public void tearDown() {
    System.getProperties().remove("java.naming.factory.initial");
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
    assertFalse(connectionA == connectionB);
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
      assertEquals("Session already running in thread for context JndiSessionContextTest.testStartingTwice", e.getMessage());
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
    SessionContextFactory.setJndiDataSource("JndiSessionContextTest.testIsRollbackOnly", "datasource", null, TransactionManagementType.CONTAINER);
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
  

  public static class MockInitialContextFactory implements
      InitialContextFactory {

    public Context getInitialContext(Hashtable<?, ?> environment)
        throws NamingException {
      return new MockContext();
    }

  }

  public static class MockContext implements Context {


    public Object lookup(String name) throws NamingException {
      if (name.equals("datasource")) {
        return new MockDataSource();
      } else {
        throw new NamingException(name + " not found");
      }
    }

    public Object addToEnvironment(String propName, Object propVal)
        throws NamingException {

      return null;
    }

    public void bind(Name name, Object obj) throws NamingException {

    }

    public void bind(String name, Object obj) throws NamingException {

    }

    public void close() throws NamingException {

    }

    public Name composeName(Name name, Name prefix) throws NamingException {

      return null;
    }

    public String composeName(String name, String prefix)
        throws NamingException {

      return null;
    }

    public Context createSubcontext(Name name) throws NamingException {

      return null;
    }

    public Context createSubcontext(String name) throws NamingException {

      return null;
    }

    public void destroySubcontext(Name name) throws NamingException {

    }

    public void destroySubcontext(String name) throws NamingException {

    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {

      return null;
    }

    public String getNameInNamespace() throws NamingException {

      return null;
    }

    public NameParser getNameParser(Name name) throws NamingException {

      return null;
    }

    public NameParser getNameParser(String name) throws NamingException {

      return null;
    }

    public NamingEnumeration<NameClassPair> list(Name name)
        throws NamingException {

      return null;
    }

    public NamingEnumeration<NameClassPair> list(String name)
        throws NamingException {

      return null;
    }

    public NamingEnumeration<Binding> listBindings(Name name)
        throws NamingException {

      return null;
    }

    public NamingEnumeration<Binding> listBindings(String name)
        throws NamingException {

      return null;
    }

    public Object lookup(Name name) throws NamingException {

      return null;
    }

    public Object lookupLink(Name name) throws NamingException {

      return null;
    }

    public Object lookupLink(String name) throws NamingException {

      return null;
    }

    public void rebind(Name name, Object obj) throws NamingException {

    }

    public void rebind(String name, Object obj) throws NamingException {

    }

    public Object removeFromEnvironment(String propName) throws NamingException {

      return null;
    }

    public void rename(Name oldName, Name newName) throws NamingException {

    }

    public void rename(String oldName, String newName) throws NamingException {

    }

    public void unbind(Name name) throws NamingException {

    }

    public void unbind(String name) throws NamingException {

    }

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
