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

/**
 * Abstract base class for implementing a <code>SessionRunner</code>.
 * 
 * <p>Implements the <code>execute</code> method that has the following
 * behaviour: 
 * <br>Start a new session, start a new transaction and call
 * <code>run</code>. If no exception occured and the transaction is not 
 * in rollback only state then commit the transaction, stop the session
 * and return the result of <code>run</code>. Otherwise rollback the 
 * transaction, stop the session and re-throw the exception wrapped
 * in a <code>SystemException</code>.</p>
 * 
 * <p><code>BaseSessionRunner</code> is used by default implementations
 * of <code>SessionRunner</code> that use a <code>TransactionRunnable</code>
 * to execute queries but it can also be used directly:</p>
 * 
 * <p><blockquote><pre>
 * String result = new BaseSessionRunner&lt;String&gt;("MY_CONTEXT_NAME") {
 *   protected String run(Connection connection, Object... arguments) throws SQLException {
 *     PreparedStatement ps = connection.prepareStatement("select name from person where id = ?");
 *     String result = null;
 *     try {
 *       ps.setInt(1, (Integer)arguments[0]);
 *       ResultSet rs = ps.executeQuery();
 *       if (rs.next()) {
 *         result = rs.getString(1);
 *       }
 *       rs.close();
 *     } finally {
 *       ps.close();
 *     }
 *     return result;
 *   }
 * }.execute(55);
 * </pre></blockquote></p>
 *
 * @param <T> the type of the result of the <code>run</code> method.
 */
public abstract class BaseSessionRunner<T> implements SessionRunner<T> {

  /**
   * Holds the current session context.
   */
  protected SessionContext sessionContext;

  /**
   * Creates a <code>BaseSessionRunner</code> that creates a session
   * from the default session context.
   *
   */
  public BaseSessionRunner() {
	this(SessionContext.DEFAULT_CONTEXT_NAME);
  }

  /**
   * Creates a <code>BaseSessionRunner</code> that creates a session
   * from the session context with the given name.
   * 
   * @param contextName the context name
   */
  public BaseSessionRunner(String contextName) {
	sessionContext = SessionContextFactory.getContext(contextName);
  }

  /**
    * @see SessionRunner#execute(Object[])
    */
  public T execute(Object... arguments) throws SystemException {
	T result;
	sessionContext.startSession();
	try {
	  sessionContext.getUserTransaction().begin();
	  try {
		result = run(sessionContext.getConnection(), arguments);
	  } catch (Throwable e) {
		try {
		  sessionContext.getUserTransaction().rollback();
		} catch (SystemException se) {
		  // ignore - nothing we can do about
		}
		throw new SystemException(e);
	  }
	  if (sessionContext.getUserTransaction().isRollbackOnly()) {
		sessionContext.getUserTransaction().rollback();
	  } else {
		try {
		  sessionContext.getUserTransaction().commit();
		} catch (RollbackException re) {
		  // can't happen
		}
	  }
	} finally {
	  sessionContext.stopSession();
	}
	return result;
  }
  
  /**
   * This method is called once during the call to <code>execute</code>.
   * It must be overridden and should contain the code that needs to be
   * run in a transaction context.
   * 
   * @param connection     the thread's current database connection 
   * @param arguments      arguments passed to the <code>execute</code> method 
   * @return               a result
   * @throws SQLException  throw this if an error occured. This will cause a rollback
   *                       of the current transaction.
   */
  protected abstract T run(Connection connection, Object... arguments) throws SQLException;
}
