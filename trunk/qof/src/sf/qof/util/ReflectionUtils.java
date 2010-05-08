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
package sf.qof.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.core.Signature;

import sf.qof.exception.ValidationException;

/**
 * Utility class for reflection functionality.
 */
public final class ReflectionUtils {

  private ReflectionUtils() { }
  
  /**
   * Returns the getter methods for field names in a specified class.
   * 
   * @param startType  the class
   * @param fieldNames field names
   * @return           getter methods for fields or null
   */
  public static Method[] findGetters(Class<?> startType, String[] fieldNames) {
    Class<?> type = startType;
    Method[] getters = new Method[fieldNames.length];
    for (int i = 0; i < fieldNames.length; i++) {
      Method method = findGetter(type, fieldNames[i]);
      if (method == null) {
        // missing getter
        return null;
      }
      type = method.getReturnType();
      getters[i] = method;
    }
    return getters;
  }
  
  /**
   * Returns the getter method for a field name in a specified class.
   * 
   * @param type       the class
   * @param fieldName  field name
   * @return           getter method for a field or null
   */
  public static Method findGetter(Class<?> type, String fieldName) {
    try {
      return type.getMethod(createGetterName(fieldName), (Class[]) null);
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    }
    try {
      Method method = type.getMethod(createIsGetterName(fieldName), (Class[]) null);
      if (method.getReturnType() == Boolean.class || method.getReturnType() == Boolean.TYPE) {
        return method;
      }
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    }
    return null;
  }

  /**
   * Returns the setter method for a field name in a specified class.
   * 
   * @param type       the class
   * @param fieldName  field name
   * @return           setter method for a field or null
   */
  public static Method findSetter(Class<?> type, String fieldName) {
    String setterName = createSetterName(fieldName);
    Method[] methods = type.getDeclaredMethods();
    for (Method method : methods) {
      if (method.getName().equals(setterName)) {
        return method;
      }
    }
    return null;
  }

  /**
   * Returns the setter method for a field name in a specified class
   * that uses a parameter of a type in a set.
   * 
   * @param type           the class
   * @param fieldName      field name
   * @param mappableTypes  set of all mappable types
   * @return               setter method for a field or null
   */
  public static Method findSetter(Class<?> type, String fieldName, Set<Class<?>> mappableTypes) {
    if (mappableTypes == null) {
      return findSetter(type, fieldName);
    }
    String setterName = createSetterName(fieldName);
    for (Class<?> mappableType : mappableTypes) {
      try {
        return type.getMethod(setterName, new Class[] { mappableType });
      } catch (SecurityException e) {
        // ignore
      } catch (NoSuchMethodException e) {
        // ignore
      }
    }
    return null;
  }

  private static String createGetterName(String fieldName) {
    return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
  }

  private static String createIsGetterName(String fieldName) {
    return "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
  }

  private static String createSetterName(String fieldName) {
    return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
  }

  /**
   * Returns the collection or map type of a type.
   * 
   * @param type  the type
   * @return      the collection type or null
   */
  public static Class<?> getCollectionType(Type type) {
    if (type instanceof ParameterizedType) {
      Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
      if ((Collection.class.isAssignableFrom(rawType) || 
           Map.class.isAssignableFrom(rawType)) && type != void.class) {
        return rawType;
      }
    }
    return null;
  }

