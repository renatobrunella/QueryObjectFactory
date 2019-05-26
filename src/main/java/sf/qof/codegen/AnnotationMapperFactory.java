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
package sf.qof.codegen;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import sf.qof.Call;
import sf.qof.Delete;
import sf.qof.Insert;
import sf.qof.Query;
import sf.qof.Update;
import sf.qof.exception.ValidationException;
import sf.qof.mapping.Mapper;
import sf.qof.mapping.MappingFactory;
import sf.qof.mapping.MethodInfo;
import sf.qof.mapping.MethodInfoFactory;
import sf.qof.mapping.MethodParameterInfo;
import sf.qof.mapping.MethodReturnInfo;
import sf.qof.mapping.ParameterMapping;
import sf.qof.mapping.QueryType;
import sf.qof.mapping.ResultMapping;
import sf.qof.mapping.MappingFactory.MappingClassInfo;
import sf.qof.parser.ParameterDefinition;
import sf.qof.parser.ResultDefinition;
import sf.qof.parser.SqlParser;
import sf.qof.util.ReflectionUtils;

/**
 * Internal - AnnotationMapperFactory creates mappings from annotations and 
 * method definitions.
 */
public final class AnnotationMapperFactory {

  private AnnotationMapperFactory() { }
  
  public static Mapper create(Class<?> queryDefinitionClass, Method method) {
    if (method.isAnnotationPresent(Query.class)) {
      return create(queryDefinitionClass, MethodInfoFactory.createMethodInfo(method), method.getAnnotation(Query.class));
    } else if (method.isAnnotationPresent(Insert.class)) {
      return create(queryDefinitionClass, MethodInfoFactory.createMethodInfo(method), method.getAnnotation(Insert.class));
    } else if (method.isAnnotationPresent(Update.class)) {
      return create(queryDefinitionClass, MethodInfoFactory.createMethodInfo(method), method.getAnnotation(Update.class));
    } else if (method.isAnnotationPresent(Delete.class)) {
      return create(queryDefinitionClass, MethodInfoFactory.createMethodInfo(method), method.getAnnotation(Delete.class));
    } else if (method.isAnnotationPresent(Call.class)) {
      return create(queryDefinitionClass, MethodInfoFactory.createMethodInfo(method), method.getAnnotation(Call.class));
    } else {
      return null;
    }
  }

  private static Mapper create(Class<?> queryDefinitionClass, MethodInfo methodInfo, Query annotation) {
    SqlParser parser = new SqlParser(annotation.sql(), false);
    return new Mapper(methodInfo, QueryType.QUERY, parser.getSql(),
        createParameterMappers(queryDefinitionClass, methodInfo, parser.getParameterDefinitions()),
        createResultMappers(queryDefinitionClass, methodInfo, parser.getResultDefinitions(), 
            annotation.factoryClass(), annotation.factoryMethod(),
            annotation.collectionClass(), annotation.collectionInitialCapacity()));
  }

  private static Mapper create(Class<?> queryDefinitionClass, MethodInfo methodInfo, Insert annotation) {
    SqlParser parser = new SqlParser(annotation.sql(), false);
    return new Mapper(methodInfo, QueryType.INSERT, parser.getSql(),
        createParameterMappers(queryDefinitionClass, methodInfo, parser.getParameterDefinitions()), null);
  }

  private static Mapper create(Class<?> queryDefinitionClass, MethodInfo methodInfo, Update annotation) {
    SqlParser parser = new SqlParser(annotation.sql(), false);
    return new Mapper(methodInfo, QueryType.UPDATE, parser.getSql(), 
        createParameterMappers(queryDefinitionClass, methodInfo, parser.getParameterDefinitions()), null);
  }

  private static Mapper create(Class<?> queryDefinitionClass, MethodInfo methodInfo, Delete annotation) {
    SqlParser parser = new SqlParser(annotation.sql(), false);
    return new Mapper(methodInfo, QueryType.DELETE, parser.getSql(), 
        createParameterMappers(queryDefinitionClass, methodInfo, parser.getParameterDefinitions()), null);
  }

  private static Mapper create(Class<?> queryDefinitionClass, MethodInfo methodInfo, Call annotation) {
    SqlParser parser = new SqlParser(annotation.sql(), true);
    return new Mapper(methodInfo, QueryType.CALL, parser.getSql(), 
        createParameterMappers(queryDefinitionClass, methodInfo, parser.getParameterDefinitions()),
        createResultMappers(queryDefinitionClass, methodInfo, parser.getResultDefinitions(), 
            annotation.factoryClass(), annotation.factoryMethod(), Object.class, 0));
  }

