/*
 * Copyright 2007, 2008 brunella ltd
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sf.qof.adapter.CommonAdapterRegistrar;
import sf.qof.adapter.MappingAdapter;
import sf.qof.codegen.AnnotationMapperFactory;
import sf.qof.codegen.QueryObjectGenerator;
import sf.qof.customizer.Customizer;
import sf.qof.customizer.DefaultCustomizer;
import sf.qof.dialect.DefaultDialect;
import sf.qof.dialect.SQLDialect;
import sf.qof.mapping.Mapper;
import sf.qof.mapping.MappingFactory;
import sf.qof.util.CallStackIntrospector;
import sf.qof.util.ClassGenerationCache;
import sf.qof.util.ObjectInstantiator;

/**
 * Used to create query object implementations from definition interfaces or classes. 
 * It only provides static methods and can not be instantiated. 
 *
 * <p> Definition interfaces or classes define abstract methods that are decorated 
 * with annotations to specify the SQL query and mappings to primitive types or 
 * Java beans.</p>
 *
 * <p><blockquote><pre>
 *     public class Person {
 *         int id;
 *         String name;
 *         
 *         public int getId() {
 *             return id;
 *         }
 *         public void setId(int id) {
 *             this.id = id;
 *         }
 *         ...
 *     }
 *     
 *     public interface PersonQueries extends BaseQuery {
 *         &#64;Query(sql = "select id {%%.id}, name {%%.name} from person where id = {%1}")
 *         Person getPerson(int id);
 *         ...
 *     }
 *     
 *     Connection connection = ... // get the database connection from somewhere
 *     PersonQueries personQueries = <b>QueryObjectFactory.createQueryObject(PersonQueries.class);</b>
 *     personQueries.setConnection(connection);
 *     Person person = personQueries.getPerson(123);
 * </pre></blockquote></p>
 * 
 * <p> Generated query object classes implement <code>BaseQuery</code> to set the 
 * connection etc.</p>
 * 
 * <p> Generated query object classes are cached for each ClassLoader. The generation
 * process is thread safe i.e. if two threads are trying to create the same query object 
 * implementation one will wait till the generation of the class completes and then
 * just instantiate an object while the other thread is generating the class.</p>
 * 
 * <p> The generation process can be customized by using a <code>Customizer</code></p>
 *
 * @see BaseQuery
 * @see Query
 * @see Insert
 * @see Update
 * @see Delete
 * @see Call
 */
public class QueryObjectFactory {

  protected QueryObjectFactory() { }
  
