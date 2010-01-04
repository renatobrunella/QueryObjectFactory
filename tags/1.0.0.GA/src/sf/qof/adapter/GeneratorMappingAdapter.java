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
package sf.qof.adapter;

import sf.qof.mapping.ParameterMapping;
import sf.qof.mapping.ResultMapping;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;

/**
 * Interface for generator custom mappers.
 * 
 */
public interface GeneratorMappingAdapter extends MappingAdapter {
  /**
   * Method to map the current record of a result set or the result of a callable statement to 
   * an object on the stack. <code>indexes</code> defines the SQL indexes for the result set 
   * to be used in functions such a <code>getInt(index)</code>. 
   * 
   * @param resultMapping the result mapping
   * @param co          code emitter
   * @param result      <code>ResultSet</code> or <code>CallableStatement</code> local variable
   * @param indexes     array of SQL indexes
   */
  void generateFromResult(ResultMapping resultMapping, CodeEmitter co, Local result, int[] indexes);

  /**
   * Method to map the current record of a result set to an object on the stack.
   * <code>columns</code> defines the SQL column name for the result set to be used
   * in functions such a <code>getInt(name)</code>. 
   * 
   * @param resultMapping the result mapping
   * @param co          code emitter
   * @param resultSet   <code>ResultSet</code> local variable
   * @param columns     array of SQL column names
   */
  void generateFromResultSet(ResultMapping resultMapping, CodeEmitter co, Local resultSet, String[] columns);

  /**
   * Method to map the object on top of the stack to a prepared statement.
   * <code>indexes</code> defines the SQL index for the prepared statement to be used
   * in functions such a <code>setInt(index, value)</code>. 
   * 
   * @param parameterMapping    the parameter mapping
   * @param co                  code emitter
   * @param preparedStatement   prepared statement local variable
   * @param indexes             array of SQL indexes
   * @param indexOffset         offset to be added to the indexes if not null
   */
  void generateToPreparedStatement(ParameterMapping parameterMapping, CodeEmitter co, Local preparedStatement, int[] indexes, Local indexOffset);

  /**
   * Method to register an output parameter in a callable statement.
   * 
   * @param resultMapping      the result mapping
   * @param co                 code emitter
   * @param callableStatement  callable statement local variable
   * @param indexes            array of SQL indexes
   */
  void generateRegisterOutputParameters(ResultMapping resultMapping, CodeEmitter co, Local callableStatement, int[] indexes);
}
