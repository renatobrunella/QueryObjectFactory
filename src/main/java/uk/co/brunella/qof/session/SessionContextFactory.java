/*
 * Copyright 2007 - 2010 brunella ltd
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
package uk.co.brunella.qof.session;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Implementation of a session context factory.
 *
 * <code>SessionContextFactory</code> provides factory methods to create and cache
 * <code>SessionContext</code> objects.
 *
 * <p>The <code>SessionContextFactory</code> can be used in the following way:</p>
 *
 * <blockquote><pre>
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
 * </pre></blockquote>
 *
 * @see SessionContext
 * @see UserTransaction
 * @since 1.0.0
 */
public class SessionContextFactory {

    private final static Map<String, SessionContext> sessionContextMap = new HashMap<>();

    protected SessionContextFactory() {
    }

    /**
     * Creates and returns the default <code>SessionContext</code>.
     *
     * @return the session context
     * @since 1.0.0
     */
    public static SessionContext getContext() {
        return getContext(SessionContext.DEFAULT_CONTEXT_NAME, DefaultSessionContext.class);
    }

    /**
     * Creates and returns the <code>SessionContext</code> for a given context name.
     *
     * @param contextName the context name
     * @return the session context
     * @since 1.0.0
     */
    public static SessionContext getContext(String contextName) {
        return getContext(contextName, DefaultSessionContext.class);
    }

    /**
     * Removes the default <code>SessionContext</code>.
     *
     * @since 1.1.0
     */
    public static void removeContext() {
        removeContext(SessionContext.DEFAULT_CONTEXT_NAME);
    }

    /**
     * Removes the <code>SessionContext</code> with a given name.
     *
     * @param contextName the context name
     * @since 1.1.0
     */
    public static void removeContext(String contextName) {
        sessionContextMap.remove(contextName);
    }

    private synchronized static SessionContext getContext(String contextName, Class<?> sessionContextClass) {
        SessionContext sessionContext = sessionContextMap.get(contextName);
        if (sessionContext == null) {
            sessionContext = createSessionContext(sessionContextClass, contextName);
            sessionContextMap.put(contextName, sessionContext);
        }
        return sessionContext;
    }

    private static SessionContext createSessionContext(Class<?> sessionContextClass, String contextName) {
        try {
            Constructor<?> constructor = sessionContextClass.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            return (SessionContext) constructor.newInstance(contextName);
        } catch (Exception e) {
            throw new RuntimeException("Creation of session context failed", e);
        }
    }

    /**
     * Registers a <code>DataSource</code> with the default session context.
     *
     * @param dataSource the data source
     * @since 1.0.0
     */
    public static void setDataSource(DataSource dataSource) {
        setDataSource(SessionContext.DEFAULT_CONTEXT_NAME, dataSource);
    }

    /**
     * Registers a <code>DataSource</code> with the specified session context.
     *
     * @param contextName the session context name
     * @param dataSource  the data source
     * @since 1.0.0
     */
    public static void setDataSource(String contextName, DataSource dataSource) {
        ((DefaultSessionContext) getContext(contextName)).setDataSource(dataSource);
    }

    /**
     * Registers a JNDI data source with the default session context.
     *
     * @param jndiName                  the JNDI name of the data source
     * @param jndiProperties            the properties for <code>InitialContext</code>
     * @param transactionManagementType the transaction management type
     * @since 1.1.0
     */
    public static void setJndiDataSource(String jndiName, Hashtable<?, ?> jndiProperties, TransactionManagementType transactionManagementType) {
        setJndiDataSource(SessionContext.DEFAULT_CONTEXT_NAME, jndiName, jndiProperties, transactionManagementType);
    }

    /**
     * Registers a JNDI data source with the specified session context.
     *
     * @param contextName               the session context name
     * @param jndiName                  the JNDI name of the data source
     * @param jndiProperties            the properties for <code>InitialContext</code>
     * @param transactionManagementType the transaction management type
     * @see TransactionManagementType
     * @since 1.1.0
     */
    public static void setJndiDataSource(String contextName, String jndiName, Hashtable<?, ?> jndiProperties, TransactionManagementType transactionManagementType) {
        JndiSessionContext sessionContext = (JndiSessionContext) getContext(contextName, JndiSessionContext.class);
        sessionContext.setJndiDataSource(jndiName, jndiProperties, transactionManagementType);
    }

    /**
     * Registers a <code>SessionConnectionHandler</code> with the default session context.
     * <p>
     * The default handler is <code>DefaultSessionConnectionHandler</code>.
     *
     * @param sessionConnectionHandler the session connection handler
     * @see DefaultSessionConnectionHandler
     * @since 1.0.0
     */
    public static void setSessionConnectionHandler(SessionConnectionHandler sessionConnectionHandler) {
        setSessionConnectionHandler(SessionContext.DEFAULT_CONTEXT_NAME, sessionConnectionHandler);
    }

