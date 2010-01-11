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

/**
 * Defines the methods that allow an application to explicitly manage 
 * transaction boundaries.
 * 
 * <p>A <code>UserTransaction</code> can be retrieved from the session that 
 * is associated to the current thread from the session context:</p>
 *
 * <p><blockquote><pre>
 * UserTransaction trx = SessionContextFactory.getContext().getUserTransaction();
 * </pre></blockquote></p>
 * 
 * @see SessionContext#getUserTransaction()
 * @see SessionContextFactory
 */
public interface UserTransaction {

  /**
   * Create a new transaction and associate it with the current thread.
   * 
   * @throws SystemException
   *             Thrown if the transaction manager encounters an unexpected
   *             error condition.
   * @throws IllegalStateException
   *             Thrown if the transaction is already started
   */
  void begin() throws SystemException;

  /**
   * Complete the transaction associated with the current thread. When this
   * method completes, the thread is no longer associated with a transaction.
   * 
   * @throws SystemException
   *             Thrown if the transaction manager encounters an unexpected
   *             error condition.
   * @throws RollbackException
   *             Thrown to indicate that the transaction has been rolled back
   *             rather than committed.
   * @throws IllegalStateException
   *             Thrown if there the transaction was not started
   */
  void commit() throws SystemException, RollbackException;

  /**
   * Roll back the transaction associated with the current thread. When this
   * method completes, the thread is no longer associated with a transaction.
   * 
   * @throws SystemException
   *             Thrown if the transaction manager encounters an unexpected
   *             error condition.
   * @throws IllegalStateException
   *             Thrown if there the transaction was not started
   */
  void rollback() throws SystemException;

  /**
   * Modify the transaction associated with the current thread such that the
   * only possible outcome of the transaction is to roll back the transaction.
   * 
   * @throws SystemException
   *             Thrown if the transaction manager encounters an unexpected
   *             error condition.
   * @throws IllegalStateException
   *             Thrown if there the transaction was not started
   */
  void setRollbackOnly() throws SystemException;

  /**
   * Returns true if the only possible outcome of the transaction that is
   * associated with the current thread is to roll back the transaction.
   * 
   * @return true if current transaction can only be rolled back
   *
   * @throws IllegalStateException
   *             Thrown if there the transaction was not started
   */
  boolean isRollbackOnly();
}
