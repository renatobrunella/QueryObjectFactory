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
package sf.qof.testtools;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class LoggingDelegationProxyFactory {

  public static Object createProxy(Object delegatee, Class<?>... interfaces) {
    return createProxy(null, delegatee, interfaces);
  }

  public static Object createProxy(LoggingDelegationProxy proxy, Object delegatee, Class<?>... interfaces) {
    Class<?>[] proxyInterfaces = new Class<?>[1 + interfaces.length];
    for (int i = 0; i < interfaces.length; i++) {
      proxyInterfaces[i] = interfaces[i];
    }
    proxyInterfaces[proxyInterfaces.length - 1] = LoggingDelegationProxy.class;
    List<String> log;
    if (proxy == null) {
      log = new ArrayList<String>();
    } else {
      log = proxy.getLog();
    }
    return Proxy.newProxyInstance(LoggingDelegationProxyFactory.class.getClassLoader(), proxyInterfaces,
        new LoggingDelegationProxyInvocationHandler(delegatee, log));
  }

  private static class LoggingDelegationProxyInvocationHandler implements InvocationHandler {

    private Object delegatee;
    private List<String> log;
    private boolean logClass;

    public LoggingDelegationProxyInvocationHandler(Object delegatee, List<String> log) {
      this.delegatee = delegatee;
      this.log = log;
      logClass = false;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getName().equals("getLog")) {
        return log;
      } else if (method.getName().equals("clearLog")) {
        log.clear();
        return null;
      } else if (method.getName().equals("setLogClass")) {
        logClass = (Boolean) args[0];
        return null;
      } else {
        StringBuilder sb = new StringBuilder();
        if (logClass) {
          sb.append(method.getDeclaringClass().getName());
          sb.append('.');
        }
        sb.append(method.getName());
        sb.append('(');
        if (args != null) {
          for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i + 1 < args.length) {
              sb.append(',');
            }
          }
        }
        sb.append(')');
        log.add(sb.toString());
        try {
          Method delegateeMethod = delegatee.getClass().getMethod(method.getName(), method.getParameterTypes());
          return delegateeMethod.invoke(delegatee, args);
        } catch (NoSuchMethodException e) {
          return null;
        } catch (InvocationTargetException e) {
          throw e.getCause();
        } 
      }
    }
  }

}