    /**
     * Registers a <code>SessionConnectionHandler</code> with the specified session context.
     * <p>
     * The default handler is <code>DefaultSessionConnectionHandler</code>.
     *
     * @param contextName              the session context name
     * @param sessionConnectionHandler the session connection handler if null use the default handler
     * @see DefaultSessionConnectionHandler
     * @since 1.0.0
     */
    public static void setSessionConnectionHandler(String contextName, SessionConnectionHandler sessionConnectionHandler) {
        ((BaseSessionContext) getContext(contextName)).setSessionConnectionHandler(sessionConnectionHandler);
    }

    /**
     * Sets the auto commit policy of the connection for the default session context.
     * <p>
     * The default behaviour is to call <code>connection.setAutoCommit(false)</code>.
     *
     * @param setAutoCommitToFalse If true <code>setAutoCommit(false)</code> of the connection is called.
     *                             If false <code>setAutoCommit</code> of the connection is not called.
     * @since 1.0.0
     */
    public static void setAutoCommitPolicy(boolean setAutoCommitToFalse) {
        setAutoCommitPolicy(SessionContext.DEFAULT_CONTEXT_NAME, setAutoCommitToFalse);
    }

    /**
     * Sets the auto commit policy of the connection for the specified session context.
     * <p>
     * The default behaviour is to call <code>connection.setAutoCommit(false)</code>.
     *
     * @param contextName          the session context name
     * @param setAutoCommitToFalse If true <code>setAutoCommit(false)</code> of the connection is called.
     *                             If false <code>setAutoCommit</code> of the connection is not called.
     * @since 1.0.0
     */
    public static void setAutoCommitPolicy(String contextName, boolean setAutoCommitToFalse) {
        ((BaseSessionContext) getContext(contextName)).setAutoCommitPolicy(setAutoCommitToFalse);
    }

    private enum SessionState {
        STOPPED, RUNNING
    }

    /**
     * Internal default implementation of <code>SessionContext</code>.
     * <p>
     * Uses a <code>ThreadLocal</code> field to handle sessions for different threads.
     */
    protected static class DefaultSessionContext extends BaseSessionContext {

        private DataSource dataSource;

        private DefaultSessionContext(String contextName) {
            super(contextName);
        }

        @Override
        protected DataSource getDataSource() {
            return dataSource;
        }

        private void setDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        protected UserTransaction getNewUserTransaction(Connection connection, TransactionManagementType transactionManagementType, Session session) throws SystemException {
            if (transactionManagementType == null) {
                throw new IllegalArgumentException("Transaction management type is null");
            }
            if (transactionManagementType != TransactionManagementType.NONE) {
                throw new SystemException("Transaction management type is not supported");
            }
            return new DefaultUserTransaction(session, connection);
        }
    }

    /**
     * Internal JNDI implementation of <code>SessionContext</code>.
     * <p>
     * Uses a <code>ThreadLocal</code> field to handle sessions for different threads.
     */
    protected static class JndiSessionContext extends BaseSessionContext {

        private String jndiName;
        private Hashtable<?, ?> jndiProperties;
        private TransactionManagementType transactionManagementType;

        private JndiSessionContext(String contextName) {
            super(contextName);
            setAutoCommitToFalse = false;
        }

        private void setJndiDataSource(String jndiName, Hashtable<?, ?> jndiProperties, TransactionManagementType transactionManagementType) {
            this.jndiName = jndiName;
            this.jndiProperties = jndiProperties;
            if (transactionManagementType == null) {
                throw new IllegalArgumentException("Transaction management type is null");
            }
            if (transactionManagementType == TransactionManagementType.NONE) {
                this.transactionManagementType = TransactionManagementType.BEAN;
            } else {
                this.transactionManagementType = transactionManagementType;
            }
            // if the CONTAINER manages the transaction do not set autocommit
            // if the BEAN manages the transaction set autocommit to false
            this.setAutoCommitToFalse = this.transactionManagementType != TransactionManagementType.CONTAINER;

        }

        protected void setAutoCommitPolicy(boolean setAutoCommitToFalse) {
            if (transactionManagementType == TransactionManagementType.BEAN) {
                this.setAutoCommitToFalse = setAutoCommitToFalse;
            } else {
                // ignore if CONTAINER manages the transaction
            }
        }

        @Override
        protected DataSource getDataSource() throws SystemException {
            if (jndiName == null) {
                throw new SystemException("No JNDI name defined for context " + contextName);
            }
            try {
                InitialContext ctx = new InitialContext(jndiProperties);
                return (DataSource) ctx.lookup(jndiName);
            } catch (NamingException e) {
                throw new SystemException("JNDI lookup failed", e);
            }
        }

