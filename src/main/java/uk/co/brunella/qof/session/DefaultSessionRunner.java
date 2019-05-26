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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * <code>DefaultSessionRunner</code> is the default implementation that runs
 * a <code>TransactionRunnable</code> in a session context.
 * <p>
 * Calling <code>execute</code> starts a new session, calls the <code>run</code>
 * method of the <code>TransactionRunnable</code> and commits or rolls back
 * the transaction on success or failure.
 *
 * <p>Typical usage is like this:</p>
 *
 * <blockquote><pre>
 * List&lt;Person&gt; personList = ...
 * PersonUpdaterRunnable runnable = new PersonUpdaterRunnable();
 * Integer numberOfUpdates =
 *   new DefaultSessionRunner&lt;Integer&gt;(runnable, "MY_CONTEXT_NAME").execute(personList);
 * </pre></blockquote>
 *
 * @param <T> the type of the result of the <code>TransactionRunnable</code>. If
 *            no result is returned this type should be <code>Void</code>
 * @see SessionRunner
 * @since 1.0.0
 */
public class DefaultSessionRunner<T> extends BaseSessionRunner<T> {

    private TransactionRunnable<T> runnable;

    /**
     * Creates a <code>DefaultSessionRunner</code> that creates a session
     * from the default session context.
     *
     * @param runnable a <code>TransactionRunnable</code>
     * @since 1.0.0
     */
    public DefaultSessionRunner(TransactionRunnable<T> runnable) {
        super();
        this.runnable = runnable;
    }

    /**
     * Creates a <code>DefaultSessionRunner</code> that creates a session
     * from the session context with the given name.
     *
     * @param runnable    a <code>TransactionRunnable</code>
     * @param contextName the context name
     * @since 1.0.0
     */
    public DefaultSessionRunner(TransactionRunnable<T> runnable, String contextName) {
        super(contextName);
        this.runnable = runnable;
    }

    /**
     * Creates a <code>DefaultSessionRunner</code> that creates a session
     * from the default session context and given session policy.
     *
     * @param runnable      a <code>TransactionRunnable</code>
     * @param sessionPolicy the session policy
     * @since 1.1.0
     */
    public DefaultSessionRunner(TransactionRunnable<T> runnable, SessionPolicy sessionPolicy) {
        super(sessionPolicy);
        this.runnable = runnable;
    }

    /**
     * Creates a <code>DefaultSessionRunner</code> that creates a session
     * from the session context with the given name and given session policy.
     *
     * @param runnable      a <code>TransactionRunnable</code>
     * @param contextName   the context name
     * @param sessionPolicy the session policy
     * @since 1.1.0
     */
    public DefaultSessionRunner(TransactionRunnable<T> runnable, String contextName, SessionPolicy sessionPolicy) {
        super(contextName, sessionPolicy);
        this.runnable = runnable;
    }

