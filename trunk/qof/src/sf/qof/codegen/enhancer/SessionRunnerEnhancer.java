/*
 * Copyright 2010 brunella ltd
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
package sf.qof.codegen.enhancer;

import static sf.qof.codegen.Constants.SIG_DefaultSessionRunner_execute;
import static sf.qof.codegen.Constants.SIG_DefaultSessionRunner_executeBeanManaged;
import static sf.qof.codegen.Constants.SIG_DefaultSessionRunner_executeContainerManaged;
import static sf.qof.codegen.Constants.SIG_TransactionRunnable_run;
import static sf.qof.codegen.Constants.TYPE_DefaultSessionRunner;
import static sf.qof.codegen.Constants.TYPE_Object;
import static sf.qof.codegen.Constants.*;
import static sf.qof.codegen.Constants.TYPE_SessionPolicy;
import static sf.qof.codegen.Constants.TYPE_TransactionRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.cglib.core.Block;
import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import sf.qof.codegen.EmitUtils;
import sf.qof.codegen.enhancer.QueryObjectClassEnhancer;
import sf.qof.session.TransactionManagementType;
import sf.qof.session.UseDefaultSessionRunner;
import sf.qof.session.UseSessionContext;
import sf.qof.util.DefineClassHelper;
import sf.qof.util.ReflectionUtils;

/**
 * <code>SessionRunnerEnhancer</code> enhances methods that are annotated
 * with <code>@UseDefaultSessionRunner</code>.
 * 
 * Annotated methods are wrapped in a <code>TransactionRunnable</code>
 * anonymous class and executed using the <code>DefaultSessionRunner</code>.
 * 
 * @see sf.qof.session.UseDefaultSessionRunner
 * @see sf.qof.session.DefaultSessionRunner
 * 
 * @since 1.1.0
 */
public class SessionRunnerEnhancer implements QueryObjectClassEnhancer {

  public <T> Class<T> enhance(Class<T> queryDefinitionClass, Class<T> superClass) {
    // find methods annotated with @UseDefaultSessionRunner
    List<Method> annotatedMethods = getAllAnnotatedMethods(queryDefinitionClass, superClass);
    if (!annotatedMethods.isEmpty()) {
      if (!queryDefinitionClass.isAnnotationPresent(UseSessionContext.class)) {
        throw new RuntimeException("UseDefaultSessionRunner requires UseSessionContext annotation");
      }
      return generateClass(queryDefinitionClass, superClass, annotatedMethods, queryDefinitionClass.getAnnotation(UseSessionContext.class));
    }
    
    return superClass;
  }

