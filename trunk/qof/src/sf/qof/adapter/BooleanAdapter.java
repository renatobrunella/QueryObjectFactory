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
package sf.qof.adapter;

import static sf.qof.codegen.Constants.SIG_Boolean_valueOf;
import static sf.qof.codegen.Constants.SIG_booleanValue;
import static sf.qof.codegen.Constants.SIG_getString;
import static sf.qof.codegen.Constants.SIG_getStringNamed;
import static sf.qof.codegen.Constants.SIG_registerOutParameter;
import static sf.qof.codegen.Constants.SIG_setNull;
import static sf.qof.codegen.Constants.SIG_setString;
import static sf.qof.codegen.Constants.TYPE_Boolean;
import static sf.qof.codegen.Constants.TYPE_CallableStatement;
import static sf.qof.codegen.Constants.TYPE_PreparedStatement;
import static sf.qof.codegen.Constants.TYPE_ResultSet;
import static sf.qof.codegen.Constants.TYPE_SQLException;
import static sf.qof.codegen.Constants.TYPE_String;
import static sf.qof.codegen.Constants.TYPE_boolean;
import static sf.qof.codegen.Constants.TYPE_int;

import java.util.HashSet;
import java.util.Set;

import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import sf.qof.QueryObjectFactory;
import sf.qof.mapping.ParameterMapping;
import sf.qof.mapping.ResultMapping;
import sf.qof.util.CallStackIntrospector;

/**
 * BooleanAdapter is a generator mapping adapter for boolean data types.
 * 
 * <p>It maps <code>VARCHAR</code> columns to <code>boolean</code> and <code>Boolean</code> and vice versa.</p>
 *
 * <p>Examples:</p>
 * <pre><blockquote>
 *   (1) BooleanAdapter.register("yesno", "Y", "N", false, true);
 *   
 *   (2) BooleanAdapter.register("true-false", "true", "false", true, false);
 *   
 *   (3) BooleanAdapter.register("bigX", "X", null, true, true);
 * </blockquote></pre>
 * 
 * <p>(1) Maps boolean types to "Y" and "N", is not case-sensitive ("y" is true as well)
 * and allows null values (a SQL null value gets mapped to <code>false</code> if the 
 * type is <code>boolean</code>).</p>
 * 
 * <p>(2) Maps boolean types to the strings "true" and "false", is case-sensitive and
 * does not allow null values.</p>
 * 
 * <p>(3) Maps boolean types to "X" and null, is case-sensitive and allows nulls.</p>
 * 
 * @see #registerYesNo()
 * @see #register(String, String, String, boolean, boolean)
 */
public class BooleanAdapter implements GeneratorMappingAdapter {

  private final static Signature SIG_toUpperCase = new Signature("toUpperCase", "()Ljava/lang/String;");
  private final static Signature SIG_equals = new Signature("equals", "(Ljava/lang/Object;)Z");
  
  private String typeName;
  private String trueString;
  private String falseString;
  private boolean caseSensitive;
  private boolean allowNull;

  private BooleanAdapter(String typeName, String trueString, String falseString, boolean caseSensitive, boolean allowNull) {
    if (trueString == null) {
      throw new IllegalArgumentException("trueString must not be null");
    }
    this.typeName = typeName;
    this.trueString = trueString;
    this.falseString = falseString;
    this.caseSensitive = caseSensitive;
    this.allowNull = allowNull;
  }
  
  public void generateFromResult(ResultMapping resultMapping, CodeEmitter co, Local result, int[] indexes) {
    co.load_local(result);
    co.push(indexes[0]);
    co.invoke_interface(result.getType(), SIG_getString);
    emitProcessResult(resultMapping, co);
  }

  public void generateFromResultSet(ResultMapping resultMapping, CodeEmitter co, Local resultSet, String[] columns) {
    co.load_local(resultSet);
    co.push(columns[0]);
    co.invoke_interface(TYPE_ResultSet, SIG_getStringNamed);
    emitProcessResult(resultMapping, co);
  }

