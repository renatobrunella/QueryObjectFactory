/*
 * Copyright 2008 - 2010 brunella ltd
 *
 * Licensed under the GPL Version 3 (the "License");
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This is the default session connection handler.
 * 
 * It's default behaviour is to raise an exception if the connection returned
 * from the data source is null and to set auto commit to false.
 *
 * @since 1.0.0
 */
public class DefaultSessionConnectionHandler implements SessionConnectionHandler {

  private boolean setAutoCommitToFalse;

  public DefaultSessionConnectionHandler(boolean setAutoCommitToFalse) {
    this.setAutoCommitToFalse = setAutoCommitToFalse;
  }
  /**
   * Returns a new connection from the data source and sets auto commit to false.
   * 
   * @param dataSource the data source
   * @return a new connection
   * @throws SystemException
   *
   * @since 1.0.0            
   */
  public Connection getConnection(DataSource dataSource) throws SystemException {
    try {
      Connection connection = dataSource.getConnection();
      if (connection == null) {
        throw new SQLException("DataSource returned null connection");
      }
      if (setAutoCommitToFalse) {
        connection.setAutoCommit(false);
      }
      return connection;
    } catch (SQLException e) {
      throw new SystemException(e);
    }
  }

  /**
   * Closes a connection.
   * 
   * @param connection the connection
   * @throws SystemException
   *
   * @since 1.0.0            
   */
  public void closeConnection(Connection connection) throws SystemException {
    try {
      connection.close();
    } catch (SQLException e) {
      throw new SystemException(e);
    }
    
  }

}
