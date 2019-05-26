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
package uk.co.brunella.qof.mapping;

import uk.co.brunella.qof.adapter.MappingAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Mapping interface for result mappings.
 */
public interface ResultMapping extends Mapping {

    /**
     * Initializer.
     *
     * @param type                      mapping type
     * @param collectionType            collection type of method return result
     * @param beanType                  Java Bean object type of method return result
     * @param setter                    setter form Java Bean object
     * @param sqlIndexes                array of SQL indexes
     * @param sqlColumns                array of SQL column names
     * @param adapter                   custom mapping adapter
     * @param mapKeyType                map key type
     * @param constructorParameter      index of a parameter in the constructor
     * @param constructor               constructor
     * @param staticFactoryMethod       static factory method
     * @param collectionClass           class of collection
     * @param collectionInitialCapacity initial capacity of collection
     */
    void setParameters(Class<?> type, Class<?> collectionType, Class<?> beanType, Method setter, int[] sqlIndexes,
                       String[] sqlColumns, MappingAdapter adapter, Class<?> mapKeyType, Integer constructorParameter,
                       Constructor<?> constructor, Method staticFactoryMethod, Class<?> collectionClass, int collectionInitialCapacity);

    /**
     * Returns mapping type.
     *
     * @return mapping type
     */
    Class<?> getType();

    /**
     * Returns the collection type of the method return result or null.
     * This needs to implement <code>Collection</code>.
     *
     * @return collection type of the method return result
     */
    Class<?> getCollectionType();

    /**
     * Returns the type of the method return result Java Bean or null.
     *
     * @return bean type
     */
    Class<?> getBeanType();

    /**
     * Returns the setter of the Java Bean or null.
     *
     * @return setter
     */
    Method getSetter();

    /**
     * Returns an array with the column names in the SQL statement.
     *
     * @return array of SQL colum names
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
     * True if the method return result is a collection type.
     *
     * @return uses collection type
     */
    boolean usesCollection();

    /**
     * True if the method return result is either a primitive or an
     * atomic value (like <code>String</code>).
     *
     * @return uses an atomic value
     */
    boolean usesAtomic();

    /**
     * True if mapping is a key for a <code>Map</code>.
     *
     * @return is a map key
     */
    boolean isMapKey();

    /**
     * Returns the type of the method return map key or null.
     *
     * @return map key type
     */
    Class<?> getMapKeyType();

    /**
     * Returns the index of a parameter in the constructor or null.
     *
     * @return constructor parameter index
     */
    Integer getConstructorParameter();

    /**
     * Returns the constructor or null.
     *
     * @return constructor
     */
    Constructor<?> getConstructor();

    /**
     * Returns the static factory method or null.
     *
     * @return static factory method
     */
    Method getStaticFactoryMethod();

    /**
     * Returns the user defined collection class or null.
     *
     * @return collection class
     */
    Class<?> getCollectionClass();

    /**
     * Returns the initial collection size.
     *
     * @return initial collection size
     */
    int getInitialCollectionCapacity();
}