        @Override
        protected UserTransaction getNewUserTransaction(Connection connection, TransactionManagementType transactionManagementType, Session session) {
            if (transactionManagementType == null) {
                throw new IllegalArgumentException("Transaction management type is null");
            }
            if (transactionManagementType == TransactionManagementType.NONE) {
                if (this.transactionManagementType == TransactionManagementType.BEAN) {
                    return new DefaultUserTransaction(session, connection);
                } else {
                    return new NoOpUserTransaction(session);
                }
            } else {
                if (transactionManagementType == TransactionManagementType.BEAN) {
                    return new DefaultUserTransaction(session, connection);
                } else {
                    return new NoOpUserTransaction(session);
                }
            }
        }
    }

    /**
     * Internal base implementation of <code>SessionContext</code>.
     * <p>
     * Uses a <code>ThreadLocal</code> field to handle sessions for different threads.
     */
    protected static abstract class BaseSessionContext implements SessionContext, SessionContextExt {

        final static SessionConnectionHandler DEFAULT_SESSION_CONNECTION_HANDLER_SET_AUTOCOMMIT_TO_FALSE =
                new DefaultSessionConnectionHandler(true);
        final static SessionConnectionHandler DEFAULT_SESSION_CONNECTION_HANDLER =
                new DefaultSessionConnectionHandler(false);

        String contextName;
        SessionConnectionHandler sessionConnectionHandler;
        boolean setAutoCommitToFalse;

        ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<Session>() {
            protected synchronized Session initialValue() {
                return new Session();
            }
        };

        private BaseSessionContext(String contextName) {
            this.contextName = contextName;
            this.setAutoCommitToFalse = true;
        }

        protected void setSessionConnectionHandler(SessionConnectionHandler sessionConnectionHandler) {
            this.sessionConnectionHandler = sessionConnectionHandler;
        }

        protected void setAutoCommitPolicy(boolean setAutoCommitToFalse) {
            this.setAutoCommitToFalse = setAutoCommitToFalse;
        }

        protected abstract DataSource getDataSource() throws SystemException;

        protected abstract UserTransaction getNewUserTransaction(Connection connection, TransactionManagementType transactionManagementType, Session session) throws SystemException;

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
            startSession(TransactionManagementType.NONE, SessionPolicy.MUST_START_NEW_SESSION);
        }

        public void startSession(SessionPolicy sessionPolicy) throws SystemException {
            startSession(TransactionManagementType.NONE, sessionPolicy);
        }

        public void startSession(TransactionManagementType transactionManagementType) throws SystemException {
            startSession(transactionManagementType, SessionPolicy.MUST_START_NEW_SESSION);
        }

        public void startSession(TransactionManagementType transactionManagementType, SessionPolicy sessionPolicy) throws SystemException {
            if (transactionManagementType == null) {
                throw new IllegalArgumentException("Transaction management type is null");
            }
            Session session = sessionThreadLocal.get();
            DataSource dataSource;
            if (sessionPolicy == SessionPolicy.MUST_START_NEW_SESSION && session.getState() == SessionState.RUNNING) {
                throw new IllegalStateException("Session already running in thread for context "
                        + contextName + " and session policy requires to start new session");
            } else if (sessionPolicy == SessionPolicy.MUST_JOIN_EXISTING_SESSION && session.getState() == SessionState.STOPPED) {
                throw new IllegalStateException("Session is not running in thread for context "
                        + contextName + " and session policy requires to join session");
            }
            if (session.getState() == SessionState.STOPPED) {
                dataSource = getDataSource();
                if (dataSource == null) {
                    throw new SystemException("No data source defined for context " + contextName);
                }
                Connection connection;
                if (sessionConnectionHandler != null) {
                    connection = sessionConnectionHandler.getConnection(dataSource);
                } else {
                    if (transactionManagementType == TransactionManagementType.NONE) {
                        if (setAutoCommitToFalse) {
                            connection = DEFAULT_SESSION_CONNECTION_HANDLER_SET_AUTOCOMMIT_TO_FALSE.getConnection(dataSource);
                        } else {
                            connection = DEFAULT_SESSION_CONNECTION_HANDLER.getConnection(dataSource);
                        }
                    } else {
                        if (transactionManagementType == TransactionManagementType.BEAN) {
                            connection = DEFAULT_SESSION_CONNECTION_HANDLER_SET_AUTOCOMMIT_TO_FALSE.getConnection(dataSource);
                        } else {
                            connection = DEFAULT_SESSION_CONNECTION_HANDLER.getConnection(dataSource);
                        }
                    }
                }
                session.setConnection(connection);
                session.setUserTransaction(getNewUserTransaction(connection, transactionManagementType, session));
                session.setState(SessionState.RUNNING);
            }
            session.join();
        }

