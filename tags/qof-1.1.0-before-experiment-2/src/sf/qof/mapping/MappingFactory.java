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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sf.qof.adapter.MappingAdapter;
import sf.qof.exception.ValidationException;


public class MappingFactory {

  private MappingFactory() { }
  
  public static ParameterMapping createParameterMapping(ClassLoader classLoader, String mappingType, int index, Class<?> type, Class<?> collectionType,
      Class<?> beanType, Method[] getters, int[] sqlIndexes, String[] sqlColumns, boolean usesArray) {
    MappingClassInfo info;
    if (mappingType.equals("auto")) {
      info = getDefaultParameterMappingInfo(type);
    } else {
      info = getParameterMappingInfo(classLoader, mappingType);
    }
    if (info == null) {
      throw new ValidationException("No mapping found for mapping type " + mappingType);
    }
    boolean found = false;
    for (Class<?> mappableType : info.getMappableTypes()) {
      if (mappableType.isAssignableFrom(type)) {
        found = true;
        break;
      }
    }
    if (!found) {
      throw new ValidationException("No mapping for " + type);
    }
    Class<?> mappingClass = info.getMapperClass();
    try {
      ParameterMapping mapping = (ParameterMapping) mappingClass.newInstance();
      mapping.setParameters(index, type, collectionType, beanType, getters, sqlIndexes, sqlColumns, info.getAdapter(), usesArray);
      return (ParameterMapping) mapping;
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static ResultMapping createResultMapping(ClassLoader classLoader, String mappingType, Class<?> type, Class<?> collectionType, Class<?> beanType,
      Method setter, int[] sqlIndexes, String[] sqlColumns, Class<?> mapKeyType, Integer constructorParameter, Constructor<?> constructor, Method staticFactoryMethod) {
    MappingClassInfo info = null;
    Class<?> resultType;
    if (mapKeyType == null) {
      resultType = type;
    } else {
      resultType = mapKeyType;
    }
    
    if (mappingType.equals("auto")) {
      info = getDefaultResultMappingInfo(resultType);
    } else {
      info = getResultMappingInfo(classLoader, mappingType);
    }
    if (info == null) {
      throw new ValidationException("No mapping found for type " + mappingType + " or " + resultType);
    }
    boolean found = false;
    for (Class<?> mappableType : info.getMappableTypes()) {
      if (mappableType.isAssignableFrom(resultType)) {
        found = true;
        break;
      }
    }
    if (constructorParameter == null && !found) {
      throw new ValidationException("No mapping for " + resultType);
    }
    Class<?> mappingClass = info.getMapperClass();
    try {
      ResultMapping mapping = (ResultMapping) mappingClass.newInstance();
      mapping.setParameters(type, collectionType, beanType, setter, sqlIndexes, sqlColumns, 
    	  info.getAdapter(), mapKeyType, constructorParameter, constructor, staticFactoryMethod);
      return (ResultMapping) mapping;
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  // ------- mapper registration -----------

  private static Map<ClassLoader, Map<String, MappingClassInfo>> registeredResultMappers = new HashMap<ClassLoader, Map<String, MappingClassInfo>>();
  private static Map<ClassLoader, Map<String, MappingClassInfo>> registeredParameterMappers = new HashMap<ClassLoader, Map<String, MappingClassInfo>>();

  private static Map<Class<?>, MappingClassInfo> defaultResultMappers = new HashMap<Class<?>, MappingClassInfo>();
  private static Map<Class<?>, MappingClassInfo> defaultParameterMappers = new HashMap<Class<?>, MappingClassInfo>();

  public static class MappingClassInfo {
    private Class<?> mapperClass;
    private Set<Class<?>> mappableTypes;
    private MappingAdapter adapter;

    public MappingClassInfo(Class<?> mapperClass, Set<Class<?>> mappableTypes) {
      this(mapperClass, mappableTypes, null);
    }

    public MappingClassInfo(Class<?> mapperClass, Set<Class<?>> mappableTypes, MappingAdapter adapter) {
      this.mapperClass = mapperClass;
      this.mappableTypes = mappableTypes;
      this.adapter = adapter;
    }

    public Set<Class<?>> getMappableTypes() {
      return mappableTypes;
    }

    public Class<?> getMapperClass() {
      return (Class<?>) mapperClass;
    }

    public MappingAdapter getAdapter() {
      return adapter;
    }

    public boolean isAdapter() {
      return adapter != null;
    }
  }

  protected static void registerResultMapper(String type, Class<?> mapping, Set<Class<?>> types) {
    if (!ResultMapping.class.isAssignableFrom(mapping)) {
      throw new ValidationException("Mapping " + mapping.getName() + " must implement ResultMapping interface");
    }
    Map<String, MappingClassInfo> map;
    if (registeredResultMappers.containsKey(null)) {
      map = registeredResultMappers.get(null);
    } else {
      map = new HashMap<String, MappingClassInfo>();
      registeredResultMappers.put(null, map);
    }
    MappingClassInfo info = new MappingClassInfo(mapping, types);
    map.put(type, info);
    for (Class<?> t : types) {
      if (!defaultResultMappers.containsKey(t)) {
        defaultResultMappers.put(t, info);
      }
    }
  }

  protected static void registerParameterMapper(String type, Class<?> mapping, Set<Class<?>> types) {
    if (!ParameterMapping.class.isAssignableFrom(mapping)) {
      throw new ValidationException("Mapping " + mapping.getName() + " must implement ParameterMapping interface");
    }
    Map<String, MappingClassInfo> map;
    if (registeredParameterMappers.containsKey(null)) {
      map = registeredParameterMappers.get(null);
    } else {
      map = new HashMap<String, MappingClassInfo>();
      registeredParameterMappers.put(null, map);
    }
    MappingClassInfo info = new MappingClassInfo(mapping, types);
    map.put(type, info);
    for (Class<?> t : types) {
      if (!defaultParameterMappers.containsKey(t)) {
        defaultParameterMappers.put(t, info);
      }
    }
  }

  public static void registerMapper(ClassLoader classLoader, String type, MappingAdapter adapter) {
    registerResultMapper(classLoader, type, adapter);
    registerParameterMapper(classLoader, type, adapter);
  }

  public static void unregisterMapper(ClassLoader classLoader, String type) {
    unregisterResultMapper(classLoader, type);
    unregisterParameterMapper(classLoader, type);
  }
  
  public static boolean isMapperRegistered(ClassLoader classLoader, String type) {
    return isMapperResultRegistered(classLoader, type);
  }

  public static void unregisterMappers(ClassLoader classLoader) {
    registeredResultMappers.remove(classLoader);
    registeredParameterMappers.remove(classLoader);
  }

  protected static void registerResultMapper(ClassLoader classLoader, String type, MappingAdapter adapter) {
    Map<String, MappingClassInfo> map;
    if (registeredResultMappers.containsKey(classLoader)) {
      map = registeredResultMappers.get(classLoader);
    } else {
      map = new HashMap<String, MappingClassInfo>();
      registeredResultMappers.put(classLoader, map);
    }
    if (!map.containsKey(type)) {
      map.put(type, new MappingClassInfo(AdapterMapping.class, adapter.getTypes(), adapter));
    } else {
      throw new ValidationException("Type " + type + " already registered");
    }
  }

  protected static void registerParameterMapper(ClassLoader classLoader, String type, MappingAdapter adapter) {
    Map<String, MappingClassInfo> map;
    if (registeredParameterMappers.containsKey(classLoader)) {
      map = registeredParameterMappers.get(classLoader);
    } else {
      map = new HashMap<String, MappingClassInfo>();
      registeredParameterMappers.put(classLoader, map);
    }
    if (!map.containsKey(type)) {
      map.put(type, new MappingClassInfo(AdapterMapping.class, adapter.getTypes(), adapter));
    } else {
      throw new ValidationException("Type " + type + " already registered");
    }
  }

  protected static void unregisterResultMapper(ClassLoader classLoader, String type) {
    Map<String, MappingClassInfo> map = registeredResultMappers.get(classLoader);
    if (map != null) {
      map.remove(type);
    }
  }

  protected static void unregisterParameterMapper(ClassLoader classLoader, String type) {
    Map<String, MappingClassInfo> map = registeredParameterMappers.get(classLoader);
    if (map != null) {
      map.remove(type);
    }
  }
  
  protected static boolean isMapperResultRegistered(ClassLoader classLoader, String type) {
    Map<String, MappingClassInfo> map = registeredResultMappers.get(classLoader);
    if (map != null) {
      return map.containsKey(type);
    } else {
      return false;
    }
  }

  // --------------------------

  public static MappingClassInfo getParameterMappingInfo(ClassLoader classLoader, String type) {
    MappingClassInfo info = null;
    Map<String, MappingClassInfo> map = registeredParameterMappers.get(classLoader);
    if (map != null) {
      info = map.get(type);
    }
    if (info != null) {
      return info;
    } else if (classLoader != null) {
      return getParameterMappingInfo(null, type);
    }
    return null;
  }

  public static MappingClassInfo getResultMappingInfo(ClassLoader classLoader, String type) {
    MappingClassInfo info = null;
    Map<String, MappingClassInfo> map = registeredResultMappers.get(classLoader);
    if (map != null) {
      info = map.get(type);
    }
    if (info != null) {
      return info;
    } else if (classLoader != null) {
      return getResultMappingInfo(null, type);
    }
    return null;
  }

  public static MappingClassInfo getDefaultParameterMappingInfo(Class<?> type) {
    return defaultParameterMappers.get(type);
  }

  public static MappingClassInfo getDefaultResultMappingInfo(Class<?> type) {
    return defaultResultMappers.get(type);
  }

  // ------- register default mappers -----------
    
  static {
    registerResultMapper("string", AbstractCharacterMapping.StringMapping.class, AbstractCharacterMapping.StringMapping.getTypes());
    registerResultMapper("char", AbstractCharacterMapping.CharacterMapping.class, AbstractCharacterMapping.CharacterMapping.getTypes());
    registerResultMapper("boolean", AbstractNumberMapping.BooleanMapping.class, AbstractNumberMapping.BooleanMapping.getTypes());
    registerResultMapper("byte", AbstractNumberMapping.ByteMapping.class, AbstractNumberMapping.ByteMapping.getTypes());
    registerResultMapper("short", AbstractNumberMapping.ShortMapping.class, AbstractNumberMapping.ShortMapping.getTypes());
    registerResultMapper("int", AbstractNumberMapping.IntegerMapping.class, AbstractNumberMapping.IntegerMapping.getTypes());
    registerResultMapper("integer", AbstractNumberMapping.IntegerMapping.class, AbstractNumberMapping.IntegerMapping.getTypes());
    registerResultMapper("long", AbstractNumberMapping.LongMapping.class, AbstractNumberMapping.LongMapping.getTypes());
    registerResultMapper("float", AbstractNumberMapping.FloatMapping.class, AbstractNumberMapping.FloatMapping.getTypes());
    registerResultMapper("double", AbstractNumberMapping.DoubleMapping.class, AbstractNumberMapping.DoubleMapping.getTypes());
    registerResultMapper("date", AbstractDateTimeMapping.DateMapping.class, AbstractDateTimeMapping.DateMapping.getTypes());
    registerResultMapper("time", AbstractDateTimeMapping.TimeMapping.class, AbstractDateTimeMapping.TimeMapping.getTypes());
    registerResultMapper("timestamp", AbstractDateTimeMapping.TimestampMapping.class, AbstractDateTimeMapping.TimestampMapping.getTypes());
        
    registerParameterMapper("string", AbstractCharacterMapping.StringMapping.class, AbstractCharacterMapping.StringMapping.getTypes());
    registerParameterMapper("char", AbstractCharacterMapping.CharacterMapping.class, AbstractCharacterMapping.CharacterMapping.getTypes());
    registerParameterMapper("boolean", AbstractNumberMapping.BooleanMapping.class, AbstractNumberMapping.BooleanMapping.getTypes());
    registerParameterMapper("byte", AbstractNumberMapping.ByteMapping.class, AbstractNumberMapping.ByteMapping.getTypes());
    registerParameterMapper("short", AbstractNumberMapping.ShortMapping.class, AbstractNumberMapping.ShortMapping.getTypes());
    registerParameterMapper("int", AbstractNumberMapping.IntegerMapping.class, AbstractNumberMapping.IntegerMapping.getTypes());
    registerParameterMapper("integer", AbstractNumberMapping.IntegerMapping.class, AbstractNumberMapping.IntegerMapping.getTypes());
    registerParameterMapper("long", AbstractNumberMapping.LongMapping.class, AbstractNumberMapping.LongMapping.getTypes());
    registerParameterMapper("float", AbstractNumberMapping.FloatMapping.class, AbstractNumberMapping.FloatMapping.getTypes());
    registerParameterMapper("double", AbstractNumberMapping.DoubleMapping.class, AbstractNumberMapping.DoubleMapping.getTypes());
    registerParameterMapper("date", AbstractDateTimeMapping.DateMapping.class, AbstractDateTimeMapping.DateMapping.getTypes());
    registerParameterMapper("time", AbstractDateTimeMapping.TimeMapping.class, AbstractDateTimeMapping.TimeMapping.getTypes());
    registerParameterMapper("timestamp", AbstractDateTimeMapping.TimestampMapping.class, AbstractDateTimeMapping.TimestampMapping.getTypes());
  }
}
