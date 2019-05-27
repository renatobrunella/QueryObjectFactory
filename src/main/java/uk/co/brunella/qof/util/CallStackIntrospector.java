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
package uk.co.brunella.qof.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Utility class to retrieve call stack information.
 */
public class CallStackIntrospector {

    private static Method GET_CLASS_CONTEXT;
    private static SecurityManager SECURITY_MANAGER;

    static {
        SECURITY_MANAGER = System.getSecurityManager();
        if (SECURITY_MANAGER == null) {
            SECURITY_MANAGER = new SecurityManager();
        }

        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                Class<?> securityManagerClass;
                try {
                    securityManagerClass = Class.forName("java.lang.SecurityManager");
                    GET_CLASS_CONTEXT = securityManagerClass.getDeclaredMethod("getClassContext");
                    GET_CLASS_CONTEXT.setAccessible(true);
                } catch (ClassNotFoundException | SecurityException | NoSuchMethodException ignored) {
                }
                return null;
            }
        });
    }

    /**
     * Returns the current call stack as an array of <code>Class</code>.
     *
     * @return Array of classes
     */
    static Class<?>[] getCallStack() {
        if (GET_CLASS_CONTEXT != null) {
            try {
                return (Class<?>[]) GET_CLASS_CONTEXT.invoke(SECURITY_MANAGER, (Object[]) null);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return null;
    }

    /**
     * Returns the class of the caller.
     *
     * @return Caller class
     */
    public static Class<?> getCaller() {
        return getCaller(1);
    }

    /**
     * Returns the class of the caller at a given stack level.
     *
     * @param level Stack level starting at 0 for the immediate caller
     * @return Caller class at given level or <code>null</code>
     */
    static Class<?> getCaller(int level) {
        Class<?>[] callStack = getCallStack();
        int baseLevel = findBaseLevel(callStack);
        if (baseLevel + level + 1 < 0 || baseLevel + level + 1 >= callStack.length) {
            return null;
        } else {
            return callStack[baseLevel + level + 1];
        }
    }

    private static int findBaseLevel(Class<?>[] callStack) {
        for (int i = callStack.length - 1; i >= 0; i--) {
            if (callStack[i] == CallStackIntrospector.class) {
                return i;
            }
        }
        return -1;
    }
}
