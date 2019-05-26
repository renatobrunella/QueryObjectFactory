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
package sf.qof.codegen;

import static sf.qof.codegen.Constants.SIG_Boolean_booleanValue;
import static sf.qof.codegen.Constants.SIG_Boolean_valueOf;
import static sf.qof.codegen.Constants.SIG_Byte_byteValue;
import static sf.qof.codegen.Constants.SIG_Byte_valueOf;
import static sf.qof.codegen.Constants.SIG_Character_charValue;
import static sf.qof.codegen.Constants.SIG_Character_valueOf;
import static sf.qof.codegen.Constants.SIG_Double_doubleValue;
import static sf.qof.codegen.Constants.SIG_Double_valueOf;
import static sf.qof.codegen.Constants.SIG_Float_floatValue;
import static sf.qof.codegen.Constants.SIG_Float_valueOf;
import static sf.qof.codegen.Constants.SIG_Integer_intValue;
import static sf.qof.codegen.Constants.SIG_Integer_valueOf;
import static sf.qof.codegen.Constants.SIG_Long_longValue;
import static sf.qof.codegen.Constants.SIG_Long_valueOf;
import static sf.qof.codegen.Constants.SIG_Short_shortValue;
import static sf.qof.codegen.Constants.SIG_Short_valueOf;
import static sf.qof.codegen.Constants.SIG_close;
import static sf.qof.codegen.Constants.SIG_getConnection;
import static sf.qof.codegen.Constants.SIG_postGetConnection;
import static sf.qof.codegen.Constants.SIG_ungetConnection;
import static sf.qof.codegen.Constants.TYPE_Boolean;
import static sf.qof.codegen.Constants.TYPE_Byte;
import static sf.qof.codegen.Constants.TYPE_Character;
import static sf.qof.codegen.Constants.TYPE_Double;
import static sf.qof.codegen.Constants.TYPE_Float;
import static sf.qof.codegen.Constants.TYPE_Integer;
import static sf.qof.codegen.Constants.TYPE_Long;
import static sf.qof.codegen.Constants.TYPE_Short;
import static sf.qof.codegen.Constants.TYPE_boolean;
import static sf.qof.codegen.Constants.TYPE_byte;
import static sf.qof.codegen.Constants.TYPE_char;
import static sf.qof.codegen.Constants.TYPE_double;
import static sf.qof.codegen.Constants.TYPE_float;
import static sf.qof.codegen.Constants.TYPE_int;
import static sf.qof.codegen.Constants.TYPE_long;
import static sf.qof.codegen.Constants.TYPE_short;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.sf.cglib.core.Block;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import sf.qof.mapping.Mapper;

/**
 * Internal - Contains utility functions for code emitters. 
 */
public class EmitUtils {

  private EmitUtils() {
  }

  /**
   * Emits code to call the static <code>valueOf</code> method of the
   * boxed types of primitives. 
   * 
   * @param co             the code emitter
   * @param primitiveType  primitive type
   */
  public static void boxUsingValueOf(CodeEmitter co, Type primitiveType) {
    if (primitiveType.equals(TYPE_byte)) {
      co.invoke_static(TYPE_Byte, SIG_Byte_valueOf);
    } else if (primitiveType.equals(TYPE_short)) {
      co.invoke_static(TYPE_Short, SIG_Short_valueOf);
    } else if (primitiveType.equals(TYPE_int)) {
      co.invoke_static(TYPE_Integer, SIG_Integer_valueOf);
    } else if (primitiveType.equals(TYPE_long)) {
      co.invoke_static(TYPE_Long, SIG_Long_valueOf);
    } else if (primitiveType.equals(TYPE_float)) {
      co.invoke_static(TYPE_Float, SIG_Float_valueOf);
    } else if (primitiveType.equals(TYPE_double)) {
      co.invoke_static(TYPE_Double, SIG_Double_valueOf);
    } else if (primitiveType.equals(TYPE_char)) {
      co.invoke_static(TYPE_Character, SIG_Character_valueOf);
    } else if (primitiveType.equals(TYPE_boolean)) {
      co.invoke_static(TYPE_Boolean, SIG_Boolean_valueOf);
    }
  }

  /**
   * Emits code to call the <code>xyzValue</code> method of the boxed
   * types of numbers
   * 
   * @param co             the code emitter
   * @param boxedType      the boxed type
   */
  public static void unboxUsingXValue(CodeEmitter co, Type boxedType) {
    if (boxedType.equals(TYPE_Byte)) {
      co.invoke_virtual(TYPE_Byte, SIG_Byte_byteValue);
    } else if (boxedType.equals(TYPE_Short)) {
      co.invoke_virtual(TYPE_Short, SIG_Short_shortValue);
    } else if (boxedType.equals(TYPE_Integer)) {
      co.invoke_virtual(TYPE_Integer, SIG_Integer_intValue);
    } else if (boxedType.equals(TYPE_Long)) {
      co.invoke_virtual(TYPE_Long, SIG_Long_longValue);
    } else if (boxedType.equals(TYPE_Float)) {
      co.invoke_virtual(TYPE_Float, SIG_Float_floatValue);
    } else if (boxedType.equals(TYPE_Double)) {
      co.invoke_virtual(TYPE_Double, SIG_Double_doubleValue);
    } else if (boxedType.equals(TYPE_Character)) {
      co.invoke_virtual(TYPE_Character, SIG_Character_charValue);
    } else if (boxedType.equals(TYPE_Boolean)) {
      co.invoke_virtual(TYPE_Boolean, SIG_Boolean_booleanValue);
    }
  }

  
  /**
   * Emits code to call <code>close()</code> on the local variable.
   * 
   * @param co                 the code emitter
   * @param local              the local
   */
  public static void emitClose(CodeEmitter co, Local local) {
    co.load_local(local);
    co.invoke_interface(local.getType(), SIG_close);
  }
  