  /**
   * Returns the array component type of a type.
   * 
   * @param type  the type
   * @return      the array component type or null
   */
  public static Class<?> getArrayComponentType(Type type) {
    if (type instanceof Class<?>) {
      Class<?> clazz = (Class<?>)type;
      if (clazz.isArray()) {
        return clazz.getComponentType();
      }
    } else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType)type).getGenericComponentType();
      if (componentType instanceof Class<?>) {
        return (Class<?>)componentType;
      }
    }
    return null;
  }

  /**
   * Returns the parameterized type of a collection type.
   * For example if <code>type</code> is <code>List&lt;String&gt;</code>
   * this method returns <code>String</code>. If <code>type</code> is 
   * <code>Map&lt;int, String&gt;</code> it returns <code>String</code>. 
   * 
   * @param type  the parameterized type
   * @return      the parameterized type
   * @throws ValidationException if type is not parameterized
   */
  public static Class<?> getCollectionParameterizedType(Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      int numTypeArguments = parameterizedType.getActualTypeArguments().length;
      if (numTypeArguments == 1) {
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
      } else if (numTypeArguments == 2) {
        return (Class<?>) parameterizedType.getActualTypeArguments()[1];
      } else {
        throw new ValidationException("Collection parameterized type must be one for " + type);
      }
    } else {
      throw new ValidationException("Collection " + type + " must be parameterized");
    }
  }

  /**
   * Returns the key type of a parameterized type.
   * For example if <code>type</code> is <code>Map&lt;int, String&gt;</code>
   * it returns <code>int</code>.
   * 
   * @param type  the parameterized type
   * @return      the key type
   */
  public static Class<?> getCollectionParameterizedKeyType(Type type) {
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      if (parameterizedType.getActualTypeArguments().length != 2) {
        return null;
      }
      return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    } else {
      return null;
    }
  }

  private static final Map<Class<?>, Class<?>> BOXED_CLASSES;
  private static final Map<Class<?>, Class<?>> UNBOXED_CLASSES;
  static {
    BOXED_CLASSES = new HashMap<Class<?>, Class<?>>(8);
    BOXED_CLASSES.put(Byte.class, Byte.TYPE);
    BOXED_CLASSES.put(Short.class, Short.TYPE);
    BOXED_CLASSES.put(Integer.class, Integer.TYPE);
    BOXED_CLASSES.put(Long.class, Long.TYPE);
    BOXED_CLASSES.put(Boolean.class, Boolean.TYPE);
    BOXED_CLASSES.put(Float.class, Float.TYPE);
    BOXED_CLASSES.put(Double.class, Double.TYPE);
    BOXED_CLASSES.put(Void.class, Void.TYPE);

    UNBOXED_CLASSES = new HashMap<Class<?>, Class<?>>(8);
    for (Class<?> boxed : BOXED_CLASSES.keySet()) {
      UNBOXED_CLASSES.put(BOXED_CLASSES.get(boxed), boxed);
    }
  }

  /**
   * Returns the unboxed type of a specified type. 
   * Types that are not boxed are returned unchanged.
   * 
   * @param type  the type
   * @return      unboxed type
   */
  public static Class<?> unbox(Class<?> type) {
    Class<?> returnClass = BOXED_CLASSES.get(type);
    return returnClass == null ? type : returnClass;
  }

  /**
   * Returns the boxed type of a specified primitive type. 
   * Types that are not primitive types are returned unchanged.
   * 
   * @param type  the type
   * @return      boxed type
   */
  public static Class<?> box(Class<?> type) {
    Class<?> returnClass = UNBOXED_CLASSES.get(type);
    return returnClass == null ? type : returnClass;
  }

  /**
   * Returns a method <code>Signature</code> object for a <code>Method</code> parameter.
   * 
   * @param method  the method
   * @return        a method signature
   */
  public static Signature getMethodSignature(Method method) {
    Class<?>[] params = method.getParameterTypes();
    org.objectweb.asm.Type[] paramTypes = new org.objectweb.asm.Type[params.length];
    for (int i = 0; i < params.length; i++) {
      paramTypes[i] = org.objectweb.asm.Type.getType(params[i]);
    }

    return new Signature(method.getName(), org.objectweb.asm.Type.getType(method.getReturnType()), paramTypes);
  }
  
  /**
   * Returns a constructor <code>Signature</code> object for a <code>Constructor</code> parameter.
   * 
   * @param constructor  the constructor
   * @return             a constructor signature
   */
  public static Signature getConstructorSignature(Constructor<?> constructor) {
    Class<?>[] params = constructor.getParameterTypes();
    org.objectweb.asm.Type[] paramTypes = new org.objectweb.asm.Type[params.length];
    for (int i = 0; i < params.length; i++) {
      paramTypes[i] = org.objectweb.asm.Type.getType(params[i]);
    }

    return new Signature("<init>", org.objectweb.asm.Type.VOID_TYPE, paramTypes);
  }

}
