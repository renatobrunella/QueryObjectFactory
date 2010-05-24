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
package sf.qof.util;

/**
 * Helper class with utility method to replace in-clause parameters.
 */
public class InClauseParameterReplacer {

  InClauseParameterReplacer() {}
  
  /**
   * Returns a SQL statement that replaces the <code>index</code>-th 
   * parameter '?' with a number <code>numArgs</code> of '?'.
   * 
   * @param sql     the SQL statement
   * @param index   the index of the parameter to replace
   * @param numArgs number of parameters required
   * @return        new SQL statement with additional parameters
   */
  public static String replace(String sql, int index, int numArgs) {
    return replace(sql, index, numArgs, ",");
  }
  
  /**
   * Returns a SQL statement that replaces the <code>index</code>-th 
   * parameter '?' with a number <code>numArgs</code> of '?'.
   * 
   * @param sql     the SQL statement
   * @param index   the index of the parameter to replace
   * @param numArgs number of parameters required
   * @param separator the separator used between arguments
   * @return        new SQL statement with additional parameters
   */
  public static String replace(String sql, int index, int numArgs, String separator) {
    StringBuilder sb = new StringBuilder(sql.length() + (numArgs - 1) * 2);
    int currentIndex = 0;
    boolean replaced = false;
    int i = 0;
    while (i < sql.length()) {
      char c = sql.charAt(i);
      sb.append(c);
      if (!replaced) {
        if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
          // skip until end of comment
          i += 2;
          sb.append('*');
          while (i < sql.length()) {
            c = sql.charAt(i);
            sb.append(c);
            if (c == '*' && i + 1 < sql.length() && sql.charAt(i + 1) == '/') {
              sb.append('/');
              i++;
              break;
            }
            i++;
          }
        }
        if (c == '?') {
          currentIndex++;
          if (index == currentIndex) {
            for (int j = 0; j < numArgs - 1; j++) {
              sb.append(separator).append('?');
            }
            replaced = true;
          }
        }
      }
      i++;
    }
    return sb.toString();
  }

}
