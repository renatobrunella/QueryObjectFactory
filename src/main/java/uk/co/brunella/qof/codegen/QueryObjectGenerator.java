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
package uk.co.brunella.qof.codegen;

import net.sf.cglib.core.Constants;
import net.sf.cglib.core.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import uk.co.brunella.qof.BaseQuery;
import uk.co.brunella.qof.Paging;
import uk.co.brunella.qof.adapter.DynamicMappingAdapter;
import uk.co.brunella.qof.adapter.MappingAdapter;
import uk.co.brunella.qof.customizer.Customizer;
import uk.co.brunella.qof.dialect.SQLDialect;
import uk.co.brunella.qof.exception.ValidationException;
import uk.co.brunella.qof.mapping.AdapterMapping;
import uk.co.brunella.qof.mapping.Mapper;
import uk.co.brunella.qof.mapping.Mapping;
import uk.co.brunella.qof.mapping.QueryType;
import uk.co.brunella.qof.util.DefineClassHelper;
import uk.co.brunella.qof.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.*;

/**
 * Internal - QueryObjectGenerator is the main generator class for query objects.
 */
public class QueryObjectGenerator {

    /**
     * Default fetch size used if not defined in query definition class.
     */
    public static final int DEFAULT_FETCH_SIZE = 100;

    /**
     * Default batch size used if not defined in query definition class.
     */
    public static final int DEFAULT_BATCH_SIZE = 100;

    private Customizer customizer;
    private Class<?> queryDefinitionClass;
    private Class<?> superClass;
    private String classNameType;
    private Map<String, FieldInfo> fields = new HashMap<String, FieldInfo>();
    private SQLDialect sqlDialect;
    private boolean implementPaging;
    private Method postGetConnectionMethod;

    public QueryObjectGenerator(Customizer customizer, SQLDialect sqlDialect) {
        this.customizer = customizer;
        this.sqlDialect = sqlDialect;
    }

    public SQLDialect getSqlDialect() {
        return sqlDialect;
    }

    public boolean getImplementPaging() {
        return implementPaging;
    }

    public Method getPostGetConnectionMethod() {
        return postGetConnectionMethod;
    }

    public String getClassNameType() {
        return classNameType;
    }

    public Customizer getCustomizer() {
        return customizer;
    }

    public <T> Class<T> create(Class<T> queryDefinitionClass, List<Mapper> mappers) {
        return create(queryDefinitionClass, mappers, Object.class);
    }

