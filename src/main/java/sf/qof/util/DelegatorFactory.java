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

import static sf.qof.codegen.Constants.TYPE_boolean;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.core.Signature;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

/**
 * Factory class to create a delegator object for a given delegatee class.
 *
 * <p>
 * The factory creates wrappers for each public non-final method of the
 * delegatee class. Once any of these methods is called the delegatee factory is
 * used to initialize the delegatee object. The delegatee class must have a
 * default constructor.
 * </p>
 * <p>
 * The delegatee factory must implement a static <code>initialize</code>
 * method with the first argument being the delegatee object and the other
 * arguments matching the types of the <code>constructorParameters</code>:
 * </p>
 *
 * <pre>
 * public class PersonDelegateeFactory {
 *   public static void initialize(Person personDelegatee, Integer id) {
 *     String name = getNameForId(id);
 *     personDelegatee.setName(name);
 *   }
 * }
 *
 * // create a delegator for person and pass it the id of 1
 * Person person = DelegatorFactory&lt;Person&gt;(Person.class,
 *      PersonDelegateeFactory.class, 1);
 * // person is instanziated but not initialized
 * // a call to getName() or any other public method of person for the first time
 * // calls PersonDelegateeFactory.initialize(person, 1)
 * String name = person.getName();
 * // person is initialized now
 * </pre>
 *
 * <p>
 * Initialization of delegator objects is thread-safe.
 * </p>
 * <p>
 * <code>DelegatorFactory</code> can be used to implement lazy initialization
 * of objects
 * </p>
 * <p>
 * The generated delegatee objects super class is the delegatee class.
 * </p>
 */
public class DelegatorFactory {

