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
package sf.qof.codegen;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
	  MethodInfo methodInfo = MethodInfoFactory.createMethodInfo(method);
    if (method.isAnnotationPresent(Query.class)) {
      return create(queryDefinitionClass, methodInfo, method.getAnnotation(Query.class));
    } else if (method.isAnnotationPresent(Insert.class)) {
      return create(queryDefinitionClass, methodInfo, method.getAnnotation(Insert.class));
    } else if (method.isAnnotationPresent(Update.class)) {
      return create(queryDefinitionClass, methodInfo, method.getAnnotation(Update.class));
    } else if (method.isAnnotationPresent(Delete.class)) {
      return create(queryDefinitionClass, methodInfo, method.getAnnotation(Delete.class));
    } else if (method.isAnnotationPresent(Call.class)) {
      return create(queryDefinitionClass, methodInfo, method.getAnnotation(Call.class));
    } else {
      return null;
    }
  }

  private static Mapper create(Class<?> queryDefinitionClass, MethodInfo methodInfo, Query annotation) {
    SqlParser parser = new SqlParser(annotation.sql(), false);
    return new Mapper(methodInfo, QueryType.QUERY, parser.getSql(),
        createParameterMappers(queryDefinitionClass, methodInfo, parser.getParameterDefinitions()),
        createResultMappers(queryDefinitionClass, methodInfo, parser.getResultDefinitions()));
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
        createResultMappers(queryDefinitionClass, methodInfo, parser.getResultDefinitions()));
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
      String field = parameter.getField();
      Class<?> type = null;
      Class<?> collectionType = null;
      Class<?> collectionElementType = null;
      Class<?> arrayElementType = null;
      Class<?> beanType = null;
      Method getter = null;

      // get types
      MethodParameterInfo parameterInfo = methodInfo.getParameterInfos()[index];
      collectionType = parameterInfo.getCollectionType();
      collectionElementType = parameterInfo.getCollectionElementType();
      arrayElementType = parameterInfo.getArrayElementType();
      
      boolean usesArray = arrayElementType != null && arrayElementType != Byte.TYPE;
      if (field == null) {
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
        } else {
          beanType = parameterInfo.getType();
        }
        getter = ReflectionUtils.findGetter(beanType, field);
        if (getter == null) {
          throw new ValidationException("Cannot find or access getter for " + field + " in class " + beanType.getName());
        }
        type = getter.getReturnType();
      }
      // create mapping
      ParameterMapping mapping = MappingFactory.createParameterMapping(queryDefinitionClass.getClassLoader(), 
          mappingType, index, type, collectionType, beanType, getter, sqlIndexes, sqlColumns, usesArray);
      list.add(mapping);
    }
    return list;
  }

  private static List<ResultMapping> createResultMappers(Class<?> queryDefinitionClass, MethodInfo methodInfo, ResultDefinition[] resultDefs) {
    List<ResultMapping> list = new ArrayList<ResultMapping>();
    
    MethodReturnInfo returnInfo = methodInfo.getReturnInfo();
    Constructor<?> constructor = findConstructor(queryDefinitionClass, returnInfo, resultDefs);
    
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
          setter = ReflectionUtils.findSetter(beanType, field, info.getMappableTypes());
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
          (constructorParameter != null ? constructor : null));
      list.add(mapping);
    }
    return list;
  }

  private static Constructor<?> findConstructor(Class<?> queryDefinitionClass, MethodReturnInfo returnInfo, ResultDefinition[] resultDefs) {
    List<ResultDefinition> constructorResultDefs = new ArrayList<ResultDefinition>();
    for (ResultDefinition resultDef : resultDefs) {
      if (resultDef.getConstructorParameter() > 0) {
        constructorResultDefs.add(resultDef);
      }
    }
    if (constructorResultDefs.size() == 0) {
      return null;
    }
    Collections.sort(constructorResultDefs, new Comparator<ResultDefinition>() {
      public int compare(ResultDefinition o1, ResultDefinition o2) {
        return o1.getConstructorParameter() - o2.getConstructorParameter();
      }
    });

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
}
