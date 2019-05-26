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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Caches generated classes according to a specified key.
 * <code>ClassGenerationCache</code> is thread-safe.
 * Classes are cached separately for each class loader. 
 */
public final class ClassGenerationCache {
  
  protected ClassGenerationCache() { }

  private static final class GENERATION_PENDING { };

  private static final Map<ClassLoader, Map<String, Class<?>>> cache = new WeakHashMap<ClassLoader, Map<String, Class<?>>>();

  /**
   * Returns a cached class for a specified key.
   * If no class can be found for the specified key null is returned and
   * the key is marked for generation. If a key is marked for generation
   * by another thread this method waits till the other thread completes
   * generation. 
   * 
   * @param key  key for the cached class
   * @return     the cached class or null if not found
   */
  public static Class<?> getCachedClass(Class<?>... key) {
    Map<String, Class<?>> classCache = getClassCache(key);
    synchronized (classCache) {
      Class<?> clazz = classCache.get(createMapKey(key));
      while (clazz == GENERATION_PENDING.class) {
        try {
          // wait till the class is generated
          classCache.wait();
        } catch (InterruptedException e) {
          // ignore
        }
        clazz = classCache.get(createMapKey(key));
      }
      if (clazz == null) {
        classCache.put(createMapKey(key), GENERATION_PENDING.class);
      }
      return clazz;
    }
  }

  /**
   * Puts a class into the class cache specified by a key.
   * If the class is null the key is removed.
   * 
   * @param clazz  the cached class
   * @param key    the key
   */
  public static void putCachedClass(Class<?> clazz, Class<?>... key) {
    Map<String, Class<?>> classCache = getClassCache(key);
    synchronized (classCache) {
      if (clazz == null) {
        classCache.remove(createMapKey(key));
      } else {
        classCache.put(createMapKey(key), clazz);
      }
      classCache.notifyAll();
    }
  }

  private static String createMapKey(Class<?>... key) {
	StringBuilder sb = new StringBuilder();
	for (Class<?> clazz : key) {
	  sb.append(clazz.getName());
	  sb.append('%');
	}
	return sb.toString();
  }
  
  private static Map<String, Class<?>> getClassCache(Class<?>... key) {
    ClassLoader classLoader = key[0].getClassLoader();
    synchronized (cache) {
      Map<String, Class<?>> classCache = cache.get(classLoader);
      if (classCache == null) {
        classCache = new HashMap<String, Class<?>>();
        cache.put(classLoader, classCache);
      }
      return classCache;
    }
  }
}
