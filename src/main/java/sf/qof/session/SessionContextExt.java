/*
 * Copyright 2010 brunella ltd
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
 * Extension interface for <code>SessionContext</code>.
 * 
 * Add a method to to start a session with a specified transaction management type.
 * 
 * @since 1.1.0
 */
public interface SessionContextExt extends SessionContext {

  /**
   * This method starts a session using the specified <code>TransactionManagementType</code>.
   *
   * It creates a new session for the calling thread and assigns a 
   * database connection to it and it creates a new transaction.
   * 
   * It must be called by the calling thread before any other method 
   * of this session context can be called.
   * 
   * @param transactionManagementType the transaction management type
   * @throws SystemException        Thrown if an unexpected error condition occurs
   * @throws IllegalStateException  Thrown if the session is already started
   * 
   * @since 1.1.0
   */
  void startSession(TransactionManagementType transactionManagementType) throws SystemException;
  
  /**
   * This method starts a session using the specified <code>TransactionManagementType</code>.
   *
   * It creates a new session for the calling thread and assigns a 
   * database connection to it and it creates a new transaction.
   * 
   * It must be called by the calling thread before any other method 
   * of this session context can be called.
   * 
   * Session policy can be specified to require a new session or to join
   * an existing one.
   * 
   * @param transactionManagementType the transaction management type
   * @param sessionPolicy             the session policy
   * @throws SystemException        Thrown if an unexpected error condition occurs
   * @throws IllegalStateException  Thrown if the session is already started
   * 
   * @since 1.1.0
   */
  void startSession(TransactionManagementType transactionManagementType, SessionPolicy sessionPolicy) throws SystemException;
  
  /**
   * This method starts a session.
   *
   * It creates a new session for the calling thread and assigns a 
   * database connection to it and it creates a new transaction.
   * 
   * It must be called by the calling thread before any other method 
   * of this session context can be called.
   * 
   * Session policy can be specified to require a new session or to join
   * an existing one.
   * 
   * @param sessionPolicy  The session policy
   * 
   * @throws SystemException        Thrown if an unexpected error condition occurs
   * @throws IllegalStateException  Thrown if the session is already started
   * 
   * @since 1.1.0
   */
  void startSession(SessionPolicy sessionPolicy) throws SystemException;
}
