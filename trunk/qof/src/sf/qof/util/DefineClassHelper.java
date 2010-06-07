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

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

import net.sf.cglib.core.CodeGenerationException;
import sf.qof.codegen.QueryObjectGenerator;

/**
 * Helper class to call the defineClass method of the ClassLoader.
 * 
 * @see java.lang.ClassLoader
 */
public class DefineClassHelper {

  private static Method DEFINE_CLASS;

  private static final ProtectionDomain PROTECTION_DOMAIN;

  static {
    PROTECTION_DOMAIN = (ProtectionDomain) AccessController.doPrivileged(new PrivilegedAction<Object>() {
      public Object run() {
        return QueryObjectGenerator.class.getProtectionDomain();
      }
    });

    AccessController.doPrivileged(new PrivilegedAction<Object>() {
      public Object run() {
        try {
          Class<?> loader = Class.forName("java.lang.ClassLoader"); // JVM crash w/o this
          DEFINE_CLASS = loader.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class,
              Integer.TYPE, Integer.TYPE, ProtectionDomain.class });
          DEFINE_CLASS.setAccessible(true);
        } catch (ClassNotFoundException e) {
          throw new CodeGenerationException(e);
        } catch (NoSuchMethodException e) {
          throw new CodeGenerationException(e);
        }
        return null;
      }
    });
  }

  /**
   * Calls <code>ClassLoader.defineClass</code> and returns a <code>Class</code>
   * instance if successful.
   * 
   * @param <T>         type of the class
   * @param className   class name
   * @param byteCode    array containing the byte code of the class
   * @param loader      class loader
   * @return            newly defined class
   * @throws Exception  error occurred
   * 
   * @see java.lang.ClassLoader
   */
  public static <T> Class<T> defineClass(String className, byte[] byteCode, ClassLoader loader) throws Exception {
    Object[] args = new Object[] { className, byteCode, new Integer(0), Integer.valueOf(byteCode.length),
        PROTECTION_DOMAIN };
    @SuppressWarnings("unchecked") Class<T> definedClass = (Class<T>) DEFINE_CLASS.invoke(loader, args);
    return definedClass;
  }

}
