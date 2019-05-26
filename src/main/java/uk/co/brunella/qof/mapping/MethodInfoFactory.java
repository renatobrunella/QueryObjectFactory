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
package uk.co.brunella.qof.mapping;

import net.sf.cglib.core.Signature;
import uk.co.brunella.qof.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class MethodInfoFactory {

    public static MethodInfo createMethodInfo(Method method) {
        Signature signature = ReflectionUtils.getMethodSignature(method);
        int modifiers = method.getModifiers();
        MethodParameterInfo[] parameterInfos = createParameterInfos(method);
        MethodParameterInfo[] collectionParameterInfos = createCollectionParameterInfos(parameterInfos);
        MethodReturnInfo returnInfo = createReturnInfos(method);

        return new MethodInfoImpl(signature, modifiers, parameterInfos, collectionParameterInfos, returnInfo, method.toGenericString());
    }

    private static MethodParameterInfo[] createParameterInfos(Method method) {
        Type[] genericTypes = method.getGenericParameterTypes();
        Class<?>[] types = method.getParameterTypes();
        MethodParameterInfo[] parameterInfos = new MethodParameterInfo[genericTypes.length];

        for (int i = 0; i < genericTypes.length; i++) {
            Class<?> type = types[i];
            Class<?> collectionType = ReflectionUtils.getCollectionType(genericTypes[i]);
            Class<?> collectionElementType;
            if (collectionType == null) {
                collectionElementType = null; //NOPMD
            } else {
                collectionElementType = ReflectionUtils.getCollectionParameterizedType(genericTypes[i]);
            }
            Class<?> arrayElementType = ReflectionUtils.getArrayComponentType(genericTypes[i]);
            parameterInfos[i] = new MethodParameterInfoImpl(i, type, collectionType, collectionElementType, arrayElementType);
        }

        return parameterInfos;
    }

    private static MethodParameterInfo[] createCollectionParameterInfos(MethodParameterInfo[] parameterInfos) {
        int num = 0;
        for (int i = 0; i < parameterInfos.length; i++) {
            if (parameterInfos[i].getCollectionType() != null) {
                num++;
            }
        }
        MethodParameterInfo[] collectionParameterInfos = new MethodParameterInfo[num];
        int index = 0;
        for (int i = 0; i < parameterInfos.length; i++) {
            if (parameterInfos[i].getCollectionType() != null) {
                collectionParameterInfos[index++] = parameterInfos[i];
            }
        }
        return collectionParameterInfos;
    }

    private static MethodReturnInfo createReturnInfos(Method method) {
        Class<?> type = method.getReturnType();
        Class<?> collectionType = ReflectionUtils.getCollectionType(method.getGenericReturnType());
        Class<?> collectionElementType;
        if (collectionType == null) {
            collectionElementType = null; //NOPMD
        } else {
            collectionElementType = ReflectionUtils.getCollectionParameterizedType(method.getGenericReturnType());
        }
        Class<?> mapKeyType = ReflectionUtils.getCollectionParameterizedKeyType(method.getGenericReturnType());
        return new MethodReturnInfoImpl(type, collectionType, collectionElementType, mapKeyType);
    }

    protected static class MethodInfoImpl implements MethodInfo {

        private Signature signature;
        private int modifiers;
        private MethodParameterInfo[] parameterInfos;
        private MethodParameterInfo[] collectionParameterInfos;
        private MethodReturnInfo returnInfo;
        private String description;
        public MethodInfoImpl(Signature signature, int modifiers, MethodParameterInfo[] parameterInfos,
                              MethodParameterInfo[] collectionParameterInfos, MethodReturnInfo returnInfo, String description) {
            super();
            this.signature = signature;
            this.modifiers = modifiers;
            this.parameterInfos = parameterInfos;
            this.collectionParameterInfos = collectionParameterInfos;
            this.returnInfo = returnInfo;
            this.description = description;
        }

        public Signature getSignature() {
            return signature;
        }

        public int getModifiers() {
            return modifiers;
        }

        public MethodParameterInfo[] getParameterInfos() {
            return parameterInfos;
        }

        public MethodReturnInfo getReturnInfo() {
            return returnInfo;
        }

        public MethodParameterInfo[] getCollectionParameterInfos() {
            return collectionParameterInfos;
        }

        public String toString() {
            return description;
        }

        public String getDescription() {
            return description;
        }
    }

    protected static class MethodParameterInfoImpl implements MethodParameterInfo {

        private int index;
        private Class<?> type;
        private Class<?> collectionType;
        private Class<?> collectionElementType;
        private Class<?> arrayElementType;

        public MethodParameterInfoImpl(int index, Class<?> type, Class<?> collectionType, Class<?> collectionElementType,
                                       Class<?> arrayElementType) {
            super();
            this.index = index;
            this.type = type;
            this.collectionType = collectionType;
            this.collectionElementType = collectionElementType;
            this.arrayElementType = arrayElementType;
        }

        public int getIndex() {
            return index;
        }

        public Class<?> getType() {
            return type;
        }

        public Class<?> getCollectionType() {
            return collectionType;
        }

        public Class<?> getCollectionElementType() {
            return collectionElementType;
        }

        public Class<?> getArrayElementType() {
            return arrayElementType;
        }
    }

    protected static class MethodReturnInfoImpl implements MethodReturnInfo {

        private Class<?> type;
        private Class<?> collectionType;
        private Class<?> collectionElementType;
        private Class<?> mapKeyType;

        public MethodReturnInfoImpl(Class<?> type, Class<?> collectionType, Class<?> collectionElementType,
                                    Class<?> mapKeyType) {
            super();
            this.type = type;
            this.collectionType = collectionType;
            this.collectionElementType = collectionElementType;
            this.mapKeyType = mapKeyType;
        }

        public Class<?> getType() {
            return type;
        }

        public Class<?> getCollectionType() {
            return collectionType;
        }

        public Class<?> getCollectionElementType() {
            return collectionElementType;
        }

        public Class<?> getMapKeyType() {
            return mapKeyType;
        }

    }
}