  private static List<ParameterMapping> createParameterMappers(Class<?> queryDefinitionClass, MethodInfo methodInfo, ParameterDefinition[] parameterDefs) {
    List<ParameterMapping> list = new ArrayList<ParameterMapping>();
    for (ParameterDefinition parameter : parameterDefs) {
      // get fields from annotation
      String mappingType = parameter.getType();
      int index = parameter.getParameter() - 1;
      if (index < 0 || index >= methodInfo.getParameterInfos().length) {
        throw new ValidationException("Invalid parameter index for method " + methodInfo);
      }
      int[] sqlIndexes = parameter.getIndexes();
      String[] sqlColumns = parameter.getNames();
      if (sqlIndexes == null && sqlColumns == null) {
        throw new ValidationException("Either indexes or columns must be defined");
      }
      String[] fields = parameter.getFields();
      Class<?> type = null;
      Class<?> collectionType = null;
      Class<?> collectionElementType = null;
      Class<?> arrayElementType = null;
      Class<?> beanType = null;
      Method[] getters = null;

      // get types
      MethodParameterInfo parameterInfo = methodInfo.getParameterInfos()[index];
      collectionType = parameterInfo.getCollectionType();
      collectionElementType = parameterInfo.getCollectionElementType();
      arrayElementType = parameterInfo.getArrayElementType();
      
      boolean usesArray = arrayElementType != null && arrayElementType != Byte.TYPE;
      if (fields == null) {
        if (collectionType != null) {
          type = collectionElementType;
        } else if (usesArray) {
          type = arrayElementType;
        } else {
          type = parameterInfo.getType();
        }
      } else {
        if (collectionType != null) {
          beanType = collectionElementType;
        } else if (usesArray) {
          beanType = arrayElementType;
        } else {
          beanType = parameterInfo.getType();
        }
        getters = ReflectionUtils.findGetters(beanType, fields);
        if (getters == null) {
          throw new ValidationException("Cannot find or access getter for " + Arrays.toString(fields) + " in class " + beanType.getName());
        }
        type = getters[getters.length - 1].getReturnType();
      }
      // create mapping
      ParameterMapping mapping = MappingFactory.createParameterMapping(queryDefinitionClass.getClassLoader(), 
          mappingType, index, type, collectionType, beanType, getters, sqlIndexes, sqlColumns, usesArray, parameter.getParameterSeparator());
      list.add(mapping);
    }
    return list;
  }

