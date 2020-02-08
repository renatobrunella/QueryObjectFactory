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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Helper class to instantiate an object.
 */
public final class ObjectInstantiator {

    private ObjectInstantiator() {
    }

    /**
     * Creates a new instance of <code>clazz</code> using the given parameters.
     * If no constructor for the given parameters can be found in
     * <code>clazz</code> an exception is thrown.
     *
     * @param <T>      object type
     * @param clazz    class type
     * @param initArgs constructor arguments
     * @return an instance of clazz
     * @throws RuntimeException instantiation failed
     */
    public static <T> T newInstance(Class<T> clazz, Object[] initArgs) {
        if (initArgs == null || initArgs.length == 0) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] constructorParams = constructor.getParameterTypes();
                if (constructorParams.length == initArgs.length) {
                    boolean match = true;
                    for (int i = 0; i < constructorParams.length && match; i++) {
                        if (initArgs[i] != null) {
                            Class<?> constructorParam = constructorParams[i];
                            Class<?> initArg = initArgs[i].getClass();
                            if (constructorParam.isPrimitive()) {
                                initArg = ReflectionUtils.unbox(initArg);
                            }
                            match = constructorParam.isAssignableFrom(initArg);
                        }
                    }
                    if (match) {
                        try {
                            @SuppressWarnings("unchecked") T newInstance = (T) constructor.newInstance(initArgs);
                            return newInstance;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Cannot find matching constructor");
    }
}