  protected static final Customizer DEFAULT_CUSTOMIZER = new DefaultCustomizer(); 
  protected static final SQLDialect DEFAULT_SQL_DIALECT = new DefaultDialect();; 
  protected static Map<ClassLoader, Customizer> customizerMap = new HashMap<ClassLoader, Customizer>();
  protected static Map<ClassLoader, SQLDialect> sqlDialectMap = new HashMap<ClassLoader, SQLDialect>();

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
   * 
   * @see sf.qof.QueryObjectFactory#createQueryObject(Class, Object...)
   * @see sf.qof.QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class)
   * @see sf.qof.QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class, Object...)
   * 
   * @since 1.0
   */
  public static <T> T createQueryObject(Class<T> queryDefinitionClass) {
    return createQueryObject(queryDefinitionClass, new Object[] {});
  }

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
   * @param parameters parameters to be used in the constructor
   * @return an instance that implements the abstract definitions defined in <code>queryDefinitionClass</code>
   * 
   * @see sf.qof.QueryObjectFactory#createQueryObject(Class)
   * @see sf.qof.QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class)
   * @see sf.qof.QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class, Object...)
   * 
   * @since 1.0
   */
  public static <T> T createQueryObject(Class<T> queryDefinitionClass, Object... parameters) {
    if (queryDefinitionClass.isInterface()) {
      return (T) createQueryObjectFromSuperClass(queryDefinitionClass, Object.class, parameters);
    } else {
      return (T) createQueryObjectFromSuperClass(queryDefinitionClass, queryDefinitionClass, parameters);
    }
  }

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
   * @param superClass the class the query object class will inherit from
   * @return an instance that implements the abstract definitions defined in <code>queryDefinitionClass</code>
   * 
   * @see sf.qof.QueryObjectFactory#createQueryObject(Class)
   * @see sf.qof.QueryObjectFactory#createQueryObject(Class, Object...)
   * @see sf.qof.QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class, Object...)
   * 
   * @since 1.0
   */
  public static <T, S> T createQueryObjectFromSuperClass(Class<T> queryDefinitionClass, Class<S> superClass) {
    return createQueryObjectFromSuperClass(queryDefinitionClass, superClass, new Object[] {});
  }

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
   * @param superClass the class the query object class will inherit from
   * @param parameters parameters to be used in the constructor
   * @return an instance that implements the abstract definitions defined in <code>queryDefinitionClass</code>
   * 
   * @see sf.qof.QueryObjectFactory#createQueryObject(Class)
   * @see sf.qof.QueryObjectFactory#createQueryObject(Class, Object...)
   * @see sf.qof.QueryObjectFactory#createQueryObjectFromSuperClass(Class, Class)
   * 
   * @since 1.0
   */
  public static <T, S> T createQueryObjectFromSuperClass(Class<T> queryDefinitionClass, Class<S> superClass,
      Object... parameters) {
    if ((queryDefinitionClass != superClass) && !queryDefinitionClass.isInterface()) {
      throw new RuntimeException("Invalid class hierarchie");
    }
    @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>)ClassGenerationCache.getCachedClass(queryDefinitionClass);
    if (clazz == null) {
      try {
        List<Mapper> mappers = new ArrayList<Mapper>();
        // get all public methods
        for (Method method : queryDefinitionClass.getMethods()) {
          Mapper mapper = AnnotationMapperFactory.create(queryDefinitionClass, method);
          if (mapper != null) {
        	mappers.add(mapper);
          }
        }
        // get all protected methods
        for (Method method : queryDefinitionClass.getDeclaredMethods()) {
          if (Modifier.isProtected(method.getModifiers())) {
            Mapper mapper = AnnotationMapperFactory.create(queryDefinitionClass, method);
            if (mapper != null) {
              mappers.add(mapper);
            }
          }
        }
        ClassLoader classLoader = queryDefinitionClass.getClassLoader();
        Customizer customizer = customizerMap.get(classLoader);
        if (customizer == null) {
          customizer = DEFAULT_CUSTOMIZER;
        }
        SQLDialect sqlDialect = sqlDialectMap.get(classLoader);
        if (sqlDialect == null) {
          sqlDialect = DEFAULT_SQL_DIALECT;
        }
        clazz = new QueryObjectGenerator(customizer, sqlDialect).create(queryDefinitionClass, mappers, superClass);
        // put the newly created class
        ClassGenerationCache.putCachedClass(clazz, queryDefinitionClass);
      } catch (RuntimeException e) {
        ClassGenerationCache.putCachedClass(null, queryDefinitionClass);
        throw e;
      }
    }
    return ObjectInstantiator.newInstance(clazz, parameters);
  }

  /**
   * Sets the <code>Customizer</code> for the code generation.
   * 
   * @param customizer a customizer
   * 
   * @see sf.qof.customizer.Customizer
   */
  public static void setCustomizer(Customizer customizer) {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    setCustomizer(classLoader, customizer);
  }

  protected static synchronized void setCustomizer(ClassLoader classLoader, Customizer customizer) {
    customizerMap.put(classLoader, customizer);
  }

  /**
   * Resets the <code>Customizer</code> for the code generation to the default customizer.
   * 
   * @see sf.qof.customizer.Customizer
   * @see sf.qof.customizer.DefaultCustomizer
   */
  public static void setDefaultCustomizer() {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    setDefaultCustomizer(classLoader);
  }

  protected static synchronized void setDefaultCustomizer(ClassLoader classLoader) {
    customizerMap.remove(classLoader);
  }
  
  /**
   * Register a custom mapping adapter in the mapping registry.
   * 
   * <p> This method can be used to register custom mapping adapters. 
   * 
   * @param type        mapping type name
   * @param adapter     mapping adapter
   * 
   * @see sf.qof.adapter.GeneratorMappingAdapter
   * @see sf.qof.adapter.DynamicMappingAdapter
   * @see sf.qof.adapter.MappingAdapter
   */
  public static void registerMapper(String type, MappingAdapter adapter) {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    registerMapper(classLoader, type, adapter);
  }

  protected static synchronized void registerMapper(ClassLoader classLoader, String type, MappingAdapter adapter) {
    MappingFactory.registerMapper(classLoader, type, adapter);
  }

  /**
   * Unregister a custom mapping adapter from the mapping registry.
   * 
   * @param type        mapping type name
   */
  public static void unregisterMapper(String type) {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    unregisterMapper(classLoader, type);
  }

  protected static synchronized void unregisterMapper(ClassLoader classLoader, String type) {
    MappingFactory.unregisterMapper(classLoader, type);
  }
  
  /**
   * Sets the SQL dialect.
   * 
   * @param dialect    SQL dialect
   * 
   * @see sf.qof.dialect.SQLDialect
   */
  public static void setSQLDialect(SQLDialect dialect) {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    setSQLDialect(classLoader, dialect);
  }

  protected static synchronized void setSQLDialect(ClassLoader classLoader, SQLDialect dialect) {
    sqlDialectMap.put(classLoader, dialect);
  }
  
  static {
    CommonAdapterRegistrar.registerCommonAdapters();
  }
}