  private static List<ResultMapping> createResultMappers(Class<?> queryDefinitionClass, MethodInfo methodInfo, ResultDefinition[] resultDefs, 
      Class<?> factoryClass, String factoryMethod, Class<?> collectionClass, int collectionInitialCapacity) {
    List<ResultMapping> list = new ArrayList<ResultMapping>();
    
    MethodReturnInfo returnInfo = methodInfo.getReturnInfo();
    Constructor<?> constructor = null;
    Method staticFactoryMethod = null;
    if (factoryClass == Object.class) {
      constructor = findConstructor(queryDefinitionClass, methodInfo, returnInfo, resultDefs);
    } else {
      staticFactoryMethod = findStaticMethod(queryDefinitionClass, methodInfo, factoryClass, factoryMethod, returnInfo, resultDefs);
    }
    for (ResultDefinition result : resultDefs) {
      // get fields from annotation
      String mappingType = result.getType();
      int[] sqlIndexes = result.getIndexes();
      String[] sqlColumns = result.getColumns();
      if (sqlIndexes == null && sqlColumns == null) {
        throw new ValidationException("Either indexes or columns must be defined");
      }
      Integer constructorParameter = null;
      if (result.getConstructorParameter() > 0) {
        constructorParameter = result.getConstructorParameter();
      }
      String field = result.getField();
      Class<?> type = null;
      Class<?> collectionType = returnInfo.getCollectionType();
      Class<?> collectionElementType = returnInfo.getCollectionElementType();
      Class<?> mapKeyType = null;
      Class<?> beanType = null;
      Method setter = null;

      // check user defined collection class
      Class<?> collection = null;
      if (collectionClass != Object.class) {
        collection = collectionClass;
        if (collectionType == null) {
          throw new ValidationException("Return type of method must be a collection if collectionClass is defined");
        }
        if (!collectionType.isAssignableFrom(collection)) {
          throw new ValidationException("Cannot assign " + collection.getName() + " to return type " + collectionType.getName());
        }
        if (collection.isInterface()) {
          throw new ValidationException("collectionClass cannot be an interface " + collection.getName());
        }
        if (Modifier.isAbstract(collection.getModifiers())) {
          throw new ValidationException("collectionClass cannot be an abstract class " + collection.getName());
        }
        if (collectionInitialCapacity != 0) {
          try {
            collection.getConstructor(int.class);
          } catch (Exception e) {
            throw new ValidationException("Type " + collection.getName() + " does not have constructor to set initial capacity");
          }
        }
      }
      
      // get types
      
      if (field == null) {
        if (collectionType != null) {
          type = collectionElementType;
          if (result.isMapKey()) {
            mapKeyType = returnInfo.getMapKeyType();
          }
        } else {
          type = returnInfo.getType();
        }
      } else {
        if (collectionType != null) {
          beanType = returnInfo.getCollectionElementType();
          if (result.isMapKey()) {
            mapKeyType = returnInfo.getMapKeyType();
          }
        } else {
          beanType = returnInfo.getType();
        }
        MappingClassInfo info = MappingFactory.getResultMappingInfo(queryDefinitionClass.getClassLoader(), mappingType);
        if (info == null) {
          setter = ReflectionUtils.findSetter(beanType, field);
        } else {
          Set<Class<?>> mappableTypes = info.getMappableTypes();
          // special handling for Enum
          if (mappableTypes.size() == 1 && mappableTypes.contains(Enum.class)) {
            setter = ReflectionUtils.findSetter(beanType, field);
          } else {
            setter = ReflectionUtils.findSetter(beanType, field, mappableTypes);
          }
        }
        if (setter == null) {
          throw new ValidationException("Cannot find or access setter for " + field + " in class " + beanType.getName()
              + " for mapping type " + mappingType);
        }
        type = setter.getParameterTypes()[0];
      }

      // create mapping
      ResultMapping mapping = MappingFactory.createResultMapping(queryDefinitionClass.getClassLoader(), mappingType,
          type, collectionType, beanType, setter, sqlIndexes, sqlColumns, mapKeyType, constructorParameter,
          (constructorParameter != null ? constructor : null), (constructorParameter != null ? staticFactoryMethod : null),
          collection, collectionInitialCapacity);
      list.add(mapping);
    }
    return list;
  }

  private static Constructor<?> findConstructor(Class<?> queryDefinitionClass, MethodInfo methodInfo, MethodReturnInfo returnInfo, ResultDefinition[] resultDefs) {
    List<ResultDefinition> constructorResultDefs = getConstrutorResultDefs(resultDefs);
    if (constructorResultDefs.size() == 0) {
      return null;
    }

    for (ResultDefinition resultDefinition : constructorResultDefs) {
      if ("auto".equals(resultDefinition.getType())) {
        String methodName = queryDefinitionClass.getName() + "." + methodInfo.getSignature().getName();
        throw new ValidationException("Constructor parameters must have a type definition: {%%" +
            resultDefinition.getConstructorParameter() + "} in method " + methodName);
      }
    }
    
    Class<?> type;
    if (returnInfo.getCollectionType() != null) {
      type = returnInfo.getCollectionElementType();
    } else {
      type = returnInfo.getType();
    }

    Constructor<?>[] constructors = type.getConstructors();
    
    Constructor<?> matchingConstructor = null;
    for (Constructor<?> constructor : constructors) {
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      if (parameterTypes.length != constructorResultDefs.size()) {
        continue;
      }
      for (int i = 0; i < parameterTypes.length; i++) {
        ResultDefinition def = constructorResultDefs.get(i);
        MappingClassInfo info = MappingFactory.getResultMappingInfo(queryDefinitionClass.getClassLoader(), def.getType());
        if (!info.getMappableTypes().contains(parameterTypes[i])) {
          break;
        }
        if (i + 1 == parameterTypes.length) {
          matchingConstructor = constructor;
          break;
        }
      }
      if (matchingConstructor != null) {
        break;
      }
    }

    if (matchingConstructor == null) {
      // try again but relaxed rules
      for (Constructor<?> constructor : constructors) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (parameterTypes.length != constructorResultDefs.size()) {
          continue;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
          ResultDefinition def = constructorResultDefs.get(i);
          MappingClassInfo info = MappingFactory.getResultMappingInfo(queryDefinitionClass.getClassLoader(), def.getType());
          boolean found = false;
          for (Class<?> mappableType : info.getMappableTypes()) {
            if (mappableType.isAssignableFrom(parameterTypes[i])) {
              found = true;
              break;
            }
          }
          if (!found) {
            break;
          }
          if (i + 1 == parameterTypes.length) {
            matchingConstructor = constructor;
            break;
          }
        }
        if (matchingConstructor != null) {
          break;
        }
      }
    }
    