  /**
   * Emits code to call <code>getConnection()</code> and stores returned connection
   *  in the local connection variable.
   * 
   * @param co                 the code emitter
   * @param generator          the query object generator
   * @param localConnection    the local to store the connection
   * 
   * @see sf.qof.BaseQuery#getConnection()
   */
  public static void emitGetConnection(CodeEmitter co, QueryObjectGenerator generator, Local localConnection) {
    co.load_this();
    co.invoke_virtual(Type.getType(generator.getClassNameType()), SIG_getConnection);
    co.store_local(localConnection);
    if (generator.getPostGetConnectionMethod() != null) {
      // emit a call to postGetConnection(connection)
      co.load_this();
      co.load_local(localConnection);
      co.invoke_virtual(Type.getType(generator.getPostGetConnectionMethod().getDeclaringClass()), SIG_postGetConnection);
    }
  }
  
  /**
   * Emits code to call <code>ungetConnection(Connection)</code> on the local connection variable.
   * 
   * @param co                 the code emitter
   * @param generator          the query object generator
   * @param localConnection    the local that holds the connection
   * 
   * @see sf.qof.BaseQuery#ungetConnection(java.sql.Connection)
   */
  public static void emitUngetConnection(CodeEmitter co, QueryObjectGenerator generator, Local localConnection) {
    co.load_this();
    co.load_local(localConnection);
    co.invoke_virtual(Type.getType(generator.getClassNameType()), SIG_ungetConnection);
  }

  public static void emitCatchException(CodeEmitter co, Block tryBlock, Type exception) {
    Label label = co.make_label();
    co.mark(label);
    if (exception == null) {
      co.visitTryCatchBlock(tryBlock.getStart(), tryBlock.getEnd(), label, null);
    } else {
      co.visitTryCatchBlock(tryBlock.getStart(), tryBlock.getEnd(), label, exception.getInternalName());
    }
  }
  
  public static void createAndStoreNewResultObject(CodeEmitter co, Mapper mapper, Local localResultSet, Local localStoreResult) {
    // constructor mappings
    Local[] constructorParameters = new Local[mapper.getNumberOfConstructorParameters()];
    ResultMappingGenerator rmp = new ResultMappingGenerator(co, localResultSet, null, null, true, constructorParameters);
    mapper.acceptResultMappers(rmp);
    
    if (mapper.getStaticFactoryMethod() == null) {
      // create a new instance of the result object
      co.new_instance(localStoreResult.getType());
      co.dup();
      
      // push constructor parameters on stack (if any)
      for (int i = 0; i < constructorParameters.length; i++) {
        co.load_local(constructorParameters[i]);
      }
      if (mapper.getConstructor() == null && mapper.getStaticFactoryMethod() == null) {
        // invoke default constructor
        co.invoke_constructor(localStoreResult.getType());
      } else {
        // invoke constructor with parameters
        Signature constructorSignature = createConstructorSignature(mapper.getConstructor());
        co.invoke_constructor(localStoreResult.getType(), constructorSignature);
      }
      
    } else {
      // use a static factory method to create result object
      
      // push parameters on stack (if any)
      for (int i = 0; i < constructorParameters.length; i++) {
        co.load_local(constructorParameters[i]);
      }
      // call static factory method to create result object
      Signature staticFactoryMethodSignature = createMethodSignature(mapper.getStaticFactoryMethod()); 
      co.invoke_static(Type.getType(mapper.getStaticFactoryMethod().getDeclaringClass()), staticFactoryMethodSignature);

    }
    co.store_local(localStoreResult);
  }

  private static Signature createConstructorSignature(Constructor<?> constructor) {
    Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
    Type[] parameterTypes = new Type[constructorParameterTypes.length];
    for (int i = 0; i < constructorParameterTypes.length; i++) {
      parameterTypes[i] = Type.getType(constructorParameterTypes[i]);
    }
    return new Signature("<init>", Type.getType(void.class), parameterTypes);
  }
  
  private static Signature createMethodSignature(Method method) {
    Class<?>[] constructorParameterTypes = method.getParameterTypes();
    Type[] parameterTypes = new Type[constructorParameterTypes.length];
    for (int i = 0; i < constructorParameterTypes.length; i++) {
      parameterTypes[i] = Type.getType(constructorParameterTypes[i]);
    }
    return new Signature(method.getName(), Type.getType(method.getReturnType()), parameterTypes);
  }

}
