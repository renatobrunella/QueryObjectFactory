/*
 * Copyright 2008 - 2010 brunella ltd
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
package sf.qof.bundle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import sf.qof.QueryObjectFactory;
import sf.qof.QueryObjectFactoryService;
import sf.qof.adapter.MappingAdapter;
import sf.qof.customizer.Customizer;
import sf.qof.dialect.SQLDialect;
import sf.qof.mapping.MappingFactory;
import sf.qof.util.CallStackIntrospector;

/**
 * Internal - OSGi bundle activator.
 * 
 * Registers the <code>sf.qof.QueryObjectFactoryService</code> service.
 * 
 * @see sf.qof.QueryObjectFactoryService
 */
public class QueryObjectFactoryActivator implements BundleActivator, ServiceFactory {

  protected volatile QueryObjectFactoryDelegator delegator = null;
  protected final Set<ClassLoader> trackedClassLoaders = Collections.synchronizedSet(new HashSet<ClassLoader>());
  protected volatile Logger logger;

  /**
   * Called when this bundle is started so the Framework can perform the
   * bundle-specific activities necessary to start this bundle. This method
   * can be used to register services or to allocate any resources that this
   * bundle needs.
   * 
   * <p>
   * This method must complete and return to its caller in a timely manner.
   * 
   * @param context The execution context of the bundle being started.
   * @throws java.lang.Exception If this method throws an exception, this
   *         bundle is marked as stopped and the Framework will remove this
   *         bundle's listeners, unregister all services registered by this
   *         bundle, and release all services used by this bundle.
   */
  public synchronized void start(BundleContext context) throws Exception {
    logger = LoggerFactory.getLogger(context);
    logger.open();
    delegator = new QueryObjectFactoryDelegator();
    context.registerService(QueryObjectFactoryService.class.getName(), this, null);
  }

  /**
   * Called when this bundle is stopped so the Framework can perform the
   * bundle-specific activities necessary to stop the bundle. In general, this
   * method should undo the work that the <code>BundleActivator.start</code>
   * method started. There should be no active threads that were started by
   * this bundle when this bundle returns. A stopped bundle must not call any
   * Framework objects.
   * 
   * <p>
   * This method must complete and return to its caller in a timely manner.
   * 
   * @param context The execution context of the bundle being stopped.
   * @throws java.lang.Exception If this method throws an exception, the
   *         bundle is still marked as stopped, and the Framework will remove
   *         the bundle's listeners, unregister all services registered by the
   *         bundle, and release all services used by the bundle.
   */
  public synchronized void stop(BundleContext context) throws Exception {
    for (ClassLoader classLoader : trackedClassLoaders) {
      delegator.unregisterTrackedClassLoader(classLoader);
    }
    trackedClassLoaders.clear();

    delegator = null; //NOPMD
    logger.close();
  }

  /**
   * Creates a new service object.
   * 
   * <p>
   * The Framework invokes this method the first time the specified
   * <code>bundle</code> requests a service object using the
   * <code>BundleContext.getService(ServiceReference)</code> method. The
   * service factory can then return a specific service object for each
   * bundle.
   * 
   * <p>
   * The Framework caches the value returned (unless it is <code>null</code>),
   * and will return the same service object on any future call to
   * <code>BundleContext.getService</code> from the same bundle.
   * 
   * <p>
   * The Framework will check if the returned service object is an instance of
   * all the classes named when the service was registered. If not, then
   * <code>null</code> is returned to the bundle.
   * 
   * @param bundle The bundle using the service.
   * @param registration The <code>ServiceRegistration</code> object for the
   *        service.
   * @return A service object that <strong>must </strong> be an instance of
   *         all the classes named when the service was registered.
   * @see BundleContext#getService
   */
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    QueryObjectFactoryServiceImpl service = new QueryObjectFactoryServiceImpl(bundle);
    