  private <T> List<Method> getAllAnnotatedMethods(Class<T> queryDefinitionClass, Class<T> superClass) {
    Set<Method> annotatedMethods = new HashSet<Method>();
    Class<?> clazz = queryDefinitionClass;
    while (clazz != null) {
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(UseDefaultSessionRunner.class)) {
          annotatedMethods.add(method);
        }
      }
      clazz = clazz.getSuperclass();
    }
    clazz = superClass;
    while (clazz != null) {
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(UseDefaultSessionRunner.class)) {
          annotatedMethods.add(method);
        }
      }
      clazz = clazz.getSuperclass();
    }
    
    return new ArrayList<Method>(annotatedMethods);
  }
  
  /*
   * Generates a class that inherits from the super class and implements all methods annotated by @UseDefaultSessionRunner
   */
  private <T> Class<T> generateClass(Class<T> queryDefinitionClass, Class<T> superClass, 
      List<Method> annotatedMethods, UseSessionContext sessionContext) {
    ClassWriter cw = new DebuggingClassWriter(true);
    ClassEmitter ce = new ClassEmitter(cw);

    ce.begin_class(Constants.V1_2, Constants.ACC_PUBLIC, getClassName(superClass), 
        Type.getType(superClass), null, "<generated>");

    addConstructors(ce, superClass);
    
    int index = 0;
    for (Method method : annotatedMethods) {
      Class<?> clazz = superClass;
      Method enhancedMethod = null;
      while (clazz != null) {
        try {
          enhancedMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
          break;
        } catch (Exception e) { }
        clazz = clazz.getSuperclass();
      }
      if (enhancedMethod == null) {
        throw new RuntimeException("Could not find matching method");
      }
      
      Signature sigAccessMethod = generateStaticAccessorMethod(ce, enhancedMethod, index);
      
      Type runnable = generateTransactionRunnable(queryDefinitionClass, ce.getClassType(), enhancedMethod, index, sigAccessMethod);

      UseDefaultSessionRunner annotation = method.getAnnotation(UseDefaultSessionRunner.class);
      generateEnhancedMethod(ce, enhancedMethod, sessionContext, annotation, runnable, sigAccessMethod);
      
      index++;
    }

    ce.end_class();

    try {
      return DefineClassHelper.defineClass(getClassName(superClass), cw.toByteArray(),
          queryDefinitionClass.getClassLoader());
    } catch (Exception e) {
      throw new RuntimeException("SessionRunnerEnhancer could not create new class", e);
    }
  }

  /*
   * Generate static access$x methods
   */
  private Signature generateStaticAccessorMethod(ClassEmitter ce, Method enhancedMethod, int index) {
    Class<?>[] params = enhancedMethod.getParameterTypes();
    org.objectweb.asm.Type[] paramTypes = new org.objectweb.asm.Type[params.length + 1];
    paramTypes[0] = ce.getClassType();
    for (int i = 0; i < params.length; i++) {
      paramTypes[i + 1] = org.objectweb.asm.Type.getType(params[i]);
    }

    Signature sigAccessMethod = new Signature("access$" + index, org.objectweb.asm.Type.getType(enhancedMethod.getReturnType()), paramTypes);
    
    Type[] exceptionTypes = getExceptionTypes(enhancedMethod);
    CodeEmitter co = ce.begin_method(Modifier.STATIC, sigAccessMethod, exceptionTypes, null);

    co.load_args(0, paramTypes.length);
    
    // use invokespecial to call
    co.invoke_constructor(Type.getType(enhancedMethod.getDeclaringClass()), ReflectionUtils.getMethodSignature(enhancedMethod));
    co.return_value();
    
    co.end_method();
    
    return sigAccessMethod;
  }

  /*
   * Generates a class implementing TransactionRunnable:
   * - constructor takes outer class instance and all parameters of the delegated method and stores
   *   them in final fields
   * - implementation of run method calls static access$x method in outer class 
   */
  private Type generateTransactionRunnable(Class<?> queryDefinitionClass, Type outerClass, Method enhancedMethod, int index, Signature sigAccessMethod) {
    String className = outerClass.getClassName() + "$" + (index + 1);
    
    Class<?>[] parameterTypes = enhancedMethod.getParameterTypes();
    ClassWriter cw = new DebuggingClassWriter(true);
    ClassEmitter ce = new ClassEmitter(cw);
    
    ce.begin_class(Constants.V1_2, 0, className, 
        null, new Type[] { TYPE_TransactionRunnable }, "<generated>");
    
    // create fields
    ce.declare_field(Constants.ACC_FINAL, "this$1", outerClass, null, null);
    for (int i = 0; i < parameterTypes.length; i++) {
      ce.declare_field(Constants.ACC_FINAL, "val$v" + i, Type.getType(parameterTypes[i]), null, null);
    }

    // create constructor
    Signature sigConstructor = getTransactionRunnableConstructorSignature(outerClass, enhancedMethod);
    CodeEmitter co = ce.begin_method(0, sigConstructor, null, null);

    co.load_this();
    co.load_arg(0);
    co.putfield("this$1");

    for (int i = 0; i < parameterTypes.length; i++) {
      co.load_this();
      co.load_arg(i + 1);
      co.putfield("val$v" + i);
    }
    
    co.load_this();
    co.invoke_constructor(ce.getSuperType(), new Signature("<init>", "()V"));
    co.return_value();
    co.end_method();

    // create run method
    // Object run(Connection connection, Object... arguments) throws SQLException;
    co = ce.begin_method(Constants.ACC_PUBLIC, SIG_TransactionRunnable_run, new Type[] { TYPE_SQLException }, null);

    Block tryBlock = co.begin_block();
    
    // return access$x(OuterClass.this, parameter, ...);
    co.load_this();
    co.getfield("this$1");

    for (int i = 0; i < parameterTypes.length; i++) {
      co.load_this();
      co.getfield("val$v" + i);
    }
    
    co.invoke_static(outerClass, sigAccessMethod);
    
    if (enhancedMethod.getReturnType() == Void.TYPE) {
      co.aconst_null();
    } else if (enhancedMethod.getReturnType().isPrimitive()) {
      EmitUtils.boxUsingValueOf(co, Type.getType(enhancedMethod.getReturnType()));
    }
    
    co.return_value();

    tryBlock.end();
    
    EmitUtils.emitCatchException(co, tryBlock, TYPE_SystemException);
    Local localException = co.make_local(TYPE_SystemException);
    Label labelNotSQLException = co.make_label();
    co.store_local(localException);

    co.load_local(localException);
    co.invoke_virtual(TYPE_SystemException, SIG_getCause);
    co.instance_of(TYPE_SQLException);
    co.if_jump(CodeEmitter.EQ, labelNotSQLException);
    
    co.load_local(localException);
    co.invoke_virtual(TYPE_SystemException, SIG_getCause);
    co.checkcast(TYPE_SQLException);
    co.athrow();
    
    co.mark(labelNotSQLException);
    co.new_instance(TYPE_SQLException);
    co.dup();
    co.load_local(localException);
    co.invoke_virtual(TYPE_SystemException, SIG_getMessage);
    co.invoke_constructor(TYPE_SQLException, new Signature("<init>", "(Ljava/lang/String;)V"));
    co.athrow();

    co.end_method();
    
    ce.end_class();

    try {
      DefineClassHelper.defineClass(className, cw.toByteArray(),
          queryDefinitionClass.getClassLoader());
    } catch (Exception e) {
      throw new RuntimeException("SessionRunnerEnhancer could not create new class", e);
    }
    
    return ce.getClassType();
  }
  
  private Signature getTransactionRunnableConstructorSignature(Type outerType, Method method) {
    Class<?>[] params = method.getParameterTypes();
    org.objectweb.asm.Type[] paramTypes = new org.objectweb.asm.Type[params.length + 1];
    paramTypes[0] = outerType;
    for (int i = 0; i < params.length; i++) {
      paramTypes[i + 1] = org.objectweb.asm.Type.getType(params[i]);
    }

    return new Signature("<init>", org.objectweb.asm.Type.VOID_TYPE, paramTypes);
  }

  /*
   * Generates enhanced method:
   * - Use DefaultSessionRunner.executeXYZ(new TransactionRunnable(), sessionContextName, sessionPolicy, transactionManagementType
   * - Exception handling: SystemException is unwrapped
   */
  private void generateEnhancedMethod(ClassEmitter ce, Method enhancedMethod, 
      UseSessionContext sessionContext, UseDefaultSessionRunner annotation, Type transactionRunnable, Signature sigAccessMethod) {
    Signature sigMethod = ReflectionUtils.getMethodSignature(enhancedMethod);

    Type[] exceptionTypes = getExceptionTypes(enhancedMethod);
    CodeEmitter co = ce.begin_method(enhancedMethod.getModifiers(), sigMethod, exceptionTypes, null);
    
    co.new_instance(transactionRunnable);
    co.dup();
    co.load_this();
    co.load_args();
    co.invoke_constructor(transactionRunnable, getTransactionRunnableConstructorSignature(ce.getClassType(), enhancedMethod));
    // SessionContext name
    co.push(sessionContext.name());
    co.getstatic(TYPE_SessionPolicy, annotation.sessionPolicy().name(), TYPE_SessionPolicy);
    co.push(0);
    co.newarray(TYPE_Object);

    if (annotation.transactionManagementType() == TransactionManagementType.BEAN) {
      co.invoke_static(TYPE_DefaultSessionRunner, SIG_DefaultSessionRunner_executeBeanManaged);
    } else if (annotation.transactionManagementType() == TransactionManagementType.CONTAINER) {
      co.invoke_static(TYPE_DefaultSessionRunner, SIG_DefaultSessionRunner_executeContainerManaged);
    } else {
      co.invoke_static(TYPE_DefaultSessionRunner, SIG_DefaultSessionRunner_execute);
    }
    if (enhancedMethod.getReturnType().isPrimitive()) {
      Type boxedType = Type.getType(ReflectionUtils.box(enhancedMethod.getReturnType()));
      co.checkcast(boxedType);
      EmitUtils.unboxUsingXValue(co, boxedType);
    } else {
      co.checkcast(Type.getType(enhancedMethod.getReturnType()));
    }
    
    co.return_value();
    
    co.end_method();
  }

  private Type[] getExceptionTypes(Method method) {
    Class<?>[] exceptions = method.getExceptionTypes();
    Type[] exceptionTypes = new Type[exceptions.length];
    for (int i = 0; i < exceptions.length; i++) {
      exceptionTypes[i] = Type.getType(exceptions[i]);
    }
    return exceptionTypes;
  }

  private String getClassName(Class<?> baseClass) {
    return baseClass.getName() + "$SubmissionRunner";
  }
  
  private void addConstructors(ClassEmitter ce, Class<?> superClass) {
    Constructor<?>[] superConstructors = superClass.getDeclaredConstructors();
    for (Constructor<?> superConstuctor : superConstructors) {
      addConstructor(ce, superConstuctor);
    }
  }

  private void addConstructor(ClassEmitter ce, Constructor<?> superConstructor) {
    Signature sigConstructor = ReflectionUtils.getConstructorSignature(superConstructor);
    CodeEmitter co = ce.begin_method(superConstructor.getModifiers(), sigConstructor, null, null);
    co.load_this();
    for (int i = 0; i < superConstructor.getParameterTypes().length; i++) {
      co.load_arg(i);
    }
    co.invoke_constructor(ce.getSuperType(), sigConstructor);
    co.return_value();
    co.end_method();
  }
  
}
