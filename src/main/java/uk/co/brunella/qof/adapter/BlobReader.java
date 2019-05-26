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
package uk.co.brunella.qof.adapter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * BlobReader is a helper class to read data from <code>Blob</code>s to a byte-array.
 * 
 * @see Blob
 */
public class BlobReader {

  /**
   * Reads data from a blob.
   * 
   * @param blob           the blob
   * @return               the blob data as string 
   * @throws SQLException  an error occurred
   * @see Blob
   */
  public static byte[] readBlob(Blob blob) throws SQLException {
    InputStream bis = new BufferedInputStream(blob.getBinaryStream());
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      for (int n = bis.read(); n != -1; n = bis.read()) {
        os.write(n);
      }
      bis.close();
      byte[] bytes = os.toByteArray();
      os.close();
      return bytes;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
