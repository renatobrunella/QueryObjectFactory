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
package sf.qof.adapter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * ClobReader is a helper class to read data from <code>Clob</code>s to a string.
 * 
 * @see java.sql.Clob
 */
public class ClobReader {

  /**
   * Reads data from a clob.
   * 
   * @param clob           the clob
   * @return               the clob data as string 
   * @throws SQLException  an error occurred
   * @see java.sql.Clob
   */
  public static String readClob(Clob clob) throws SQLException {
    Reader reader = clob.getCharacterStream();
    StringWriter writer = new StringWriter();
    char[] buf = new char[256];
    try {
      for (int n = reader.read(buf); n != -1; n = reader.read(buf)) {
        writer.write(buf, 0, n);
      }
      reader.close();
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return writer.toString();
  }

}
