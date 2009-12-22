/*
 * Copyright 2007 brunella ltd
 *
 * Licensed under the LGPL Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package sf.qof.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Implementation of a session context factory.
 * 
 * <code>SessionContextFactory</code> provides factory methods to create and cache
 * <code>SessionContext</code> objects.
 * 
 * <p>The <code>SessionContextFactory</code> can be used in the following way:</p>
 * 
 * <p><blockquote><pre>
 * DataSource ds = ... // get the data source from somewhere
 * // register the data source with the default session context 
 * SessionContextFactory.setDataSource(ds);
 * ...
 * // get the default session context
 * SessionContext ctx = SessionContextFactory.getContext();
 * 
 * // start a new session 
 * ctx.startSession();
 * 
 * // start a new transaction
 * ctx.getUserTransaction().begin();
 * 
 * // get the database connection for the current session
 * Connection con = ctx.getConnection();
 * 
 * // do something with the connection
 * ...
 * 
 * // commit and end the current transaction
 * ctx.getUserTransaction().commit();
 * 
 * // stop the session
 * ctx.stopSession();
 * </pre></blockquote></p>
 * 
 * @see sf.qof.session.SessionContext
 * @see sf.qof.session.UserTransaction
 * 
 */
public class SessionContextFactory {

  private final static Map<String, SessionContext> sessionContextMap = new HashMap<String, SessionContext>();

  private SessionContextFactory() {
  }

  /**
   * Creates and returns the default <code>SessionContext</code>.  
   * 
   * @return the session context
   */
  public static SessionContext getContext() {
    return getContext(SessionContext.DEFAULT_CONTEXT_NAME);
  }

  /**
   * Creates and returns the <code>SessionContext</code> for a given context name.
   * 
   * @param contextName the context name
   * @return the session context
   */
  public synchronized static SessionContext getContext(String contextName) {
    SessionContext sessionContext = sessionContextMap.get(contextName);
    if (sessionContext == null) {
      sessionContext = new DefaultSessionContext(contextName);
      sessionContextMap.put(contextName, sessionContext);
    }
    return sessionContext;
  }

  /**
   * Registers a <code>DataSource</code> with the default session context.
   * 
   * @param dataSource  the data source
   */
  public static void setDataSource(DataSource dataSource) {
    setDataSource(SessionContext.DEFAULT_CONTEXT_NAME, dataSource);
  }

  /**
   * Registers a <code>DataSource</code> with the specified session context. 
   * 
   * @param contextName  the session context name
   * @param dataSource   the data source
   */
  public static void setDataSource(String contextName, DataSource dataSource) {
    ((DefaultSessionContext) getContext(contextName)).setDataSource(dataSource);
  }

  /**
   * Registers a <code>SessionConnectionHandler</code> with the default session context.
   * 
   * The default handler is <code>DefaultSessionConnectionHandler</code>.
   * 
   * @param sessionConnectionHandler the session connection handler
   * 
   * @see DefaultSessionConnectionHandler
   */
  public static void setSessionConnectionHandler(SessionConnectionHandler sessionConnectionHandler) {
    setSessionConnectionHandler(SessionContext.DEFAULT_CONTEXT_NAME, sessionConnectionHandler);
  }
  
  /**
   * Registers a <code>SessionConnectionHandler</code> with the specified session context.
   * 
   * The default handler is <code>DefaultSessionConnectionHandler</code>.
   * 
   * @param contextName  the session context name
   * @param sessionConnectionHandler the session connection handler if null use the default handler
   * 
   * @see DefaultSessionConnectionHandler
   */
  public static void setSessionConnectionHandler(String contextName, SessionConnectionHandler sessionConnectionHandler) {
    ((DefaultSessionContext) getContext(contextName)).setSessionConnectionHandler(sessionConnectionHandler);
  }

  /**
   * Sets the auto commit policy of the connection for the default session context.
   * 
   * The default behaviour is to call <code>connection.setAutoCommit(false)</code>.
   * 
   * @param setAutoCommitToFalse  If true <code>setAutoCommit(false)</code> of the connection is called. 
   *   If false <code>setAutoCommit</code> of the connection is not called.
   */
  public static void setAutoCommitPolicy(boolean setAutoCommitToFalse) {
    ((DefaultSessionContext) getContext(SessionContext.DEFAULT_CONTEXT_NAME)).setAutoCommitPolicy(setAutoCommitToFalse);
  }

