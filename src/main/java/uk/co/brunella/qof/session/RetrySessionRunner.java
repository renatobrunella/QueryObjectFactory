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
 * <code>RetrySessionRunner</code> is an implementation of <code>SessionRunner</code>
 * that runs a <code>TransactionRunnable</code> in a session context and retries
 * if an error occured.
 * <p>
 * Calling <code>execute</code> starts a new session call the <code>run</code>
 * method of the <code>TransactionRunnable</code> and commits or rolls back
 * the transaction on success or failure. If an error occured it retries up to
 * <code>numberOfRetries</code> times. <code>delayInMilliSeconds</code> can be
 * used to wait a specified amount of time between the retries.
 *
 * <p>Typical usage is like this:</p>
 *
 * <p><blockquote><pre>
 * List&lt;Person&gt; personList = ...
 * PersonUpdaterRunnable runnable = new PersonUpdaterRunnable();
 * Integer numberOfUpdates =
 *   new RetrySessionRunner&lt;Integer&gt;(runnable, "MY_CONTEXT_NAME").execute(personList, 3);
 * </pre></blockquote></p>
 *
 * @param <T> the type of the result of the <code>TransactionRunnable</code>. If
 *            no result is returned this type should be <code>Void</code>
 * @see SessionRunner
 * @since 1.0.0
 */
public class RetrySessionRunner<T> extends BaseSessionRunner<T> {

    private int numberOfRetries;
    private long delayInMilliSeconds;
    private TransactionRunnable<T> runnable;

    /**
     * Creates a <code>RetrySessionRunner</code> that creates a session
     * from the default session context.
     *
     * @param runnable        a <code>TransactionRunnable</code>
     * @param numberOfRetries max number of retries
     * @since 1.0.0
     */
    public RetrySessionRunner(TransactionRunnable<T> runnable, int numberOfRetries) {
        this(runnable, numberOfRetries, 0);
    }

    /**
     * Creates a <code>RetrySessionRunner</code> that creates a session
     * from the default session context.
     *
     * @param runnable        a <code>TransactionRunnable</code>
     * @param numberOfRetries max number of retries
     * @param sessionPolicy   the session policy
     * @since 1.1.0
     */
    public RetrySessionRunner(TransactionRunnable<T> runnable, int numberOfRetries, SessionPolicy sessionPolicy) {
        this(runnable, numberOfRetries, 0, sessionPolicy);
    }

    /**
     * Creates a <code>RetrySessionRunner</code> that creates a session
     * from the default session context.
     *
     * @param runnable            a <code>TransactionRunnable</code>
     * @param numberOfRetries     max number of retries
     * @param delayInMilliSeconds delay inbetween retries in milli seconds
     * @since 1.0.0
     */
    public RetrySessionRunner(TransactionRunnable<T> runnable, int numberOfRetries, long delayInMilliSeconds) {
        this(runnable, SessionContext.DEFAULT_CONTEXT_NAME, numberOfRetries, delayInMilliSeconds);
    }

    /**
     * Creates a <code>RetrySessionRunner</code> that creates a session
     * from the default session context.
     *
     * @param runnable            a <code>TransactionRunnable</code>
     * @param numberOfRetries     max number of retries
     * @param delayInMilliSeconds delay inbetween retries in milli seconds
     * @param sessionPolicy       the session policy
     * @since 1.1.0
     */
    public RetrySessionRunner(TransactionRunnable<T> runnable, int numberOfRetries, long delayInMilliSeconds, SessionPolicy sessionPolicy) {
        this(runnable, SessionContext.DEFAULT_CONTEXT_NAME, numberOfRetries, delayInMilliSeconds, sessionPolicy);
    }

    /**
     * Creates a <code>RetrySessionRunner</code> that creates a session
     * from the session context with the given name.
     *
     * @param runnable        a <code>TransactionRunnable</code>
     * @param contextName     the context name
     * @param numberOfRetries max number of retries
     * @since 1.0.0
     */
    public RetrySessionRunner(TransactionRunnable<T> runnable, String contextName, int numberOfRetries) {
        this(runnable, contextName, numberOfRetries, 0);
    }

    /**
     * Creates a <code>RetrySessionRunner</code> that creates a session
     * from the session context with the given name.
     *
     * @param runnable        a <code>TransactionRunnable</code>
     * @param contextName     the context name
     * @param numberOfRetries max number of retries
     * @param sessionPolicy   the session policy
     * @since 1.1.0
     */
    public RetrySessionRunner(TransactionRunnable<T> runnable, String contextName, int numberOfRetries, SessionPolicy sessionPolicy) {
        this(runnable, contextName, numberOfRetries, 0, sessionPolicy);
    }

    /**
     * Creates a <code>RetrySessionRunner</code> that creates a session
     * from the session context with the given name.
     *
     * @param runnable            a <code>TransactionRunnable</code>
     * @param contextName         the context name
     * @param numberOfRetries     max number of retries
     * @param delayInMilliSeconds delay inbetween retries in milli seconds
     * @since 1.0.0
     */
    public RetrySessionRunner(TransactionRunnable<T> runnable, String contextName, int numberOfRetries, long delayInMilliSeconds) {
        super(contextName);
        this.runnable = runnable;
        this.numberOfRetries = numberOfRetries;
        this.delayInMilliSeconds = delayInMilliSeconds;
    }

    /**
     * Creates a <code>RetrySessionRunner</code> that creates a session
     * from the session context with the given name.
     *
     * @param runnable            a <code>TransactionRunnable</code>
     * @param contextName         the context name
     * @param numberOfRetries     max number of retries
     * @param delayInMilliSeconds delay inbetween retries in milli seconds
     * @param sessionPolicy       the session policy
     * @since 1.1.0
     */
    public RetrySessionRunner(TransactionRunnable<T> runnable, String contextName, int numberOfRetries, long delayInMilliSeconds, SessionPolicy sessionPolicy) {
        super(contextName, sessionPolicy);
        this.runnable = runnable;
        this.numberOfRetries = numberOfRetries;
        this.delayInMilliSeconds = delayInMilliSeconds;
    }

    /**
     * @see SessionRunner#execute(Object[])
     */
    @Override
    public T execute(Object... arguments) throws SystemException {
        T result = null;
        int tries = 0;
        boolean success = false;
        while (!success && tries++ <= numberOfRetries) {
            try {
                result = super.execute(arguments);
                success = true;
            } catch (SystemException e) {
                if (tries > numberOfRetries) {
                    throw e;
                }
            }
            if (!success && delayInMilliSeconds > 0) {
                try {
                    Thread.sleep(delayInMilliSeconds);
                } catch (InterruptedException e) {
                }
            }
        }
        return result;
    }

    @Override
    protected T run(Connection connection, Object... arguments) throws SQLException {
        return runnable.run(connection, arguments);
    }
}