    return service;
  }

  /**
   * Releases a service object.
   * 
   * <p>
   * The Framework invokes this method when a service has been released by a
   * bundle. The service object may then be destroyed.
   * 
   * @param bundle The bundle releasing the service.
   * @param registration The <code>ServiceRegistration</code> object for the
   *        service.
   * @param service The service object returned by a previous call to the
   *        <code>ServiceFactory.getService</code> method.
   * @see BundleContext#ungetService
   */
  public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
    QueryObjectFactoryServiceImpl qofService = (QueryObjectFactoryServiceImpl) service;
    if (qofService.bundleClassLoader != null) {
      qofService.untrack(qofService.bundleClassLoader);
    }
  }

  /*
   * Implementation of QueryObjectFactoryService
   */
  private class QueryObjectFactoryServiceImpl implements QueryObjectFactoryService {

    private String bundleSignature;
    private ClassLoader bundleClassLoader;

    public QueryObjectFactoryServiceImpl(Bundle bundle) {
      this.bundleSignature = bundleSignature(bundle);
    }

    private String bundleSignature(Bundle bundle) {
      return bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]";
    }

    public <T> T createQueryObject(Class<T> queryDefinitionClass) {
      long start = System.currentTimeMillis();
      T queryObject;
      try {
        queryObject = delegator.createQueryObject_(queryDefinitionClass);
      } catch (RuntimeException e) {
        logError(bundleSignature + " createQueryObject(" + queryDefinitionClass.getName() + ") failed", e);
        throw e;
      }
      long end = System.currentTimeMillis();
      logDebug(bundleSignature + " createQueryObject(" + queryDefinitionClass.getName() + ") successful in "
          + (end - start) + "ms");
      return queryObject;
    }

    public <T> T createQueryObject(Class<T> queryDefinitionClass, Object... parameters) {
      long start = System.currentTimeMillis();
      T queryObject;
      try {
        queryObject = delegator.createQueryObject_(queryDefinitionClass, parameters);
      } catch (RuntimeException e) {
        logError(bundleSignature + " createQueryObject(" + queryDefinitionClass.getName() + ") failed", e);
        throw e;
      }
      long end = System.currentTimeMillis();
      logDebug(bundleSignature + " createQueryObject(" + queryDefinitionClass.getName() + ") successful in "
          + (end - start) + "ms");
      return queryObject;
    }

    public <T, S> T createQueryObjectFromSuperClass(Class<T> queryDefinitionClass, Class<S> superClass) {
      long start = System.currentTimeMillis();
      T queryObject;
      try {
        queryObject = delegator.createQueryObjectFromSuperClass_(queryDefinitionClass, superClass);
      } catch (RuntimeException e) {
        logError(bundleSignature + " createQueryObjectFromSuperClass(" + queryDefinitionClass.getName() + ") failed",
            e);
        throw e;
      }
      long end = System.currentTimeMillis();
      logDebug(bundleSignature + " createQueryObjectFromSuperClass(" + queryDefinitionClass.getName()
          + ") successful in " + (end - start) + "ms");
      return queryObject;
    }

    public <T, S> T createQueryObjectFromSuperClass(Class<T> queryDefinitionClass, Class<S> superClass,
        Object... parameters) {
      long start = System.currentTimeMillis();
      T queryObject;
      try {
        queryObject = delegator.createQueryObjectFromSuperClass_(queryDefinitionClass, superClass, parameters);
      } catch (RuntimeException e) {
        logError(bundleSignature + " createQueryObjectFromSuperClass(" + queryDefinitionClass.getName() + ") failed",
            e);
        throw e;
      }
      long end = System.currentTimeMillis();
      logDebug(bundleSignature + " createQueryObjectFromSuperClass(" + queryDefinitionClass.getName()
          + ") successful in " + (end - start) + "ms");
      return queryObject;
    }

    public void registerMapper(String type, MappingAdapter adapter) {
      ClassLoader classLoader = validateClassLoader();
      try {
        delegator.registerMapper_(classLoader, type, adapter);
      } catch (RuntimeException e) {
        logError(bundleSignature + " registerMapper(" + type + ", " + adapter.getClass().getName() + ") failed", e);
        throw e;
      }
      logDebug(bundleSignature + " registerMapper(" + type + ", " + adapter.getClass().getName() + ") successful");
    }

    public void unregisterMapper(String type) {
      ClassLoader classLoader = validateClassLoader();
      try {
        delegator.unregisterMapper_(classLoader, type);
      } catch (RuntimeException e) {
        logError(bundleSignature + " unregisterMapper(" + type + ") failed", e);
        throw e;
      }
      logDebug(bundleSignature + " unregisterMapper(" + type + ") successful");
    }
    
    public boolean isMapperRegistered(String type) {
      ClassLoader classLoader = validateClassLoader();
      boolean result;
      try {
        result = delegator.isMapperRegistered_(classLoader, type);
      } catch (RuntimeException e) {
        logError(bundleSignature + " isMapperRegistered(" + type + ") failed", e);
        throw e;
      }
      logDebug(bundleSignature + " isMapperRegistered(" + type + ") successful");
      return result;
    }

    public void setCustomizer(Customizer customizer) {
      ClassLoader classLoader = validateClassLoader();
      try {
        delegator.setCustomizer_(classLoader, customizer);
      } catch (RuntimeException e) {
        logError(bundleSignature + " setCustomizer(" + customizer.getClass().getName() + ") failed", e);
        throw e;
      }
      logDebug(bundleSignature + " setCustomizer(" + customizer.getClass().getName() + ") successful");
    }

    public void setDefaultCustomizer() {
      ClassLoader classLoader = validateClassLoader();
      try {
        delegator.setDefaultCustomizer_(classLoader);
      } catch (RuntimeException e) {
        logError(bundleSignature + " setDefaultCustomizer() failed", e);
        throw e;
      }
      logDebug(bundleSignature + " setDefaultCustomizer() successful");
    }

    public void setSQLDialect(SQLDialect dialect) {
      ClassLoader classLoader = validateClassLoader();
      try {
        delegator.setSQLDialect_(classLoader, dialect);
      } catch (RuntimeException e) {
        logError(bundleSignature + " setSQLDialect() failed", e);
        throw e;
      }
      logDebug(bundleSignature + " setSQLDialect(" + dialect.getClass().getName() + ") successful");
    }

    private void logDebug(String message) {
      logger.log(Logger.LOG_DEBUG, message);
    }

    private void logError(String message, Throwable exception) {
      logger.log(Logger.LOG_ERROR, message, exception);
    }

    protected synchronized void track(ClassLoader classLoader) {
      trackedClassLoaders.add(classLoader);
    }

    protected synchronized void untrack(ClassLoader classLoader) {
      trackedClassLoaders.remove(classLoader);
      if (delegator != null) {
        delegator.unregisterTrackedClassLoader(classLoader);
      }
    }

    private ClassLoader validateClassLoader() {
      ClassLoader classLoader = CallStackIntrospector.getCaller(2).getClassLoader();
      if (bundleClassLoader != null && bundleClassLoader != classLoader) {
        throw new RuntimeException("Invalid class loader - service called from different bundles");
      } else if (bundleClassLoader == null) {
        bundleClassLoader = classLoader;
        track(classLoader);
      }
      return classLoader;
    }
  }

  /*
   * Delegator object to access protected static methods in sf.qof.QueryObjectFactory
   */
  private static class QueryObjectFactoryDelegator extends QueryObjectFactory {

    public <T> T createQueryObject_(Class<T> queryDefinitionClass) {
      return createQueryObject(queryDefinitionClass);
    }

    public <T> T createQueryObject_(Class<T> queryDefinitionClass, Object... parameters) {
      return createQueryObject(queryDefinitionClass, parameters);
    }

    public <T, S> T createQueryObjectFromSuperClass_(Class<T> queryDefinitionClass, Class<S> superClass) {
      return createQueryObjectFromSuperClass(queryDefinitionClass, superClass);
    }

    public <T, S> T createQueryObjectFromSuperClass_(Class<T> queryDefinitionClass, Class<S> superClass,
        Object... parameters) {
      return createQueryObjectFromSuperClass(queryDefinitionClass, superClass, parameters);
    }

    public void registerMapper_(ClassLoader classLoader, String type, MappingAdapter adapter) {
      registerMapper(classLoader, type, adapter);
    }

    public void unregisterMapper_(ClassLoader classLoader, String type) {
      unregisterMapper(classLoader, type);
    }
    
    public boolean isMapperRegistered_(ClassLoader classLoader, String type) {
      return isMapperRegistered(classLoader, type);
    }

    public void setCustomizer_(ClassLoader classLoader, Customizer customizer) {
      setCustomizer(classLoader, customizer);
    }

    public void setDefaultCustomizer_(ClassLoader classLoader) {
      setDefaultCustomizer(classLoader);
    }

    public void setSQLDialect_(ClassLoader classLoader, SQLDialect dialect) {
      setSQLDialect(classLoader, dialect);
    }

    public void unregisterTrackedClassLoader(ClassLoader classLoader) {
      customizerMap.remove(classLoader);
      sqlDialectMap.remove(classLoader);
      MappingFactory.unregisterMappers(classLoader);
    }
  }
}