  /**
   * Sets the auto commit policy of the connection for the specified session context.
   * 
   * The default behaviour is to call <code>connection.setAutoCommit(false)</code>.
   * 
   * @param contextName  the session context name
   * @param setAutoCommitToFalse  If true <code>setAutoCommit(false)</code> of the connection is called. 
   *   If false <code>setAutoCommit</code> of the connection is not called.
   */
  public static void setAutoCommitPolicy(String contextName, boolean setAutoCommitToFalse) {
    ((DefaultSessionContext) getContext(contextName)).setAutoCommitPolicy(setAutoCommitToFalse);
  }

  /**
   * Internal implementation of <code>SessionContext</code>. 
   *
   * Uses a <code>ThreadLocal</code> field to handle sessions for different threads.
   * 
   */
  protected static class DefaultSessionContext implements SessionContext {

    private final static SessionConnectionHandler DEFAULT_SESSION_CONNECTION_HANDLER_SET_AUTOCOMMIT_TO_FALSE =
      new DefaultSessionConnectionHandler(true);
    private final static SessionConnectionHandler DEFAULT_SESSION_CONNECTION_HANDLER =
      new DefaultSessionConnectionHandler(false);
  
    private DataSource dataSource;
    private String contextName;
    private SessionConnectionHandler sessionConnectionHandler;
    private boolean setAutoCommitToFalse;
  
    private ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<Session>() {
      protected synchronized Session initialValue() {
        return new Session();
      }
    };
  
    private DefaultSessionContext(String contextName) {
      this.contextName = contextName;
      this.sessionConnectionHandler = null;
      this.setAutoCommitToFalse = true;
    }
  
    private void setDataSource(DataSource dataSource) {
      this.dataSource = dataSource;
    }
    
    private void setSessionConnectionHandler(SessionConnectionHandler sessionConnectionHandler) {
      if (sessionConnectionHandler == null) {
        this.sessionConnectionHandler = null;
      } else {
        this.sessionConnectionHandler = sessionConnectionHandler;
      }
    }
  
    private void setAutoCommitPolicy(boolean setAutoCommitToFalse) {
      this.setAutoCommitToFalse = setAutoCommitToFalse;
    }
    
    public Connection getConnection() {
      Session session = sessionThreadLocal.get();
      if (session.getState() == SessionState.STOPPED) {
        throw new IllegalStateException("Session is not running in thread for context " + contextName);
      } else {
        return session.getConnection();
      }
    }
  
    public UserTransaction getUserTransaction() {
      Session session = sessionThreadLocal.get();
      if (session.getState() == SessionState.STOPPED) {
        throw new IllegalStateException("Session is not running in thread for context " + contextName);
      } else {
      return session.getUserTransaction();
      }
    }
  
    public void startSession() throws SystemException {
      Session session = sessionThreadLocal.get();
      if (session.getState() == SessionState.RUNNING) {
        throw new IllegalStateException("Session already running in thread for context " + contextName);
      } else {
        if (dataSource == null) {
          throw new SystemException("No data source defined for context " + contextName);
      }
      Connection connection = null;
      if (sessionConnectionHandler != null) {
        connection = sessionConnectionHandler.getConnection(dataSource);
      } else {
        if (setAutoCommitToFalse) {
          connection = DEFAULT_SESSION_CONNECTION_HANDLER_SET_AUTOCOMMIT_TO_FALSE.getConnection(dataSource);
        } else {
          connection = DEFAULT_SESSION_CONNECTION_HANDLER.getConnection(dataSource);
        }
      }
      session.setConnection(connection);
      session.setUserTransaction(new DefaultUserTransaction(connection));
      session.setState(SessionState.RUNNING);
      }
    }
  
