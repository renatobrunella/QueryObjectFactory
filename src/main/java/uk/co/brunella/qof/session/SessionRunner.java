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


/**
 * The <code>SessionRunner</code> interface should be implemented for
 * code that runs in a session context. 
 * 
 * @param <T> the type of the result of the executed code. If no result is 
 *            returned this type should be <code>Void</code>
 * 
 * @see DefaultSessionRunner
 * @see RetrySessionRunner
 * @see BaseSessionRunner
 * 
 * @since 1.0.0
 */
public interface SessionRunner<T> {

  /**
   * A call to <code>execute</code> starts a new session and executes some code
   * in a transactional context.
   * 
   * If an exception is thrown by the executed code the
   * transaction is rolled back otherwise it is committed.
   * 
   * @param arguments arguments that are passed to the executed code
   * @return the result of the executed code
   * @throws SystemException thrown if an unexpected error occurred
   * 
   * @since 1.0.0
   */
  T execute(Object... arguments) throws SystemException;
}
