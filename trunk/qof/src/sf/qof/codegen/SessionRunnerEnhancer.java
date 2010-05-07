package sf.qof.codegen;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.core.Signature;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import sf.qof.session.UseDefaultSessionRunner;
import sf.qof.session.UseSessionContext;
import sf.qof.util.DefineClassHelper;
import sf.qof.util.ReflectionUtils;

public class SessionRunnerEnhancer implements Enhancer {

  public <T> Class<T> enhance(Class<T> queryDefinitionClass, Class<T> superClass) {
    
    List<Method> annotatedMethods = getAnnotatedMethods(queryDefinitionClass, superClass);
    
    if (!annotatedMethods.isEmpty()) {
      if (!queryDefinitionClass.isAnnotationPresent(UseSessionContext.class)) {
        throw new RuntimeException("UseDefaultSessionRunner requires UseSessionContext annotation");
      }
      return generateClass(queryDefinitionClass, superClass, annotatedMethods);
    }
    
    return superClass;
  }

  private <T> List<Method> getAnnotatedMethods(Class<T> queryDefinitionClass, Class<T> superClass) {
    List<Method> annotatedMethods = new ArrayList<Method>(); 
    for (Method method : queryDefinitionClass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(UseDefaultSessionRunner.class)) {
        annotatedMethods.add(method);
      }
    }
    return annotatedMethods;
  }
  
  private <T> Class<T> generateClass(Class<T> queryDefinitionClass, Class<T> superClass, List<Method> annotatedMethods) {
    ClassWriter cw = new DebuggingClassWriter(true);
    ClassEmitter ce = new ClassEmitter(cw);

    ce.begin_class(Constants.V1_2, Constants.ACC_PUBLIC, getClassName(superClass), 
        Type.getType(superClass), null, "<generated>");

    addConstructors(ce, superClass);
    
    int index = 0;
    for (Method method : annotatedMethods) {
      UseDefaultSessionRunner annotation = method.getAnnotation(UseDefaultSessionRunner.class);
      try {
        Method superMethod = superClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
        generateStaticAccessorMethod(ce, superClass, superMethod, index);
        generateSessionRunnerMethod(ce, superClass, superMethod, annotation, index);
      } catch (Exception e) {
        throw new RuntimeException("Could not find matching method", e);
      }
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

  private void generateStaticAccessorMethod(ClassEmitter ce, Class<?> superClass, Method superMethod, int index) {
    Class<?>[] params = superMethod.getParameterTypes();
    org.objectweb.asm.Type[] paramTypes = new org.objectweb.asm.Type[params.length + 1];
    paramTypes[0] = org.objectweb.asm.Type.getType(superClass);
    for (int i = 0; i < params.length; i++) {
      paramTypes[i + 1] = org.objectweb.asm.Type.getType(params[i]);
    }

    Signature sigMethod = new Signature("access$" + index, org.objectweb.asm.Type.getType(superMethod.getReturnType()), paramTypes);

    
    //TODO Exceptions: Do they need to be defined???
    CodeEmitter co = ce.begin_method(Modifier.STATIC, sigMethod, null, null);

    for (int i = 0; i < paramTypes.length; i++) {
      co.load_arg(i);
    }
    // use this to call invokespecial
    System.out.println(paramTypes[0]);
    co.invoke_constructor(paramTypes[0], ReflectionUtils.getMethodSignature(superMethod));
    co.return_value();
    
    co.end_method();
  }

  private void generateSessionRunnerMethod(ClassEmitter ce, Class<?> superClass, Method superMethod, UseDefaultSessionRunner annotation, int index) {
    Signature sigMethod = ReflectionUtils.getMethodSignature(superMethod);
    //TODO Exceptions: Do they need to be defined???
    CodeEmitter co = ce.begin_method(superMethod.getModifiers(), sigMethod, null, null);
    
    co.aconst_null();
    co.return_value();
    
    co.end_method();
  }

  private String getClassName(Class<?> baseClass) {
    return baseClass.getName() + "$SubmissionRunner";
  }
  
  private void addConstructors(ClassEmitter ce, Class<?> superClass) {
    Constructor<?>[] superConstructors = superClass.getDeclaredConstructors();
    for (Constructor<?> superConstuctor : superConstructors) {
      addConstrcutor(ce, superConstuctor);
    }
  }

  private void addConstrcutor(ClassEmitter ce, Constructor<?> superConstructor) {
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