  private void emitProcessResult(ResultMapping resultMapping, CodeEmitter co) {
    Label labelNull = co.make_label();
    Label labelNotFalse = co.make_label();
    Label labelEnd = co.make_label();
    Local localString = co.make_local(TYPE_String);
    Local localTrueFalse = co.make_local(TYPE_boolean);
    Local localResult = null;
    if (resultMapping.getType() == Boolean.class) {
      localResult = co.make_local(TYPE_Boolean);
    }
    co.store_local(localString);
    co.load_local(localString);
    co.ifnull(labelNull);
    // not null
    co.load_local(localString);
    if (!caseSensitive) {
      co.invoke_virtual(TYPE_String, SIG_toUpperCase);
      co.push(trueString.toUpperCase());
    } else {
      co.push(trueString);
    }
    co.invoke_virtual(Type.getType(Object.class), SIG_equals);
    co.store_local(localTrueFalse);
    if (falseString != null) {
      co.load_local(localTrueFalse);
      co.if_jump(CodeEmitter.NE, labelNotFalse);
      co.load_local(localString);
      if (!caseSensitive) {
        co.invoke_virtual(TYPE_String, SIG_toUpperCase);
        co.push(falseString.toUpperCase());
      } else {
        co.push(falseString);
      }
      co.invoke_virtual(Type.getType(Object.class), SIG_equals);
      // negate result
      Label labelIsFalse = co.make_label();
      Label labelIsTrueFalseEnd = co.make_label();
      
      co.if_jump(CodeEmitter.EQ, labelIsFalse);
      co.push(false);
      co.goTo(labelIsTrueFalseEnd);
      co.mark(labelIsFalse);
      co.push(true);
      co.mark(labelIsTrueFalseEnd);
      
      co.store_local(localTrueFalse);
      co.load_local(localTrueFalse);
      co.if_jump(CodeEmitter.EQ, labelNotFalse);
      co.throw_exception(TYPE_SQLException, "invalid value for mapper \"" + typeName + "\"");
      co.mark(labelNotFalse);
    }
    if (resultMapping.getType() == Boolean.class) {
      co.load_local(localTrueFalse);
      co.invoke_static(TYPE_Boolean, SIG_Boolean_valueOf);
      co.store_local(localResult);
    }
    
    co.goTo(labelEnd);

    co.mark(labelNull);
    // null
    if (falseString != null) {
      if (allowNull) {
        if (resultMapping.getType() == Boolean.class) {
          co.aconst_null();
          co.store_local(localResult);
        } else {
          co.push(false);
          co.store_local(localTrueFalse);
        }
      } else {
        co.throw_exception(TYPE_SQLException, "null value not allowed for mapper \"" + typeName + "\"");
      }
    } else {
      if (resultMapping.getType() == Boolean.class) {
        co.push(false);
        co.invoke_static(TYPE_Boolean, SIG_Boolean_valueOf);
        co.store_local(localResult);
      } else {
        co.push(false);
        co.store_local(localTrueFalse);
      }
    }
    co.mark(labelEnd);
    if (resultMapping.getType() == Boolean.class) {
      co.load_local(localResult);
    } else {
      co.load_local(localTrueFalse);
    }
  }

