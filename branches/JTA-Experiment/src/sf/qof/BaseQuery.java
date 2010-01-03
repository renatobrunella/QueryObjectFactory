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
package sf.qof;

import java.sql.Connection;

/**
 * Defines the basic methods to get and set connections, batch size and fetch size for 
 * query objects. <code>BaseQuery</code> is implemented by all query objects.   
 * 
 */
public interface BaseQuery {
  
  /**
   * Set the connection to be used in the query object.
   * 
   * @param connection the connection
   * 
   * @see #getConnection
   */
  void setConnection(Connection connection);

  /**
   * Returns the connection used in the query object.
   * 
   * @return the connection
   * 
   * @see #setConnection(Connection)
   * @see #ungetConnection(Connection)
   */
  Connection getConnection();

  /**
   * This method is called to return a connection after a <code>getConnection</code> call.
   * 
   * @param connection the connection
   * 
   * @see #getConnection()
   */
  void ungetConnection(Connection connection);
  
  /**
   * Sets the fetch size to be used in select queries.
   * 
   * <p> If the query definition class or interface defines the static field <code>DEFAULT_FETCH_SIZE</code>
   * then batch size will <code>QueryObjectGenerator.DEFAULT_FETCH_SIZE</code>
   * 
   * <p> <blockquote><pre>
   *    public static final int DEFAULT_FETCH_SIZE = 100;
   * </pre></blockquote>
   * @param size fetch size
   * 
   * @see sf.qof.codegen.QueryObjectGenerator#DEFAULT_FETCH_SIZE
   * @see #getFetchSize()
   */
  void setFetchSize(int size);

  /**
   * Returns the fetch size.
   * 
   * @return fetch size
   * @see #setFetchSize(int)
   */
  int getFetchSize();

  /**
   * Sets the batch size to be used in insert, update, delete and call queries.
   * 
   * <p> A batch size of 0 disables batch processing.
   * 
   * <p> If the query definition class or interface defines the static field <code>DEFAULT_BATCH_SIZE</code>
   * then batch size will <code>QueryObjectGenerator.DEFAULT_BATCH_SIZE</code>
   * 
   * <p> <blockquote><pre>
   *    public static final int DEFAULT_BATCH_SIZE = 100;
   * </pre></blockquote>
   * 
   * @param size batch size
   * 
   * @see sf.qof.codegen.QueryObjectGenerator#DEFAULT_BATCH_SIZE
   * @see #getBatchSize()
   */
  void setBatchSize(int size);

  /**
   * Returns the batch size.
   * 
   * @return batch size
   * @see #setBatchSize(int)
   */
  int getBatchSize();
}
