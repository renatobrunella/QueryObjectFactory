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
 * The <code>TransactionRunnable</code> interface should be implemented for
 * code that needs to be run in a transactional context. 
 *
 * <p>Typical usage is like this:</p>
 * 
 * <p><blockquote><pre>
 * TransactionRunnable&lt;Void&gt; runnable = new TransactionRunnable&lt;Void&gt;() {
 * 	 public Void run(Connection connection, Object... arguments) throws SQLException {
 *     List&lt;Person&gt; personList = (List&lt;Person&gt;)arguments[0];
 *     PreparedStatement ps = connection.prepareStatement("...");
 *     ...
 *     return null;
 *   }
 * };
 * </pre></blockquote></p>
 * 
 * @param <T> the type of the result of a call to run. If no result is 
 *            returned this type should be <code>Void</code>
 * 
 * @see SessionRunner
 */
public interface TransactionRunnable<T> {

  /**
   * This method is called by a <code>SessionRunner</code>.
   * 
   * @param connection      the database connection
   * @param arguments       a variable list of arguments
   * @return                a result
   * @throws SQLException   thrown if an exception occured. 
   *                        This will force a rollback of the transaction
   * 
   * @see SessionRunner
   */
  T run(Connection connection, Object... arguments) throws SQLException;

}