    /**
     * Returns an instance of a delegator object for a delegatee class.
     *
     * @param <T>                   type of the delegatee object
     * @param delegateeClass        class of the delegatee object
     * @param delegateeFactory      factory to initialize the delegatee object
     * @param constructorParameters parameters that are passed to the delegatee factory
     * @return delegator object instance
     */
    public static <T> T create(Class<T> delegateeClass, Class<?> delegateeFactory, Object... constructorParameters) {
        @SuppressWarnings("unchecked") Class<T> delegatorClass = (Class<T>) ClassGenerationCache.getCachedClass(delegateeClass, delegateeFactory);
        if (delegatorClass == null) {
            try {
                // create new class
                delegatorClass = createClass(delegateeClass, delegateeFactory, constructorParameters);

                // put the newly created class
                ClassGenerationCache.putCachedClass(delegatorClass, delegateeClass, delegateeFactory);
            } catch (Throwable e) {
                ClassGenerationCache.putCachedClass(null, delegateeClass, delegateeFactory);
                throw new RuntimeException(e);
            }
        }

        // create instance
        @SuppressWarnings("unchecked") Class[] constructorParameterTypes = new Class[constructorParameters.length];
        for (int i = 0; i < constructorParameters.length; i++) {
            constructorParameterTypes[i] = constructorParameters[i].getClass();
        }
        try {
            return delegatorClass.getConstructor(constructorParameterTypes).newInstance(constructorParameters);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> Class<T> createClass(Class<T> delegateeClass, Class<?> delegateeFactory,
                                            Object... constructorParameters) throws Exception {

        DebuggingClassWriter cw = new DebuggingClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassEmitter ce = new ClassEmitter(cw);

        String className = delegateeClass.getName() + "$" + delegateeFactory.getName() + "$Delegator";
        if (className.startsWith("java")) {
            className = "$" + className;
        }

        Type[] delegateeInterfaces = getInterfaceTypes(delegateeClass);

        ce.begin_class(Constants.V1_2, Constants.ACC_PUBLIC, className, Type.getType(delegateeClass), delegateeInterfaces,
                "<generated>");

        createConstructorAndFields(ce, constructorParameters);

        createInitializeMethod(ce, delegateeFactory, delegateeClass, constructorParameters);
        createMethods(ce, delegateeClass);

        ce.end_class();

        ClassLoader classLoader = delegateeClass.getClassLoader();
        if (classLoader == null) {
            classLoader = DelegatorFactory.class.getClassLoader();
        }

        return DefineClassHelper.defineClass(className, cw.toByteArray(), classLoader);
    }

    private static void createConstructorAndFields(ClassEmitter ce, Object[] constructorParameters) {
        ce.declare_field(Constants.ACC_PRIVATE, "$$initialized", TYPE_boolean, false);
        for (int i = 0; i < constructorParameters.length; i++) {
            ce.declare_field(Constants.ACC_PRIVATE, "$$" + i, Type.getType(constructorParameters[i].getClass()), null);
        }

        Signature sigConstructor = new Signature("<init>", Type.VOID_TYPE, getTypes(constructorParameters));
        CodeEmitter co = ce.begin_method(Constants.ACC_PUBLIC, sigConstructor, null);
        co.load_this();
        co.invoke_constructor(ce.getSuperType(), new Signature("<init>", "()V"));
        // store constructor parameters
        for (int i = 0; i < constructorParameters.length; i++) {
            co.load_this();
            co.load_arg(i);
            co.putfield("$$" + i);
        }
        co.return_value();
        co.end_method();
    }

    private static void createMethods(ClassEmitter ce, Class<?> delegateeClass) {
        for (Method method : delegateeClass.getMethods()) {
            createMethod(ce, method);
        }
    }

    private static void createInitializeMethod(ClassEmitter ce, Class<?> delegateeFactory, Class<?> delegateeClass,
                                               Object[] constructorParameters) {
        CodeEmitter co = ce.begin_method(Constants.ACC_PRIVATE + Constants.ACC_SYNCHRONIZED, new Signature("$initialize",
                "()V"), null);
        co.load_this();
        co.getfield("$$initialized");
        Label labelInitialized = co.make_label();
        co.if_jump(CodeEmitter.NE, labelInitialized);

        co.load_this();
        co.push(true);
        co.putfield("$$initialized");
        co.load_this();
        StringBuilder sb = new StringBuilder();
        sb.append("(L").append(Type.getType(delegateeClass).getInternalName()).append(';');
        for (int i = 0; i < constructorParameters.length; i++) {
            sb.append('L').append(Type.getType(constructorParameters[i].getClass()).getInternalName()).append(';');
        }
        sb.append(")V");
        String desc = sb.toString();
        for (int i = 0; i < constructorParameters.length; i++) {
            co.load_this();
            co.getfield("$$" + i);
        }
        co.invoke_static(Type.getType(delegateeFactory), new Signature("initialize", desc));

        co.mark(labelInitialized);

        co.return_value();
        co.end_method();
    }

    private static void createMethod(ClassEmitter ce, Method method) {
        if (Modifier.isFinal(method.getModifiers())) {
            return;
        }
        Signature methodSignature = ReflectionUtils.getMethodSignature(method);
        CodeEmitter co = ce.begin_method(Constants.ACC_PUBLIC, methodSignature, getTypes(method.getExceptionTypes()));
        // call delegatee
        co.load_this();
        co.getfield("$$initialized");
        Label labelInitialized = co.make_label();
        co.if_jump(CodeEmitter.NE, labelInitialized);
        co.load_this();
        co.invoke_virtual_this(new Signature("$initialize", "()V"));
        co.mark(labelInitialized);
        co.load_this();
        int num = method.getParameterTypes().length;
        for (int i = 0; i < num; i++) {
            co.load_arg(i);
        }
        co.super_invoke();
        co.return_value();
        co.end_method();
    }

    private static Type[] getInterfaceTypes(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        return getTypes(interfaces);
    }

    private static Type[] getTypes(Class<?>[] classes) {
        Type[] types = new Type[classes.length];
        for (int i = 0; i < classes.length; i++) {
            types[i] = Type.getType(classes[i]);
        }
        return types;
    }

    private static Type[] getTypes(Object[] objs) {
        Type[] types = new Type[objs.length];
        for (int i = 0; i < objs.length; i++) {
            types[i] = Type.getType(objs[i].getClass());
        }
        return types;
    }
}
