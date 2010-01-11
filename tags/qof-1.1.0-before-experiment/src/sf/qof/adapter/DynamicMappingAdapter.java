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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Interface for dynamic custom mappers.
 * 
 */
public interface DynamicMappingAdapter extends MappingAdapter {

  /**
   * Method to map the object <code>value</code> to a prepared statement.
   * <code>indexes</code> defines the SQL index for the prepared statement to be used
   * in functions such a <code>setInt(index, value)</code>. 
   * 
   * @param ps      prepared statement              
   * @param value   value object
   * @param indexes array of SQL indexes
   * 
   * @throws SQLException
   */
  void set(PreparedStatement ps, Object value, int[] indexes) throws SQLException;

  /**
   * Method to register an out parameter for a callable statement.
   * 
   * @param cs            callable statement
   * @param indexes       array of SQL indexes
   * 
   * @throws SQLException
   */
  void registerOutputParameter(CallableStatement cs, int[] indexes) throws SQLException;
  
  /**
   * Method to map the current record of a result set to the returned object.
   * <code>indexes</code> defines the SQL indexes for the result set to be used
   * in functions such a <code>getInt(index)</code>. 
   * 
   * @param rs      result set
   * @param indexes array of SQL indexes
   * @return        newly created object
   * @throws SQLException
   */
  Object get(ResultSet rs, int[] indexes) throws SQLException;

  /**
   * Method to map the current record of a result set to the returned object.
   * <code>columns</code> defines the SQL column name for the result set to be used
   * in functions such a <code>getInt(name)</code>. 
   * 
   * @param rs      result set
   * @param columns array of SQL column names
   * @return        newly created object
   * @throws SQLException
   */
  Object get(ResultSet rs, String[] columns) throws SQLException;
  
  /**
   * Method to map the result of a callable statement to the returned object.
   * <code>indexes</code> defines the SQL indexes for the result set to be used
   * in functions such a <code>getInt(index)</code>. 
   * 
   * @param cs      callable statement
   * @param indexes array of SQL indexes
   * @return        newly created object
   * @throws SQLException
   */
  Object get(CallableStatement cs, int[] indexes) throws SQLException;

}
