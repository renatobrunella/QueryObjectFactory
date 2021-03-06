/*
 * Copyright 2007 - 2011 brunella ltd
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
package uk.co.brunella.qof.mapping;

import uk.co.brunella.qof.adapter.MappingAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class AbstractBaseMapping implements ParameterMapping, ResultMapping {
    // SQL mappings
    private int[] sqlIndexes;
    private String[] sqlColumns;
    // Java mappings
    protected int index;
    protected Class<?> type;
    private Class<?> collectionType;
    private Class<?> mapKeyType;
    private Class<?> beanType;
    private Method[] getters;
    private Method setter;
    protected MappingAdapter adapter;
    private Integer constructorParameter;
    protected Constructor<?> constructor;
    private Method staticFactoryMethod;
    private Class<?> collectionClass;
    private int collectionInitialCapacity;
    private boolean usesArray;
    private String parameterSeparator;

    // implements ParameterMapping
    public void setParameters(int index, Class<?> type, Class<?> collectionType, Class<?> beanType, Method[] getters,
                              int[] sqlIndexes, String[] sqlColumns, MappingAdapter adapter, boolean usesArray, String parameterSeparator) {
        this.index = index;
        this.type = type;
        this.collectionType = collectionType;
        this.beanType = beanType;
        this.getters = getters;
        this.sqlIndexes = sqlIndexes;
        this.sqlColumns = sqlColumns;
        this.adapter = adapter;
        this.usesArray = usesArray;
        this.parameterSeparator = parameterSeparator;
    }

    // implements ResultMapping
    public void setParameters(Class<?> type, Class<?> collectionType, Class<?> beanType, Method setter, int[] sqlIndexes,
                              String[] sqlColumns, MappingAdapter adapter, Class<?> mapKeyType, Integer constructorParameter,
                              Constructor<?> constructor, Method staticFactoryMethod, Class<?> collectionClass, int collectionInitialCapacity) {
        this.index = -1;
        this.type = type;
        this.collectionType = collectionType;
        this.beanType = beanType;
        this.setter = setter;
        this.sqlIndexes = sqlIndexes;
        this.sqlColumns = sqlColumns;
        this.adapter = adapter;
        this.mapKeyType = mapKeyType;
        this.constructorParameter = constructorParameter;
        this.constructor = constructor;
        this.staticFactoryMethod = staticFactoryMethod;
        this.collectionClass = collectionClass;
        this.collectionInitialCapacity = collectionInitialCapacity;
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

    public Method[] getGetters() {
        return getters;
    }

    public Method getSetter() {
        return setter;
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
        return beanType == null && constructor == null && staticFactoryMethod == null;
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

    public Method getStaticFactoryMethod() {
        return staticFactoryMethod;
    }

    public Class<?> getCollectionClass() {
        return collectionClass;
    }

    public int getInitialCollectionCapacity() {
        return collectionInitialCapacity;
    }

    public boolean usesArray() {
        return usesArray;
    }

    public String getParameterSeparator() {
        return parameterSeparator;
    }

    private String getSqlColumnsString() {
        return sqlColumns == null ? "" :
                "[" + Arrays.stream(sqlColumns).map(c -> "\"" + c + "\"").collect(Collectors.joining(",")) + "]";
    }

    private String getSqlIndexesString() {
        return sqlIndexes == null ? "" :
                "[" + Arrays.stream(sqlIndexes).mapToObj(Integer::toString).collect(Collectors.joining(",")) + "]";
    }

    private String stripType(Class<?> type) {
        String typeName = type.getName();
        typeName = typeName.replace("java.lang.", "");
        typeName = typeName.replace("java.util.", "");
        typeName = typeName.replace("AbstractNumberMapping$", "");
        typeName = typeName.replace("AbstractCharacterMapping$", "");
        typeName = typeName.replace("AbstractDateTimeMapping$", "");
        return typeName;
    }

    public String parameterMappingInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Parameter: ").append(stripType(this.getClass())).append('\n');
        sb.append('\t').append(getSqlColumnsString()).append(getSqlIndexesString());
        sb.append(" = parameter ").append(index).append(' ');
        if (getters != null) {
            sb.append(Arrays.toString(getters));
        } else {
            sb.append(stripType(type));
        }
        if (adapter != null) {
            sb.append("\tuse adapter ").append(adapter.getClass());
        }

        return sb.toString();
    }

    public String resultMappingInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Result: ").append(stripType(this.getClass())).append('\n');
        sb.append('\t').append(getSqlColumnsString()).append(getSqlIndexesString());
        sb.append(" => ");
        if (setter != null) {
            sb.append(setter);
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
