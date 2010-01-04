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
package sf.qof.mapping;

import java.lang.reflect.Method;

import sf.qof.adapter.MappingAdapter;

/**
 * Mapping interface for parameter mappings.
 * 
 */
public interface ParameterMapping extends Mapping {

  /**
   * Initializer.
   * 
   * @param index           method parameter index
   * @param type            mapping type
   * @param collectionType  collection type of method parameter
   * @param beanType        Java Bean object type of method parameter
   * @param getter          getter form Java Bean object
   * @param sqlIndexes      array of SQL indexes
   * @param sqlColumns      array of SQL column names
   * @param adapter         custom mapping adapter
   * @param usesArray       parameter is an array
   */
  void setParameters(int index, Class<?> type, Class<?> collectionType, Class<?> beanType, Method getter, int[] sqlIndexes,
      String[] sqlColumns, MappingAdapter adapter, boolean usesArray);

  /**
   * Returns the method parameter index.
   * 
   * @return method parameter index
   */
  int getIndex();

  /**
   * Returns mapping type.
   * 
   * @return mapping type
   */
  Class<?> getType();

  /**
   * Returns the collection type of the method parameter or null. 
   * This needs to implement <code>Collection</code>.
   * 
   * @return collection type of the method parameter
   */
  Class<?> getCollectionType();

  /**
   * Returns the type of the method parameter Java Bean or null.
   * 
   * @return bean type
   */
  Class<?> getBeanType();

  /**
   * Returns the getter of the Java Bean or null.
   * 
   * @return getter
   */
  Method getGetter();

  /**
   * Returns an array with the column names in the SQL statement.
   * 
   * @return array of SQL column names
   */
  String[] getSqlColumns();

  /**
   * Returns an array with the indexes in the SQL statement.
   * 
   * @return array of SQL indexes
   */
  int[] getSqlIndexes();

  /**
   * Returns a custom mapping adapter.
   * 
   * @return mapping adapter
   */
  MappingAdapter getAdapter();

  /**
   * True if the method parameter is a collection type.
   * 
   * @return uses collection type
   */
  boolean usesCollection();

  /**
   * True if the method parameter is either a primitive or an 
   * atomic value (like <code>String</code>).
   * 
   * @return uses an atomic value
   */
  boolean usesAtomic();

  /**
   * True if the parameter is an array.
   * 
   * @return uses an array
   */
  boolean usesArray();
}