    /**
     * A call to <code>execute</code> starts a new session and executes some code
     * in a transactional context.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>       the result type
     * @param runnable  a <code>TransactionRunnable</code>
     * @param arguments arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T execute(TransactionRunnable<T> runnable, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable).execute(arguments);
    }

    /**
     * A call to <code>executeBeanManaged</code> starts a new session and
     * executes some code in a transactional context using bean managed
     * transaction management.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>       the result type
     * @param runnable  a <code>TransactionRunnable</code>
     * @param arguments arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T executeBeanManaged(TransactionRunnable<T> runnable, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable).executeBeanManaged(arguments);
    }

    /**
     * A call to <code>executeContainerManaged</code> starts a new session and
     * executes some code in a transactional context using container managed
     * transaction management.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>       the result type
     * @param runnable  a <code>TransactionRunnable</code>
     * @param arguments arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T executeContainerManaged(TransactionRunnable<T> runnable, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable).executeContainerManaged(arguments);
    }

    /**
     * A call to <code>execute</code> starts a new session and executes some code
     * in a transactional context.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>         the result type
     * @param runnable    a <code>TransactionRunnable</code>
     * @param contextName the context name
     * @param arguments   arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T execute(TransactionRunnable<T> runnable, String contextName, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable, contextName).execute(arguments);
    }

    /**
     * A call to <code>executeBeanManaged</code> starts a new session and
     * executes some code in a transactional context using bean managed
     * transaction management.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>         the result type
     * @param runnable    a <code>TransactionRunnable</code>
     * @param contextName the context name
     * @param arguments   arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T executeBeanManaged(TransactionRunnable<T> runnable, String contextName, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable, contextName).executeBeanManaged(arguments);
    }

    /**
     * A call to <code>executeContainerManaged</code> starts a new session and
     * executes some code in a transactional context using container managed
     * transaction management.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>         the result type
     * @param runnable    a <code>TransactionRunnable</code>
     * @param contextName the context name
     * @param arguments   arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T executeContainerManaged(TransactionRunnable<T> runnable, String contextName, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable, contextName).executeContainerManaged(arguments);
    }


    /**
     * A call to <code>execute</code> starts a new session and executes some code
     * in a transactional context.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>           the result type
     * @param runnable      a <code>TransactionRunnable</code>
     * @param sessionPolicy the session policy
     * @param arguments     arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T execute(TransactionRunnable<T> runnable, SessionPolicy sessionPolicy, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable, sessionPolicy).execute(arguments);
    }

    /**
     * A call to <code>executeBeanManaged</code> starts a new session and
     * executes some code in a transactional context using bean managed
     * transaction management.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>           the result type
     * @param runnable      a <code>TransactionRunnable</code>
     * @param sessionPolicy the session policy
     * @param arguments     arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T executeBeanManaged(TransactionRunnable<T> runnable, SessionPolicy sessionPolicy, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable, sessionPolicy).executeBeanManaged(arguments);
    }

    /**
     * A call to <code>executeContainerManaged</code> starts a new session and
     * executes some code in a transactional context using container managed
     * transaction management.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>           the result type
     * @param runnable      a <code>TransactionRunnable</code>
     * @param sessionPolicy the session policy
     * @param arguments     arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T executeContainerManaged(TransactionRunnable<T> runnable, SessionPolicy sessionPolicy, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable, sessionPolicy).executeContainerManaged(arguments);
    }

    /**
     * A call to <code>execute</code> starts a new session with a given name and
     * executes some code in a transactional context.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>           the result type
     * @param runnable      a <code>TransactionRunnable</code>
     * @param contextName   the context name
     * @param sessionPolicy the session policy
     * @param arguments     arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T execute(TransactionRunnable<T> runnable, String contextName, SessionPolicy sessionPolicy, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable, contextName, sessionPolicy).execute(arguments);
    }

    /**
     * A call to <code>executeBeanManaged</code> starts a new session with
     * a given name and executes some code in a transactional context using
     * bean managed transaction management.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>           the result type
     * @param runnable      a <code>TransactionRunnable</code>
     * @param contextName   the context name
     * @param sessionPolicy the session policy
     * @param arguments     arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T executeBeanManaged(TransactionRunnable<T> runnable, String contextName, SessionPolicy sessionPolicy, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable, contextName, sessionPolicy).executeBeanManaged(arguments);
    }

    /**
     * A call to <code>executeContainerManaged</code> starts a new session with
     * a given name and executes some code in a transactional context using
     * container managed transaction management.
     * <p>
     * If an exception is thrown by the executed code the
     * transaction is rolled back otherwise it is committed.
     *
     * @param <T>           the result type
     * @param runnable      a <code>TransactionRunnable</code>
     * @param contextName   the context name
     * @param sessionPolicy the session policy
     * @param arguments     arguments that are passed to the executed code
     * @return the result of the executed code
     * @throws SystemException thrown if an unexpected error occurred
     * @since 1.1.0
     */
    public static <T> T executeContainerManaged(TransactionRunnable<T> runnable, String contextName, SessionPolicy sessionPolicy, Object... arguments) throws SystemException {
        return new DefaultSessionRunner<T>(runnable, contextName, sessionPolicy).executeContainerManaged(arguments);
    }

    @Override
    protected T run(Connection connection, Object... arguments) throws SQLException {
        return runnable.run(connection, arguments);
    }
}