    public void stopSession() throws SystemException {
      Session session = sessionThreadLocal.get();
      if (session.getState() == SessionState.STOPPED) {
        throw new IllegalStateException("Session is not running in thread for context " + contextName);
      } else {
        session.setState(SessionState.STOPPED);
        ((DefaultUserTransaction) session.getUserTransaction()).close();
        session.setUserTransaction(null);
        if (sessionConnectionHandler != null) {
          sessionConnectionHandler.closeConnection(session.getConnection());
        } else {
          if (setAutoCommitToFalse) {
            DEFAULT_SESSION_CONNECTION_HANDLER_SET_AUTOCOMMIT_TO_FALSE.closeConnection(session.getConnection());
          } else {
            DEFAULT_SESSION_CONNECTION_HANDLER.closeConnection(session.getConnection());
          }
        }
        session.setConnection(null);
      }
    }
  
  }

  private static class Session {

    private Connection connection;
    private UserTransaction userTransaction;
    private SessionState state = SessionState.STOPPED;
  
    public Connection getConnection() {
      return connection;
    }
  
    public void setConnection(Connection connection) {
      this.connection = connection;
    }
  
    public SessionState getState() {
      return state;
    }
  
    public void setState(SessionState state) {
      this.state = state;
    }
  
    public UserTransaction getUserTransaction() {
      return userTransaction;
    }
  
    public void setUserTransaction(UserTransaction userTransaction) {
      this.userTransaction = userTransaction;
    }
  }

  private static enum SessionState {
    STOPPED, RUNNING
  }

  /**
   * Internal implementation of <code>UserTransaction</code>.
   *
   */
  protected static class DefaultUserTransaction implements UserTransaction {

  private Connection connection;

  private TransactionState transactionState;

  /**
   * Constructs a DefaultUserTransaction object.
   *
   * @param connection  the current database connection
   */
  public DefaultUserTransaction(Connection connection) {
    this.connection = connection;
    this.transactionState = TransactionState.NEW;
  }

  public void begin() throws SystemException {
    if (transactionState == TransactionState.NEW) {
    transactionState = TransactionState.IN_TRANSACTION;
    } else {
    throw new IllegalStateException("Invalid state: Transaction is " + transactionState);
    }
  }

  public void commit() throws SystemException, RollbackException {
    if (transactionState == TransactionState.NEW
      || transactionState == TransactionState.CLOSED) {
    throw new IllegalStateException("Invalid state: Transaction is " + transactionState);
    } else {
    if (transactionState == TransactionState.IN_TRANSACTION_ROLLBACK) {
      transactionState = TransactionState.NEW;
      try {
      connection.rollback();
      } catch (SQLException e) {
      throw new SystemException(e);
      }
      throw new RollbackException("Transaction was rolled back");
    } else {
      transactionState = TransactionState.NEW;
      try {
      connection.commit();
      } catch (SQLException e) {
      throw new SystemException(e);
      }
    }
    }
  }

  public boolean isRollbackOnly() {
    if (transactionState == TransactionState.NEW
      || transactionState == TransactionState.CLOSED) {
    throw new IllegalStateException("Invalid state: Transaction is " + transactionState);
    } else {
    return transactionState == TransactionState.IN_TRANSACTION_ROLLBACK;
    }
  }

  public void rollback() throws SystemException {
    if (transactionState == TransactionState.NEW
      || transactionState == TransactionState.CLOSED) {
    throw new IllegalStateException("Invalid state: Transaction is " + transactionState);
    } else {
    try {
      transactionState = TransactionState.NEW;
      connection.rollback();
    } catch (SQLException e) {
      throw new SystemException(e);
    }
    }
  }

  public void setRollbackOnly() throws SystemException {
    if (transactionState == TransactionState.NEW
      || transactionState == TransactionState.CLOSED) {
    throw new IllegalStateException("Invalid state: Transaction is " + transactionState);
    } else {
    transactionState = TransactionState.IN_TRANSACTION_ROLLBACK;
    }
  }

  /**
   * Closes the transaction.
   */
  public void close() {
    transactionState = TransactionState.CLOSED;
  }

  private static enum TransactionState {
    NEW, IN_TRANSACTION, IN_TRANSACTION_ROLLBACK, CLOSED
  }

  }
}