  public void generateToPreparedStatement(ParameterMapping parameterMapping, CodeEmitter co, Local preparedStatement, int[] indexes, Local indexOffset) {
    Label labelFalse = co.make_label();
    Label labelTrueFalseEnd = co.make_label();
    if (parameterMapping.getType() == Boolean.class) {
      // Boolean type
      Local localBoolean = co.make_local(TYPE_Boolean);
      co.store_local(localBoolean);
      co.load_local(localBoolean);
      Label labelNull = co.make_label();
      Label labelEnd = co.make_label();
      co.ifnull(labelNull);
      // not null
      if (falseString != null) {
        co.load_local(preparedStatement);
        co.push(indexes[0]);
        if (indexOffset != null) {
          co.load_local(indexOffset);
          co.math(CodeEmitter.ADD, TYPE_int);
        }
        co.load_local(localBoolean);
        co.invoke_virtual(TYPE_Boolean, SIG_booleanValue);
        co.if_jump(CodeEmitter.EQ, labelFalse);
        // true
        co.push(trueString);
        co.goTo(labelTrueFalseEnd);
        //false
        co.mark(labelFalse);
        co.push(falseString);
        co.mark(labelTrueFalseEnd);
        co.invoke_interface(TYPE_PreparedStatement, SIG_setString);
        co.goTo(labelEnd);
      } else {
        co.load_local(localBoolean);
        co.invoke_virtual(TYPE_Boolean, SIG_booleanValue);
        co.if_jump(CodeEmitter.EQ, labelFalse);
        // true
        co.load_local(preparedStatement);
        co.push(indexes[0]);
        if (indexOffset != null) {
          co.load_local(indexOffset);
          co.math(CodeEmitter.ADD, TYPE_int);
        }
        co.push(trueString);
        co.invoke_interface(TYPE_PreparedStatement, SIG_setString);
        co.goTo(labelTrueFalseEnd);
        //false
        co.mark(labelFalse);
        co.load_local(preparedStatement);
        co.push(indexes[0]);
        if (indexOffset != null) {
          co.load_local(indexOffset);
          co.math(CodeEmitter.ADD, TYPE_int);
        }
        co.push(java.sql.Types.VARCHAR);
        co.invoke_interface(TYPE_PreparedStatement, SIG_setNull);
        
        co.mark(labelTrueFalseEnd);
        co.goTo(labelEnd);
      }
      
      co.mark(labelNull);
      // null
      if (allowNull) {
        co.load_local(preparedStatement);
        co.push(indexes[0]);
        if (indexOffset != null) {
          co.load_local(indexOffset);
          co.math(CodeEmitter.ADD, TYPE_int);
        }
        co.push(java.sql.Types.VARCHAR);
        co.invoke_interface(TYPE_PreparedStatement, SIG_setNull);
      } else {
        co.throw_exception(TYPE_SQLException, "null value not allowed for mapper \"" + typeName + "\"");
      }
      co.mark(labelEnd);
    } else {
      // boolean type
      Local localBoolean = co.make_local(TYPE_boolean);
      co.store_local(localBoolean);
      if (falseString != null) {
        co.load_local(preparedStatement);
        co.push(indexes[0]);
        if (indexOffset != null) {
          co.load_local(indexOffset);
          co.math(CodeEmitter.ADD, TYPE_int);
        }
        co.load_local(localBoolean);
        co.if_jump(CodeEmitter.EQ, labelFalse);
        // true
        co.push(trueString);
        co.goTo(labelTrueFalseEnd);
        //false
        co.mark(labelFalse);
        co.push(falseString);
        
        co.mark(labelTrueFalseEnd);
        co.invoke_interface(TYPE_PreparedStatement, SIG_setString);
      } else {
        co.load_local(localBoolean);
        co.if_jump(CodeEmitter.EQ, labelFalse);
        // true
        co.load_local(preparedStatement);
        co.push(indexes[0]);
        if (indexOffset != null) {
          co.load_local(indexOffset);
          co.math(CodeEmitter.ADD, TYPE_int);
        }
        co.push(trueString);
        co.invoke_interface(TYPE_PreparedStatement, SIG_setString);
        co.goTo(labelTrueFalseEnd);
        //false
        co.mark(labelFalse);
        co.load_local(preparedStatement);
        co.push(indexes[0]);
        if (indexOffset != null) {
          co.load_local(indexOffset);
          co.math(CodeEmitter.ADD, TYPE_int);
        }
        co.push(java.sql.Types.VARCHAR);
        co.invoke_interface(TYPE_PreparedStatement, SIG_setNull);
        co.mark(labelTrueFalseEnd);
      }
    }
  }

  public void generateRegisterOutputParameters(ResultMapping resultMapping, CodeEmitter co, Local callableStatement,
      int[] indexes) {
    co.load_local(callableStatement);
    co.push(indexes[0]);
    co.push(java.sql.Types.VARCHAR);
    co.invoke_interface(TYPE_CallableStatement, SIG_registerOutParameter);
  }

  public int getNumberOfColumns() {
    return 1;
  }

  public Set<Class<?>> getTypes() {
    return typeSet;
  }

  private static Set<Class<?>> typeSet;
  
  static {
    typeSet = new HashSet<Class<?>>();
    typeSet.add(boolean.class);
    typeSet.add(Boolean.class);
  }
  
  /**
   * Registers a yes/no adapter with name "yesno".
   * 
   * It maps "Y" to true, "N" to false, is not case-sensitive and allows null values.
   */
  public static void registerYesNo() {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    QueryObjectFactoryDelegator.registerMapper_(classLoader, "yesno", new BooleanAdapter("yesno", "Y", "N", false, true));
  }
  
  /**
   * Registers a <code>BooleanAdapter</code> with a given name.
   * 
   * @param typeName       the type name to be used in parameter and result definitions
   * @param trueString     a string representing the true value
   * @param falseString    a string representing the false value. Can be null
   * @param caseSensitive  true if the mapping is case-sensitive
   * @param allowNull      true if null values are allowed
   */
  public static void register(String typeName, String trueString, String falseString, boolean caseSensitive, boolean allowNull) {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    QueryObjectFactoryDelegator.registerMapper_(classLoader, typeName, new BooleanAdapter(typeName, trueString, falseString, caseSensitive, allowNull));
  }
  
  /*
   * Delegator object to access protected static methods in sf.qof.QueryObjectFactory
   */
  private static class QueryObjectFactoryDelegator extends QueryObjectFactory {
    public static void registerMapper_(ClassLoader classLoader, String type, MappingAdapter adapter) {
      registerMapper(classLoader, type, adapter);
    }
  }
  
}
