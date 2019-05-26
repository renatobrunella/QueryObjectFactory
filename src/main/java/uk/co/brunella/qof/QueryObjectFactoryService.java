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
package uk.co.brunella.qof;

import uk.co.brunella.qof.adapter.DynamicMappingAdapter;
import uk.co.brunella.qof.adapter.GeneratorMappingAdapter;
import uk.co.brunella.qof.adapter.MappingAdapter;
import uk.co.brunella.qof.customizer.Customizer;
import uk.co.brunella.qof.customizer.DefaultCustomizer;
import uk.co.brunella.qof.dialect.SQLDialect;

/**
 * This is the OSGi service interface that the uk.co.brunella.qof bundle registers.
 * <p>
 * OSGi bundles can either use this service or directly use the static methods
 * of <code>uk.co.brunella.qof.QueryObjectFactroy</code>.
 * <p>
 * The service implementing this interface uses the OSGi logging service. Bundles
 * using this service are tracked i.e. if they are stopped, all registered mappings
 * etc. are unregistered.
 * <p>
 * The methods provided by this service are equal to the static methods of
 * <code>uk.co.brunella.qof.QueryObjectFactroy</code>.
 *
 * @see QueryObjectFactory
 */
public interface QueryObjectFactoryService {

    /**
     * Creates a query object class defined by a query definition and returns a new instance.
     *
     * <p> If the query definition is a class then the generated query object class will be
     * a subclass of this class. The query definition class can be abstract or concrete but
     * is not allowed to be final. All constructors of the query definition class will be
     * implemented by the query object class but only the default constructor will be used
     * to instantiate the query object.
     *
     * <p>If the query definition is an interface then the superclass will be <code>Object</code>.
     *
     * @param queryDefinitionClass query definition class or interface
     * @return an instance that implements the abstract definitions defined in <code>queryDefinitionClass</code>
     * @see QueryObjectFactory#createQueryObject(Class, Object...)
     * @see QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class)
     * @see QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class, Object...)
     * @since 1.0
     */
    <T> T createQueryObject(Class<T> queryDefinitionClass);

    /**
     * Creates a query object class defined by a query definition and returns a new instance.
     *
     * <p> The generated query object class is a subclass of query definition class. The
     * query definition class can be abstract or concrete but is not allowed to be final.
     * All constructors of the query definition class will be implemented by the query object
     * class and <code>parameters</code> will be used to find a matching constructor to instantiate
     * the query object with these parameters. The matching process works on the type of
     * the parameters and will match the first constructor that matches all parameter type.
     *
     * <p> A single <code>null</code> parameter should be passed to a constructor as:
     * <code>new Class[] {null}</code>.
     *
     * @param queryDefinitionClass query definition class
     * @param parameters           parameters to be used in the constructor
     * @return an instance that implements the abstract definitions defined in <code>queryDefinitionClass</code>
     * @see QueryObjectFactory#createQueryObject(Class)
     * @see QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class)
     * @see QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class, Object...)
     * @since 1.0
     */
    <T> T createQueryObject(Class<T> queryDefinitionClass, Object... parameters);

    /**
     * Creates a query object class defined by a query definition and a super class and returns a new instance.
     *
     * <p> The generated query object class will be a subclass of <code>superClass</code>
     * which can be abstract or concrete but is not allowed to be final.
     * All constructors of <code>superClass</code> will be implemented by the query object
     * class but only the default constructor will be used to instantiate the query object.
     *
     * <p><code>queryDefinitionClass</code> must be an interface.
     *
     * @param queryDefinitionClass query definition interface
     * @param superClass           the class the query object class will inherit from
     * @return an instance that implements the abstract definitions defined in <code>queryDefinitionClass</code>
     * @see QueryObjectFactory#createQueryObject(Class)
     * @see QueryObjectFactory#createQueryObject(Class, Object...)
     * @see QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class, Object...)
     * @since 1.0
     */
    <T, S> T createQueryObjectFromSuperClass(Class<T> queryDefinitionClass, Class<S> superClass);

    /**
     * Creates a query object class defined by a query definition and a super class and returns a new instance.
     *
     * <p> The generated query object class will be a subclass of <code>superClass</code>
     * which can be abstract or concrete but is not allowed to be final.
     * All constructors of <code>superClass</code> will be implemented by the query object class
     * and <code>parameters</code> will be used to find a matching constructor to instantiate
     * the query object with these parameters. The matching process works on the type of
     * the parameters and will match the first constructor that matches all parameter type.
     *
     * <p> A single <code>null</code> parameter should be passed to a constructor as:
     * <code>new Class[] {null}</code>.
     *
     * <p><code>queryDefinitionClass</code> must be an interface.
     *
     * @param queryDefinitionClass query definition interface
     * @param superClass           the class the query object class will inherit from
     * @param parameters           parameters to be used in the constructor
     * @return an instance that implements the abstract definitions defined in <code>queryDefinitionClass</code>
     * @see QueryObjectFactory#createQueryObject(Class)
     * @see QueryObjectFactory#createQueryObject(Class, Object...)
     * @see QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class)
     * @since 1.0
     */
    <T, S> T createQueryObjectFromSuperClass(Class<T> queryDefinitionClass, Class<S> superClass, Object... parameters);

    /**
     * Sets the <code>Customizer</code> for the code generation.
     *
     * @param customizer a customizer
     * @see Customizer
     */
    void setCustomizer(Customizer customizer);

    /**
     * Resets the <code>Customizer</code> for the code generation to the default customizer.
     *
     * @see Customizer
     * @see DefaultCustomizer
     */
    void setDefaultCustomizer();

    /**
     * Register a custom mapping adapter in the mapping registry.
     *
     * <p> This method can be used to register custom mapping adapters.
     *
     * @param type    mapping type name
     * @param adapter mapping adapter
     * @see GeneratorMappingAdapter
     * @see DynamicMappingAdapter
     * @see MappingAdapter
     */
    void registerMapper(String type, MappingAdapter adapter);

    /**
     * Unregister a custom mapping adapter from the mapping registry.
     *
     * @param type mapping type name
     */
    void unregisterMapper(String type);

    /**
     * Returns true if a custom mapping adapter is installed in the mapping registry.
     *
     * @param type mapping type name
     * @return true if an adapter is installed
     */
    boolean isMapperRegistered(String type);

    /**
     * Sets the SQL dialect.
     *
     * @param dialect SQL dialect
     * @see SQLDialect
     */
    void setSQLDialect(SQLDialect dialect);

}