    if (matchingConstructor == null) {
      throw new RuntimeException("Could not find matching constructor in " + type);
    }
    
    return matchingConstructor;
  }

  private static Method findStaticMethod(Class<?> queryDefinitionClass, MethodInfo methodInfo, Class<?> factoryClass,
      String factoryMethod, MethodReturnInfo returnInfo, ResultDefinition[] resultDefs) {
    List<ResultDefinition> constructorResultDefs = getConstrutorResultDefs(resultDefs);
    if (constructorResultDefs.size() == 0) {
      return null;
    }

    for (ResultDefinition resultDefinition : constructorResultDefs) {
      if ("auto".equals(resultDefinition.getType())) {
        String methodName = queryDefinitionClass.getName() + "." + methodInfo.getSignature().getName();
        throw new ValidationException("Static factory method parameters must have a type definition: {%%" +
            resultDefinition.getConstructorParameter() + "} in method " + methodName);
      }
    }

    Class<?> type;
    if (returnInfo.getCollectionType() != null) {
      type = returnInfo.getCollectionElementType();
    } else {
      type = returnInfo.getType();
    }

    Method[] factoryMethods = factoryClass.getDeclaredMethods();
    
    Method matchingFactoryMethod = null;
    for (Method method : factoryMethods) {
      if (!method.getName().equals(factoryMethod)) {
        continue;
      }
      // skip non-static or non-public methods
      if (!(Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()))) {
        continue;
      }
      // check return type of factory method
      if (!type.isAssignableFrom(method.getReturnType())) {
        continue;
      }
      Class<?>[] parameterTypes = method.getParameterTypes();
      // do we have the correct number of parameters?
      if (parameterTypes.length != constructorResultDefs.size()) {
        continue;
      }
      for (int i = 0; i < parameterTypes.length; i++) {
        ResultDefinition def = constructorResultDefs.get(i);
        MappingClassInfo info = MappingFactory.getResultMappingInfo(queryDefinitionClass.getClassLoader(), def.getType());
        if (info == null || !info.getMappableTypes().contains(parameterTypes[i])) {
          break;
        }
        if (i + 1 == parameterTypes.length) {
          matchingFactoryMethod = method;
          break;
        }
      }
      if (matchingFactoryMethod != null) {
        break;
      }
    }

    if (matchingFactoryMethod == null) {
      // try again but relaxed rules
      for (Method method : factoryMethods) {
        if (!method.getName().equals(factoryMethod)) {
          continue;
        }
        // skip non-static or non-public methods
        if (!(Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()))) {
          continue;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != constructorResultDefs.size()) {
          continue;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
          ResultDefinition def = constructorResultDefs.get(i);
          MappingClassInfo info = MappingFactory.getResultMappingInfo(queryDefinitionClass.getClassLoader(), def.getType());
          boolean found = false;
          if (info != null) {
            for (Class<?> mappableType : info.getMappableTypes()) {
              if (mappableType.isAssignableFrom(parameterTypes[i])) {
                found = true;
                break;
              }
            }
          }
          if (!found) {
            break;
          }
          if (i + 1 == parameterTypes.length) {
            matchingFactoryMethod = method;
            break;
          }
        }
        if (matchingFactoryMethod != null) {
          break;
        }
      }
    }
    
    if (matchingFactoryMethod == null) {
      throw new RuntimeException("Could not find matching static factory method " + factoryMethod + " in " + factoryClass);
    }
    
    return matchingFactoryMethod;
  }

  private static List<ResultDefinition> getConstrutorResultDefs(
      ResultDefinition[] resultDefs) {
    List<ResultDefinition> constructorResultDefs = new ArrayList<ResultDefinition>();
    for (ResultDefinition resultDef : resultDefs) {
      if (resultDef.getConstructorParameter() > 0) {
        constructorResultDefs.add(resultDef);
      }
    }
    Collections.sort(constructorResultDefs, new Comparator<ResultDefinition>() {
      public int compare(ResultDefinition o1, ResultDefinition o2) {
        return o1.getConstructorParameter() - o2.getConstructorParameter();
      }
    });
    return constructorResultDefs;
  }
  
}
