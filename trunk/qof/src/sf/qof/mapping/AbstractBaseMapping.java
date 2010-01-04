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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import sf.qof.adapter.MappingAdapter;

public abstract class AbstractBaseMapping implements ParameterMapping, ResultMapping {
  // SQL mappings
  protected int[] sqlIndexes;
  protected String[] sqlColumns;
  // Java mappings
  protected int index;
  protected Class<?> type;
  protected Class<?> collectionType;
  protected Class<?> mapKeyType;
  protected Class<?> beanType;
  protected Method method;
  protected MappingAdapter adapter;
  protected Integer constructorParameter;
  protected Constructor<?> constructor;
  protected boolean usesArray;

  // implements ParameterMapping
  public void setParameters(int index, Class<?> type, Class<?> collectionType, Class<?> beanType, Method getter,
      int[] sqlIndexes, String[] sqlColumns, MappingAdapter adapter, boolean usesArray) {
    this.index = index;
    this.type = type;
    this.collectionType = collectionType;
    this.beanType = beanType;
    this.method = getter;
    this.sqlIndexes = sqlIndexes;
    this.sqlColumns = sqlColumns;
    this.adapter = adapter;
    this.usesArray = usesArray;
  }

  // implements ResultMapping
  public void setParameters(Class<?> type, Class<?> collectionType, Class<?> beanType, Method setter, int[] sqlIndexes,
      String[] sqlColumns, MappingAdapter adapter, Class<?> mapKeyType, Integer constructorParameter, Constructor<?> constructor) {
    this.index = -1;
    this.type = type;
    this.collectionType = collectionType;
    this.beanType = beanType;
    this.method = setter;
    this.sqlIndexes = sqlIndexes;
    this.sqlColumns = sqlColumns;
    this.adapter = adapter;
    this.mapKeyType = mapKeyType;
    this.constructorParameter = constructorParameter;
    this.constructor = constructor;
  }

  public Class<?> getBeanType() {
    return beanType;
  }

  public Class<?> getCollectionType() {
    return collectionType;
  }

  public int getIndex() {
    return index;
  }

  public Method getGetter() {
    return method;
  }

  public Method getSetter() {
    return method;
  }

  public String[] getSqlColumns() {
    return sqlColumns;
  }

  public int[] getSqlIndexes() {
    return sqlIndexes;
  }

  public Class<?> getType() {
    return type;
  }

  public MappingAdapter getAdapter() {
    return adapter;
  }

  public boolean usesCollection() {
    return collectionType != null;
  }

  public boolean usesAtomic() {
    return beanType == null && constructor == null;
  }

  public boolean isMapKey() {
    return mapKeyType != null;
  }
  
  public Class<?> getMapKeyType() {
    return mapKeyType;
  }
  
  public Integer getConstructorParameter() {
	return constructorParameter;
  }

  public Constructor<?> getConstructor() {
    return constructor;
  }
  
  public boolean usesArray() {
    return usesArray;
  }

  private String getSqlColumnsString() {
    if (sqlColumns != null) {
      StringBuffer sb = new StringBuffer();
      sb.append('[');
      for (int i = 0; i < sqlColumns.length; i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append('"').append(sqlColumns[i]).append('"');
      }
      sb.append(']');
      return sb.toString();
    } else {
      return "";
    }
  }

  private String getSqlIndexesString() {
    if (sqlIndexes != null) {
      StringBuffer sb = new StringBuffer();
      sb.append('[');
      for (int i = 0; i < sqlIndexes.length; i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(sqlIndexes[i]);
      }
      sb.append(']');
      return sb.toString();
    } else {
      return "";
    }
  }

  private String stripType(Class<?> type) {
    String typeName = type.getName();
    typeName = typeName.replace("java.lang.", "");
    typeName = typeName.replace("java.util.", "");
    typeName = typeName.replace("sf.qof.mapping.AbstractNumberMapping$", "");
    typeName = typeName.replace("sf.qof.mapping.AbstractCharacterMapping$", "");
    typeName = typeName.replace("sf.qof.mapping.AbstractDateTimeMapping$", "");
    return typeName;
  }
  
  public String parameterMappingInfo() {
    StringBuffer sb = new StringBuffer();
    sb.append("Parameter: ").append(stripType(this.getClass())).append('\n');
    sb.append('\t').append(getSqlColumnsString()).append(getSqlIndexesString());
    sb.append(" = parameter ").append(index).append(' ');
    if (method != null) {
      sb.append(method);
    } else {
      sb.append(stripType(type));
    }
    if (adapter != null) {
      sb.append("\tuse adapter ").append(adapter.getClass());
    }

    return sb.toString();
  }

  public String resultMappingInfo() {
    StringBuffer sb = new StringBuffer();
    sb.append("Result: ").append(stripType(this.getClass())).append('\n');
    sb.append('\t').append(getSqlColumnsString()).append(getSqlIndexesString());
    sb.append(" => ");
    if (method != null) {
      sb.append(method);
    } else {
      sb.append(stripType(type));
    }
    if (adapter != null) {
      sb.append("\n\tuse adapter ").append(adapter.getClass());
    }
    if (constructorParameter != null) {
      sb.append("\tuse constructor ").append(constructorParameter);
    }

    return sb.toString();
  }
}