    public <T> Class<T> create(Class<T> queryDefinitionClass, List<Mapper> mappers, Class<?> superClass) {
        this.queryDefinitionClass = queryDefinitionClass;
        this.superClass = superClass;
        implementPaging = Paging.class.isAssignableFrom(queryDefinitionClass);
        postGetConnectionMethod = findPostGetConnectionMethod(queryDefinitionClass);
        try {
            String className = customizer.getClassName(queryDefinitionClass);
            classNameType = createClassNameType(className);

            if (debugLocation != null) {
                printDebugInfo(queryDefinitionClass, mappers);
            }

            DebuggingClassWriter cw = new DebuggingClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            ClassEmitter ce = new ClassEmitter(cw);

            beginClass(ce);
            addStaticInitializer(ce, mappers);
            addConstructorAndFields(ce);
            addBaseQueryMethods(ce);
            if (implementPaging) {
                addPagingMethods(ce);
            }
            addToString(ce);
            for (Mapper mapper : mappers) {
                addQueryMethod(ce, mapper);
            }
            endClass(ce);

            return DefineClassHelper.defineClass(className, cw.toByteArray(), queryDefinitionClass.getClassLoader());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createClassNameType(String className) {
        return "L" + className.replace('.', '/') + ";";
    }

    private static final String DEBUG_LOCATION_PROPERTY = "cglib.debugLocation";

    private static String debugLocation;

    static {
        debugLocation = System.getProperty(DEBUG_LOCATION_PROPERTY);
    }

    private void printDebugInfo(Class<?> queryDefinitionClass, List<Mapper> mappers) {
        String dirs = customizer.getClassName(queryDefinitionClass).replace('.', File.separatorChar);
        try {
            File file = new File(new File(debugLocation), dirs + ".map");
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new RuntimeException("Could not create directory " + file.getParentFile());
            }

            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                for (Mapper mapper : mappers) {
                    mapper.printMappingInfo(out);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void beginClass(ClassEmitter ce) {
        List<Type> interfaceTypes = new ArrayList<Type>();
        interfaceTypes.add(Type.getType(BaseQuery.class));
        if (queryDefinitionClass.isInterface()) {
            interfaceTypes.add(Type.getType(queryDefinitionClass));
        }
        if (implementPaging) {
            interfaceTypes.add(Type.getType(Paging.class));
        }
        ce.begin_class(Constants.V1_2, Constants.ACC_PUBLIC, customizer.getClassName(queryDefinitionClass), Type
                .getType(superClass), (Type[]) interfaceTypes.toArray(new Type[interfaceTypes.size()]), "<generated>");
    }

    private void endClass(ClassEmitter ce) {
        ce.end_class();
    }

    private void addQueryMethod(ClassEmitter ce, Mapper mapper) {
        CodeEmitter co;
        // int numberOfPersons() throws SQLException

        int access = Constants.ACC_PUBLIC;
        if (Modifier.isProtected(mapper.getMethod().getModifiers())) {
            access = Constants.ACC_PROTECTED;
        }
        co = ce.begin_method(access, mapper.getMethod().getSignature(), new Type[]{uk.co.brunella.qof.codegen.Constants.TYPE_SQLException});

        QueryType queryType = mapper.getQueryType();
        if (queryType == QueryType.QUERY) {
            SelectQueryMethodGenerator.addSelectQueryBody(co, this, mapper);
        } else if (queryType == QueryType.INSERT || queryType == QueryType.UPDATE || queryType == QueryType.DELETE) {
            InsertUpdateDeleteQueryMethodGenerator.addInsertUpdateDeleteQueryBody(co, this, mapper);
        } else if (queryType == QueryType.CALL) {
            CallQueryMethodGenerator.addCallQueryBody(co, this, mapper);
        } else {
            throw new RuntimeException("Not supported query type: " + queryType);
        }

        co.end_method();
    }

    private void addToString(ClassEmitter ce) {
        CodeEmitter co;
        co = ce.begin_method(Constants.ACC_PUBLIC, uk.co.brunella.qof.codegen.Constants.SIG_toString, null);
        co.push("QueryObject generated from " + queryDefinitionClass.getName());
        co.return_value();
        co.end_method();
    }

    private void addBaseQueryMethods(ClassEmitter ce) {
        customizer.getConnectionFactoryCustomizer(queryDefinitionClass).emitGetConnection(queryDefinitionClass, superClass, ce);
        customizer.getConnectionFactoryCustomizer(queryDefinitionClass).emitUngetConnection(queryDefinitionClass, superClass, ce);
        customizer.getConnectionFactoryCustomizer(queryDefinitionClass).emitSetConnection(queryDefinitionClass, superClass, ce);
        addGetterAndSetter(ce, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_FETCH_SIZE, "I");
        addGetterAndSetter(ce, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_BATCH_SIZE, "I");
    }

    private void addPagingMethods(ClassEmitter ce) {
        addFieldIfNeeded(ce, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_FIRST_RESULT, uk.co.brunella.qof.codegen.Constants.TYPE_int);
        addFieldIfNeeded(ce, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_MAX_RESULTS, uk.co.brunella.qof.codegen.Constants.TYPE_int);

        addSetterForPaging(ce, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_FIRST_RESULT, "I");
        addSetterForPaging(ce, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_MAX_RESULTS, "I");
    }

    private void addGetterAndSetter(ClassEmitter ce, String fieldName, String fieldType) {
        addGetter(ce, fieldName, fieldType);
        addSetter(ce, fieldName, fieldType);
    }

    private void addSetterForPaging(ClassEmitter ce, String fieldName, String fieldType) {
        CodeEmitter co;
        Signature signature;
        // void setField(type)
        signature = new Signature("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), "("
                + fieldType + ")Luk/co/brunella/qof/Paging;");
        co = ce.begin_method(Constants.ACC_PUBLIC, signature, null);
        co.load_this();
        co.load_arg(0);
        emitPutField(co, fieldName);
        co.load_this();
        co.return_value();
        co.end_method();
    }

    private void addSetter(ClassEmitter ce, String fieldName, String fieldType) {
        CodeEmitter co;
        Signature signature;
        // void setField(type)
        signature = new Signature("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), "("
                + fieldType + ")V");
        co = ce.begin_method(Constants.ACC_PUBLIC, signature, null);
        co.load_this();
        co.load_arg(0);
        emitPutField(co, fieldName);
        co.return_value();
        co.end_method();
    }

    private void addGetter(ClassEmitter ce, String fieldName, String fieldType) {
        CodeEmitter co;
        Signature signature;
        // type getField()
        signature = new Signature("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), "()"
                + fieldType);
        co = ce.begin_method(Constants.ACC_PUBLIC, signature, null);
        co.load_this();
        emitGetField(co, fieldName);
        co.return_value();
        co.end_method();
    }

    private void addStaticInitializer(ClassEmitter ce, List<Mapper> mappers) {
        // create a private static final field for each dynamic adapter
        Set<Class<?>> dynamicAdapters = getDynamicAdapterClasses(mappers);
        if (dynamicAdapters.size() > 0) {
            // static initializer
            CodeEmitter co = ce.begin_static();
            for (Class<?> dynamicAdapterClass : dynamicAdapters) {
                String fieldName = getAdapterFieldName(dynamicAdapterClass);
                Type fieldType = Type.getType(dynamicAdapterClass);
                ce.declare_field(Constants.PRIVATE_FINAL_STATIC, fieldName, fieldType, null);
                co.new_instance(fieldType);
                co.dup();
                co.invoke_constructor(fieldType);
                co.putfield(fieldName);
            }
            co.return_value();
            co.end_method();
        }
    }

    private void addConstructorAndFields(ClassEmitter ce) {
        // fields
        if (!isFieldInDefinitionClass(uk.co.brunella.qof.codegen.Constants.FIELD_NAME_DEFAULT_BATCH_SIZE)) {
            ce.declare_field(Constants.ACC_PUBLIC + Constants.ACC_FINAL + Constants.ACC_STATIC,
                    uk.co.brunella.qof.codegen.Constants.FIELD_NAME_DEFAULT_BATCH_SIZE, uk.co.brunella.qof.codegen.Constants.TYPE_int, Integer.valueOf(DEFAULT_BATCH_SIZE));
        }
        if (!isFieldInDefinitionClass(uk.co.brunella.qof.codegen.Constants.FIELD_NAME_DEFAULT_FETCH_SIZE)) {
            ce.declare_field(Constants.ACC_PUBLIC + Constants.ACC_FINAL + Constants.ACC_STATIC,
                    uk.co.brunella.qof.codegen.Constants.FIELD_NAME_DEFAULT_FETCH_SIZE, uk.co.brunella.qof.codegen.Constants.TYPE_int, Integer.valueOf(DEFAULT_FETCH_SIZE));
        }

        customizer.getConnectionFactoryCustomizer(queryDefinitionClass).emitFields(queryDefinitionClass, superClass, ce);

        addFieldIfNeeded(ce, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_BATCH_SIZE, uk.co.brunella.qof.codegen.Constants.TYPE_int);
        addFieldIfNeeded(ce, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_FETCH_SIZE, uk.co.brunella.qof.codegen.Constants.TYPE_int);

        CodeEmitter co;
        // init method
        co = ce.begin_method(Constants.ACC_PUBLIC, uk.co.brunella.qof.codegen.Constants.SIG_init, null);
        co.load_this();
        if (isFieldInDefinitionClass(uk.co.brunella.qof.codegen.Constants.FIELD_NAME_DEFAULT_BATCH_SIZE)) {
            co.push(getFieldValue(uk.co.brunella.qof.codegen.Constants.FIELD_NAME_DEFAULT_BATCH_SIZE));
        } else {
            co.push(DEFAULT_BATCH_SIZE);
        }
        emitPutField(co, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_BATCH_SIZE);
        co.load_this();
        if (isFieldInDefinitionClass(uk.co.brunella.qof.codegen.Constants.FIELD_NAME_DEFAULT_FETCH_SIZE)) {
            co.push(getFieldValue(uk.co.brunella.qof.codegen.Constants.FIELD_NAME_DEFAULT_FETCH_SIZE));
        } else {
            co.push(DEFAULT_FETCH_SIZE);
        }
        emitPutField(co, uk.co.brunella.qof.codegen.Constants.FIELD_NAME_FETCH_SIZE);
        co.return_value();
        co.end_method();

        Constructor<?>[] superConstructors = superClass.getDeclaredConstructors();
        for (Constructor<?> superConstuctor : superConstructors) {
            addConstructor(ce, superConstuctor);
        }
    }

    private void addConstructor(ClassEmitter ce, Constructor<?> superConstructor) {
        Signature sigConstructor = ReflectionUtils.getConstructorSignature(superConstructor);
        CodeEmitter co = ce.begin_method(superConstructor.getModifiers(), sigConstructor, null);
        co.load_this();
        for (int i = 0; i < superConstructor.getParameterTypes().length; i++) {
            co.load_arg(i);
        }
        co.invoke_constructor(ce.getSuperType(), sigConstructor);
        // call init
        co.load_this();
        co.invoke_virtual_this(uk.co.brunella.qof.codegen.Constants.SIG_init);
        co.return_value();
        co.end_method();
    }

    private void addFieldIfNeeded(ClassEmitter ce, String fieldName, Type fieldType) {
        Field field = null;
        try {
            field = superClass.getDeclaredField(fieldName);
            if (!Type.getType(field.getType()).equals(fieldType)) {
                throw new ValidationException("Class has field '" + fieldName + "' that is wrong type");
            }
            if (Modifier.isPrivate(field.getModifiers())) {
                // warning?
                throw new ValidationException("Class has private field '" + fieldName + "'");
            }
        } catch (SecurityException e) {
            //NOPMD ignore
        } catch (NoSuchFieldException e) {
            //NOPMD ignore
        }
        if (field == null) {
            ce.declare_field(Constants.ACC_PRIVATE, fieldName, fieldType, null);
            fields.put(fieldName, new FieldInfo(/*Constants.ACC_PRIVATE,*/ fieldName, ce.getClassType(), fieldType));
        } else {
            fields.put(fieldName, new FieldInfo(/*field.getModifiers(),*/ fieldName, Type.getType(superClass), fieldType));
        }
    }

    private Set<Class<?>> getDynamicAdapterClasses(List<Mapper> mappers) {
        Set<Class<?>> dynamicAdapters = new HashSet<Class<?>>();
        for (Mapper mapper : mappers) {
            if (mapper.getParameters() != null) {
                for (Mapping mapping : mapper.getParameters()) {
                    if (mapping instanceof AdapterMapping) {
                        MappingAdapter adapter = ((AdapterMapping) mapping).getAdapter();
                        if (adapter instanceof DynamicMappingAdapter) {
                            dynamicAdapters.add(adapter.getClass());
                        }
                    }
                }
            }
            if (mapper.getResults() != null) {
                for (Mapping mapping : mapper.getResults()) {
                    if (mapping instanceof AdapterMapping) {
                        MappingAdapter adapter = ((AdapterMapping) mapping).getAdapter();
                        if (adapter instanceof DynamicMappingAdapter) {
                            dynamicAdapters.add(adapter.getClass());
                        }
                    }
                }
            }
        }
        return dynamicAdapters;
    }

    public static String getAdapterFieldName(Class<?> adapterClass) {
        return adapterClass.getName().replace('.', '$');
    }

    private boolean isFieldInDefinitionClass(String name) {
        Field field;
        try {
            field = queryDefinitionClass.getField(name);
            if (field.getType() != int.class) {
                throw new ValidationException("Field " + name + " must be of type int");
            }
            return true;
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private int getFieldValue(String name) {
        Field field;
        try {
            field = queryDefinitionClass.getField(name);
            if (field.getType() != int.class) {
                throw new ValidationException("Field " + name + " must be of type int");
            }
            return field.getInt(queryDefinitionClass);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class FieldInfo {
        String name;
        Type type;
        Type owner;

        public FieldInfo(String name, Type owner, Type type) {
            this.name = name;
            this.owner = owner;
            this.type = type;
        }
    }

    public void emitGetField(CodeEmitter co, String fieldName) {
        FieldInfo info = fields.get(fieldName);
        co.getfield(info.owner, info.name, info.type);
    }

    public void emitPutField(CodeEmitter co, String fieldName) {
        FieldInfo info = fields.get(fieldName);
        co.putfield(info.owner, info.name, info.type);
    }

    private Method findPostGetConnectionMethod(Class<?> queryDefinitionClass) {
        Class<?> clazz = queryDefinitionClass;
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(uk.co.brunella.qof.codegen.Constants.SIG_postGetConnection.getName(), Connection.class);
                return method;
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            }

            clazz = clazz.getSuperclass();
        }
        return null;
    }

}
