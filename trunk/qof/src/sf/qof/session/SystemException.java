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
 * The SystemException is thrown by the transaction and session manager to
 * indicate that it has encountered an unexpected error condition that prevents
 * future transaction services from proceeding.
 * 
 */
public class SystemException extends Exception {

  private static final long serialVersionUID = -6158015852271864712L;

  /**
   * Constructs a SystemException object.
   */
  public SystemException() {
    super();
  }

  /**
   * Constructs a SystemException object.
   *
   * @param cause  a Throwable object
   */
  public SystemException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a SystemException object.
   *
   * @param message  the error message
   */
  public SystemException(String message) {
    super(message);
  }

  /**
   * Constructs a SystemException object.
   * 
   * @param message  the error message
   * @param cause    a Throwable object
   */
  public SystemException(String message, Throwable cause) {
    super(message, cause);
  }
}