        public void stopSession() throws SystemException {
            Session session = sessionThreadLocal.get();
            if (session.getState() == SessionState.STOPPED) {
                throw new IllegalStateException("Session is not running in thread for context " + contextName);
            } else if (session.unjoin() <= 0) {
                session.setState(SessionState.STOPPED);
                ((BaseUserTransaction) session.getUserTransaction()).close();
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

    static class Session {

        private Connection connection;
        private UserTransaction userTransaction;
        private SessionState state = SessionState.STOPPED;
        private int joinCount = 0;

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
            if (state == SessionState.STOPPED) {
                joinCount = 0;
            }
        }

        public UserTransaction getUserTransaction() {
            return userTransaction;
        }

        public void setUserTransaction(UserTransaction userTransaction) {
            this.userTransaction = userTransaction;
        }

        public int join() {
            return ++joinCount;
        }

        public int unjoin() {
            return --joinCount;
        }

        public boolean hasJoinedSession() {
            return joinCount > 1;
        }
    }

    /**
     * Internal implementation of <code>UserTransaction</code>.
     */
    protected static class DefaultUserTransaction extends BaseUserTransaction implements UserTransaction {

        private Connection connection;

        /**
         * Constructs a DefaultUserTransaction object.
         *
         * @param session    the current session
         * @param connection the current database connection
         */
        public DefaultUserTransaction(Session session, Connection connection) {
            super(session);
            this.connection = connection;
        }

        public void commit() throws SystemException, RollbackException {
            if (!session.hasJoinedSession()) {
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
            } else {
                if (transactionState != TransactionState.IN_TRANSACTION &&
                        transactionState != TransactionState.IN_TRANSACTION_ROLLBACK) {
                    throw new IllegalStateException("Invalid state: Transaction is closed " + transactionState);
                }
            }
        }

        public void rollback() throws SystemException {
            if (!session.hasJoinedSession()) {
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
            } else {
                setRollbackOnly();
            }
        }
    }

    /**
     * Internal no-op implementation of <code>UserTransaction</code>.
     */
    protected static class NoOpUserTransaction extends BaseUserTransaction implements UserTransaction {

        /**
         * Constructs a NoOpUserTransaction object.
         *
         * @param session the current session
         */
        public NoOpUserTransaction(Session session) {
            super(session);
        }

        public void commit() throws RollbackException {
            if (!session.hasJoinedSession()) {
                if (transactionState == TransactionState.NEW
                        || transactionState == TransactionState.CLOSED) {
                    throw new IllegalStateException("Invalid state: Transaction is " + transactionState);
                } else {
                    if (transactionState == TransactionState.IN_TRANSACTION_ROLLBACK) {
                        transactionState = TransactionState.NEW;
                        throw new RollbackException("Transaction was rolled back");
                    } else {
                        transactionState = TransactionState.NEW;
                    }
                }
            } else {
                if (transactionState != TransactionState.IN_TRANSACTION &&
                        transactionState != TransactionState.IN_TRANSACTION_ROLLBACK) {
                    throw new IllegalStateException("Invalid state: Transaction is closed " + transactionState);
                }
            }
        }

        public void rollback() throws SystemException {
            if (!session.hasJoinedSession()) {
                if (transactionState == TransactionState.NEW
                        || transactionState == TransactionState.CLOSED) {
                    throw new IllegalStateException("Invalid state: Transaction is " + transactionState);
                } else {
                    transactionState = TransactionState.NEW;
                }
            } else {
                setRollbackOnly();
            }
        }
    }

    /**
     * Internal base implementation of <code>UserTransaction</code>.
     */
    protected static abstract class BaseUserTransaction implements UserTransaction {

        protected TransactionState transactionState;
        protected Session session;

        public BaseUserTransaction(Session session) {
            this.transactionState = TransactionState.NEW;
            this.session = session;
        }

        public void begin() {
            if (!session.hasJoinedSession()) {
                if (transactionState == TransactionState.NEW) {
                    transactionState = TransactionState.IN_TRANSACTION;
                } else {
                    throw new IllegalStateException("Invalid state: Transaction is " + transactionState);
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

        public void setRollbackOnly() {
            if (transactionState == TransactionState.NEW
                    || transactionState == TransactionState.CLOSED) {
                throw new IllegalStateException("Invalid state: Transaction is " + transactionState);
            } else {
                transactionState = TransactionState.IN_TRANSACTION_ROLLBACK;
            }
        }

        public void close() {
            transactionState = TransactionState.CLOSED;
        }

        protected enum TransactionState {
            NEW, IN_TRANSACTION, IN_TRANSACTION_ROLLBACK, CLOSED
        }

    }

}
