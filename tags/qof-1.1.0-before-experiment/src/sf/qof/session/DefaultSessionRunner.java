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
package sf.qof.session;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * <code>DefaultSessionRunner</code> is the default implementation that runs
 * a <code>TransactionRunnable</code> in a session context. 
 * 
 * Calling <code>execute</code> starts a new session, calls the <code>run</code>
 * method of the <code>TransactionRunnable</code> and commits or rolls back 
 * the transaction on success or failure.
 * 
 * <p>Typical usage is like this:</p>
 * 
 * <p><blockquote><pre>
 * List&lt;Person&gt; personList = ...
 * PersonUpdaterRunnable runnable = new PersonUpdaterRunnable();
 * Integer numberOfUpdates = 
 *   new DefaultSessionRunner&lt;Integer&gt;(runnable, "MY_CONTEXT_NAME").execute(personList);
 * </pre></blockquote></p>
 * 
 * @param <T> the type of the result of the <code>TransactionRunnable</code>. If
 *            no result is returned this type should be <code>Void</code>
 * 
 * @see SessionRunner
 */
public class DefaultSessionRunner<T> extends BaseSessionRunner<T> {

  private TransactionRunnable<T> runnable;
  
  /**
   * Creates a <code>DefaultSessionRunner</code> that creates a session
   * from the default session context.
   *
   * @param runnable a <code>TransactionRunnable</code>
   */
  public DefaultSessionRunner(TransactionRunnable<T> runnable) {
	this(runnable, SessionContext.DEFAULT_CONTEXT_NAME);
  }

  /**
   * Creates a <code>DefaultSessionRunner</code> that creates a session
   * from the session context with the given name.
   * 
   * @param runnable a <code>TransactionRunnable</code>
   * @param contextName the context name
   */
  public DefaultSessionRunner(TransactionRunnable<T> runnable, String contextName) {
	super(contextName);
	this.runnable = runnable;
  }

  @Override
  protected T run(Connection connection, Object... arguments) throws SQLException {
	return runnable.run(connection, arguments);
  }
}
