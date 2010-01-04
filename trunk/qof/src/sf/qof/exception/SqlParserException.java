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
package sf.qof.exception;

/**
 * A parsing exception.
 * Thrown by the <code>SqlParser</code>
 *
 * @see sf.qof.parser.SqlParser
 */
public class SqlParserException extends RuntimeException {

  private static final long serialVersionUID = -4719857605238942051L;
  
  private int start;
  private int length;

  /**
   * Creates a SqlParserException.
   * 
   * @param message  the error message
   */
  public SqlParserException(String message) {
    super(message);
  }
  
  /**
   * Creates a SqlParserException.
   * 
   * @param message  the error message
   * @param start    start position in SQL statement
   * @param length   length of error in SQL statement
   */
  public SqlParserException(String message, int start, int length) {
    super(message);
    this.start = start;
    this.length = length;
  }

  /**
   * Returns start position of error in SQL statement.
   * 
   * @return start position
   */
  public int getStart() {
    return start;
  }

  /**
   * Returns length of error in SQL statement.
   * 
   * @return length
   */
  public int getLength() {
    return length;
  }
  
}
