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

import static sf.qof.codegen.Constants.SIG_Boolean_valueOf;
import static sf.qof.codegen.Constants.SIG_Byte_valueOf;
import static sf.qof.codegen.Constants.SIG_Character_valueOf;
import static sf.qof.codegen.Constants.SIG_Double_valueOf;
import static sf.qof.codegen.Constants.SIG_Float_valueOf;
import static sf.qof.codegen.Constants.SIG_Integer_valueOf;
import static sf.qof.codegen.Constants.SIG_Long_valueOf;
import static sf.qof.codegen.Constants.SIG_Short_valueOf;
import static sf.qof.codegen.Constants.SIG_close;
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
    if (primitiveType == TYPE_byte) {
      co.invoke_static(TYPE_Byte, SIG_Byte_valueOf);
    } else if (primitiveType == TYPE_short) {
      co.invoke_static(TYPE_Short, SIG_Short_valueOf);
    } else if (primitiveType == TYPE_int) {
      co.invoke_static(TYPE_Integer, SIG_Integer_valueOf);
    } else if (primitiveType == TYPE_long) {
      co.invoke_static(TYPE_Long, SIG_Long_valueOf);
    } else if (primitiveType == TYPE_float) {
      co.invoke_static(TYPE_Float, SIG_Float_valueOf);
    } else if (primitiveType == TYPE_double) {
      co.invoke_static(TYPE_Double, SIG_Double_valueOf);
    } else if (primitiveType == TYPE_char) {
      co.invoke_static(TYPE_Character, SIG_Character_valueOf);
    } else if (primitiveType == TYPE_boolean) {
      co.invoke_static(TYPE_Boolean, SIG_Boolean_valueOf);
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
   * Emits code to call <code>ungetConnection(Connection)</code> on the local connection variable.
   * 
   * @param co                 the code emitter
   * @param classType          the class type
   * @param local              the local
   * 
   * @see sf.qof.BaseQuery#ungetConnection(java.sql.Connection)
   */
  public static void emitUngetConnection(CodeEmitter co, Type classType, Local local) {
    co.load_this();
    co.load_local(local);
    co.invoke_virtual(classType, SIG_ungetConnection);
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
    
    co.new_instance(localStoreResult.getType());
    co.dup();
    
    for (int i = 0; i < constructorParameters.length; i++) {
      co.load_local(constructorParameters[i]);
    }
    if (mapper.getConstructor() == null) {
      co.invoke_constructor(localStoreResult.getType());
    } else {
      Signature constructorSignature = createConstructorSignature(mapper.getConstructor());
      co.invoke_constructor(localStoreResult.getType(), constructorSignature);
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

}
