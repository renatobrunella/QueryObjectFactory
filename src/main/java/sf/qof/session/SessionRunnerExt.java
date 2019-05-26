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
 * The <code>SessionRunnerExt</code> interface extends <code>SessionRunner</code>
 * and adds new methods that allow to specify the transaction management type. 
 * 
 * @param <T> the type of the result of the executed code. If no result is 
 *            returned this type should be <code>Void</code>
 * 
 * @since 1.1.0
 * 
 * @see SessionRunner
 */
public interface SessionRunnerExt<T> extends SessionRunner<T> {

  /**
   * A call to <code>executeContainerManaged</code> starts a new session and 
   * executes some code in a transactional context using container managed 
   * transaction management.
   * 
   * If an exception is thrown by the executed code the
   * transaction is rolled back otherwise it is committed.
   * 
   * @param arguments arguments that are passed to the executed code
   * @return the result of the executed code
   * @throws SystemException thrown if an unexpected error occurred
   * 
   * @since 1.1.0
   */
  T executeContainerManaged(Object... arguments) throws SystemException;

  /**
   * A call to <code>executeBeanManaged</code> starts a new session and 
   * executes some code in a transactional context using bean managed 
   * transaction management.
   * 
   * If an exception is thrown by the executed code the
   * transaction is rolled back otherwise it is committed.
   * 
   * @param arguments arguments that are passed to the executed code
   * @return the result of the executed code
   * @throws SystemException thrown if an unexpected error occurred
   * 
   * @since 1.1.0
   */
  T executeBeanManaged(Object... arguments) throws SystemException;

}
