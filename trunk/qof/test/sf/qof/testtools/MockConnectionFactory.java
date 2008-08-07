/*
 * Copyright 2007 brunella ltd
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
package sf.qof.testtools;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockConnectionFactory {

  public static Connection getConnection() {
    MockConnection mockConnection = new MockConnection();
    LoggingDelegationProxy proxy = (LoggingDelegationProxy) LoggingDelegationProxyFactory.createProxy(mockConnection,
        Connection.class, MockConnectionData.class);
    mockConnection.setProxy(proxy);
    return (Connection) proxy;
  }

  private static class MockConnection {

    private LoggingDelegationProxy proxy;
    private List<Object> resultData;
    private List<Map<String, Object>> resultSetData = new ArrayList<Map<String, Object>>();
    private int resultSetDataIndex = -1;

    private void setProxy(LoggingDelegationProxy proxy) {
      this.proxy = proxy;
    }

    public void setResultSetData(List<Map<String, Object>> resultSetData) {
      this.resultSetData = resultSetData;
      this.resultSetDataIndex = -1;
      proxy.clearLog();
    }

    public void setResultData(List<Object> resultData) {
      this.resultData = resultData;
      proxy.clearLog();
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
      MockCallableStatement mockCallableStatement = new MockCallableStatement();
      LoggingDelegationProxy proxy = (LoggingDelegationProxy) LoggingDelegationProxyFactory.createProxy(this.proxy,
          mockCallableStatement, CallableStatement.class);
      mockCallableStatement.setProxy(proxy);
      return (CallableStatement) proxy;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
      MockCallableStatement mockCallableStatement = new MockCallableStatement();
      LoggingDelegationProxy proxy = (LoggingDelegationProxy) LoggingDelegationProxyFactory.createProxy(this.proxy,
          mockCallableStatement, PreparedStatement.class);
      mockCallableStatement.setProxy(proxy);
      return (PreparedStatement) proxy;
    }

    private class MockCallableStatement {
      private LoggingDelegationProxy proxy;

      private void setProxy(LoggingDelegationProxy proxy) {
        this.proxy = proxy;
      }

      public boolean getBoolean(int parameterIndex) throws SQLException {
        return ((Boolean) getIndexedValue(parameterIndex - 1)).booleanValue();
      }

      public boolean getBoolean(String parameterName) throws SQLException {
        return ((Boolean) getIndexedValue(Integer.valueOf(parameterName))).booleanValue();
      }

      private Object getIndexedValue(int index) throws SQLException {
        if (index < resultData.size()) {
          Object obj = resultData.get(index);
          wasNull = obj == null;
          return obj;
        } else {
          throw new SQLException("No more rows");
        }
      }

      public byte getByte(int parameterIndex) throws SQLException {
        return ((Byte) getIndexedValue(parameterIndex - 1)).byteValue();
      }

      public byte getByte(String parameterName) throws SQLException {
        return ((Byte) getIndexedValue(Integer.valueOf(parameterName))).byteValue();
      }

      public Date getDate(int parameterIndex) throws SQLException {
        return (java.sql.Date) getIndexedValue(parameterIndex - 1);
      }

      public Date getDate(String parameterName) throws SQLException {
        return (java.sql.Date) getIndexedValue(Integer.valueOf(parameterName));
      }

      public double getDouble(int parameterIndex) throws SQLException {
        return ((Double) getIndexedValue(parameterIndex - 1)).doubleValue();
      }

      public double getDouble(String parameterName) throws SQLException {
        return ((Double) getIndexedValue(Integer.valueOf(parameterName))).doubleValue();
      }

      public float getFloat(int parameterIndex) throws SQLException {
        return ((Float) getIndexedValue(parameterIndex - 1)).floatValue();
      }

      public float getFloat(String parameterName) throws SQLException {
        return ((Float) getIndexedValue(Integer.valueOf(parameterName))).floatValue();
      }

      public int getInt(int parameterIndex) throws SQLException {
        return ((Integer) getIndexedValue(parameterIndex - 1)).intValue();
      }

      public int getInt(String parameterName) throws SQLException {
        return ((Integer) getIndexedValue(Integer.valueOf(parameterName))).intValue();
      }

      public long getLong(int parameterIndex) throws SQLException {
        return ((Long) getIndexedValue(parameterIndex - 1)).longValue();
      }

      public long getLong(String parameterName) throws SQLException {
        return ((Long) getIndexedValue(Integer.valueOf(parameterName))).longValue();
      }

      public short getShort(int parameterIndex) throws SQLException {
        return ((Short) getIndexedValue(parameterIndex - 1)).shortValue();
      }

      public short getShort(String parameterName) throws SQLException {
        return ((Short) getIndexedValue(Integer.valueOf(parameterName))).shortValue();
      }

      public String getString(int parameterIndex) throws SQLException {
        return (String) getIndexedValue(parameterIndex - 1);
      }

      public String getString(String parameterName) throws SQLException {
        return (String) getIndexedValue(Integer.valueOf(parameterName));
      }

      public Time getTime(int parameterIndex) throws SQLException {
        return (java.sql.Time) getIndexedValue(parameterIndex - 1);
      }

      public Time getTime(String parameterName) throws SQLException {
        return (java.sql.Time) getIndexedValue(Integer.valueOf(parameterName));
      }

      public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return (java.sql.Timestamp) getIndexedValue(parameterIndex - 1);
      }

      public Timestamp getTimestamp(String parameterName) throws SQLException {
        return (java.sql.Timestamp) getIndexedValue(Integer.valueOf(parameterName));
      }

      public Clob getClob(int parameterIndex) throws SQLException {
        return (Clob) LoggingDelegationProxyFactory.createProxy(
            new MockClob((String)getIndexedValue(parameterIndex - 1)), Clob.class);
      }

      public Blob getBlob(int parameterIndex) throws SQLException {
        return (Blob) LoggingDelegationProxyFactory.createProxy(
            new MockBlob((byte[])getIndexedValue(parameterIndex - 1)), Blob.class);
      }

      private boolean wasNull;

      public boolean wasNull() throws SQLException {
        return wasNull;
      }

      private int batches = 0;

      public void addBatch() throws SQLException {
        batches++;
      }

      public ResultSet executeQuery() throws SQLException {
        MockResultSet mockCallableStatement = new MockResultSet();
        return (ResultSet) LoggingDelegationProxyFactory
            .createProxy(this.proxy, mockCallableStatement, ResultSet.class);
      }

      public boolean execute() throws SQLException {
        return false;
      }

      public int executeUpdate() throws SQLException {
        return 1;
      }

      public int[] executeBatch() throws SQLException {
        int[] result = new int[batches];
        for (int i = 0; i < result.length; i++) {
          result[i] = i + 1;
        }
        batches = 0;
        return result;
      }

    }

    private class MockResultSet {
      public boolean getBoolean(int columnIndex) throws SQLException {
        return ((Boolean) getIndexedValue(columnIndex)).booleanValue();
      }

      public boolean getBoolean(String columnName) throws SQLException {
        return ((Boolean) getNamedValue(columnName)).booleanValue();
      }

      public byte getByte(int columnIndex) throws SQLException {
        return ((Byte) getIndexedValue(columnIndex)).byteValue();
      }

      public byte getByte(String columnName) throws SQLException {
        return ((Byte) getNamedValue(columnName)).byteValue();
      }

      public Date getDate(int columnIndex) throws SQLException {
        return (Date) getIndexedValue(columnIndex);
      }

      public Date getDate(String columnName) throws SQLException {
        return (Date) getNamedValue(columnName);
      }

      public double getDouble(int columnIndex) throws SQLException {
        return ((Double) getIndexedValue(columnIndex)).doubleValue();
      }

      public double getDouble(String columnName) throws SQLException {
        return ((Double) getNamedValue(columnName)).doubleValue();
      }

      public float getFloat(int columnIndex) throws SQLException {
        return ((Float) getIndexedValue(columnIndex)).floatValue();
      }

      public float getFloat(String columnName) throws SQLException {
        return ((Float) getNamedValue(columnName)).floatValue();
      }

      public int getInt(int columnIndex) throws SQLException {
        return ((Integer) getIndexedValue(columnIndex)).intValue();
      }

      public int getInt(String columnName) throws SQLException {
        return ((Integer) getNamedValue(columnName)).intValue();
      }

      public long getLong(int columnIndex) throws SQLException {
        return ((Long) getIndexedValue(columnIndex)).longValue();
      }

      public long getLong(String columnName) throws SQLException {
        return ((Long) getNamedValue(columnName)).longValue();
      }

      public short getShort(int columnIndex) throws SQLException {
        return ((Short) getIndexedValue(columnIndex)).shortValue();
      }

      public short getShort(String columnName) throws SQLException {
        return ((Short) getNamedValue(columnName)).shortValue();
      }

      public String getString(int columnIndex) throws SQLException {
        return (String) getIndexedValue(columnIndex);
      }

      public String getString(String columnName) throws SQLException {
        return (String) getNamedValue(columnName);
      }

      public Time getTime(int columnIndex) throws SQLException {
        return (Time) getIndexedValue(columnIndex);
      }

      public Time getTime(String columnName) throws SQLException {
        return (Time) getNamedValue(columnName);
      }

      public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return (Timestamp) getIndexedValue(columnIndex);
      }

      public Timestamp getTimestamp(String columnName) throws SQLException {
        return (Timestamp) getNamedValue(columnName);
      }

      public Clob getClob(String columnName) throws SQLException {
        return (Clob) LoggingDelegationProxyFactory.createProxy(
            new MockClob((String)getNamedValue(columnName)), Clob.class);
      }

      public Blob getBlob(String columnName) throws SQLException {
        return (Blob) LoggingDelegationProxyFactory.createProxy(
            new MockBlob((byte[])getNamedValue(columnName)), Blob.class);
      }
      
      public boolean next() throws SQLException {
        resultSetDataIndex++;
        if (resultSetData == null) {
          throw new SQLException("No data specified");
        }
        return resultSetDataIndex < resultSetData.size();
      }

      private boolean wasNull;

      public boolean wasNull() throws SQLException {
        return wasNull;
      }

      private Object getIndexedValue(int columnIndex) throws SQLException {
        if (resultSetDataIndex < resultSetData.size()) {
          Map<String, Object> row = resultSetData.get(resultSetDataIndex);
          if (row.containsKey(String.valueOf(columnIndex))) {
            Object obj = row.get(String.valueOf(columnIndex));
            wasNull = obj == null;
            return obj;
          } else {
            throw new SQLException("Invalid column name " + columnIndex);
          }
        } else {
          throw new SQLException("No more rows");
        }
      }

      private Object getNamedValue(String column) throws SQLException {
        if (resultSetDataIndex < resultSetData.size()) {
          Map<String, Object> row = resultSetData.get(resultSetDataIndex);
          if (row.containsKey(column)) {
            Object obj = row.get(column);
            wasNull = obj == null;
            return obj;
          } else {
            throw new SQLException("Invalid column name " + column);
          }
        } else {
          throw new SQLException("No more rows");
        }
      }
    }
    
    private static class MockClob {

      private String data;
      
      public MockClob(String data) {
        this.data = data;
      }

      public java.io.Reader getCharacterStream() throws SQLException {
        return new java.io.StringReader(data);
      }
    }
    
    private static class MockBlob {

      private byte[] data;
      
      public MockBlob(byte[] data) {
        this.data = data;
      }
      
      public java.io.InputStream getBinaryStream () throws SQLException {
         return new java.io.ByteArrayInputStream(data);
      }
    }
  }
}
