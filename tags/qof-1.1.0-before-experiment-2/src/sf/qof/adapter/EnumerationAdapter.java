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
package sf.qof.adapter;

import static sf.qof.codegen.Constants.SIG_getString;
import static sf.qof.codegen.Constants.SIG_getStringNamed;
import static sf.qof.codegen.Constants.SIG_registerOutParameter;
import static sf.qof.codegen.Constants.SIG_setNull;
import static sf.qof.codegen.Constants.SIG_setString;
import static sf.qof.codegen.Constants.TYPE_CallableStatement;
import static sf.qof.codegen.Constants.TYPE_PreparedStatement;
import static sf.qof.codegen.Constants.TYPE_ResultSet;
import static sf.qof.codegen.Constants.TYPE_String;
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
 * EnumerationAdapter is a generator mapping adapter for Java enumerations.
 * 
 * <p>The default registered enum adapter uses <code>enum.name()</code> to 
 * map an enumeration to a VARCHAR and <code>enum.valueOf(String)</code> to map it back.</p>
 * <p>Clients can use the <code>register(String, String, String)</code> to register
 * an enumeration adapter that uses different methods like <code>toString()</code> to
 * map enumerations from and to strings.</p>
 * 
 * @see #register()
 * @see #register(String, String, String)
 */
public class EnumerationAdapter implements GeneratorMappingAdapter {

  private String stringMethodName;
  private String enumerationMethodName;

  protected EnumerationAdapter(String stringMethodName, String enumerationMethodName) {
    this.stringMethodName = stringMethodName;
    this.enumerationMethodName = enumerationMethodName;
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
    Local localResult = co.make_local(TYPE_String);
    co.store_local(localResult);
    Label labelNull = co.make_label();
    co.load_local(localResult);
    co.ifnull(labelNull);
    co.load_local(localResult);
    Type enumType;
    if (resultMapping.getConstructor() != null) {
      enumType = Type.getType(resultMapping.getConstructor()
          .getParameterTypes()[resultMapping.getConstructorParameter() - 1]);
    } else if (resultMapping.getMapKeyType() != null) {
      enumType = Type.getType(resultMapping.getMapKeyType());
    } else {
      enumType = Type.getType(resultMapping.getType());
    }
    co.invoke_static(enumType, new Signature(enumerationMethodName, "(Ljava/lang/String;)" + enumType.getDescriptor()));
    Label labelEnd = co.make_label();
    co.goTo(labelEnd);
    co.mark(labelNull);
    co.aconst_null();
    co.mark(labelEnd);
  }

  public void generateToPreparedStatement(ParameterMapping parameterMapping, CodeEmitter co, Local preparedStatement, int[] indexes, Local indexOffset) {
    Local localEnum = co.make_local();
    Label labelNull = co.make_label();
    Label labelEnd = co.make_label();
    co.store_local(localEnum);
    co.load_local(localEnum);
    co.ifnull(labelNull);
    // not null
    co.load_local(localEnum);
    co.invoke_virtual(Type.getType(parameterMapping.getType()), new Signature(stringMethodName, "()Ljava/lang/String;"));
    Local localString = co.make_local(TYPE_String);
    co.store_local(localString);
    co.load_local(preparedStatement);
    co.push(indexes[0]);
    if (indexOffset != null) {
      co.load_local(indexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
    }
    co.load_local(localString);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setString);
    co.goTo(labelEnd);
    
    co.mark(labelNull);
    // null
    co.load_local(preparedStatement);
    co.push(indexes[0]);
    if (indexOffset != null) {
      co.load_local(indexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
    }
    co.push(java.sql.Types.VARCHAR);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setNull);
    
    co.mark(labelEnd);
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
    typeSet.add(Enum.class);
  }
  
  /**
   * Register the mapping adapter with the default name "enum".
   */
  public static void register() {
    QueryObjectFactoryDelegator.registerMapper_(null, "enum", new EnumerationAdapter("name", "valueOf"));
  }

  /**
   * Register the mapping adapter with the specified name.
   * 
   * @param typeName               type name
   * @param stringMethodName       method name of method returning string representation 
   *                               of enumeration like <code>name</code> or <code>toString</code>
   * @param enumerationMethodName  static method name of method returning an enumeration from a string 
   *                               like <code>valueOf</code> 
   */
  public static void register(String typeName, String stringMethodName, String enumerationMethodName) {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    QueryObjectFactoryDelegator.registerMapper_(classLoader, typeName, new EnumerationAdapter(stringMethodName, enumerationMethodName));
  }
  
  /*
   * Delegator object to access protected static methods in sf.qof.QueryObjectFactory
   */
  private static class QueryObjectFactoryDelegator extends QueryObjectFactory {
    public static void registerMapper_(ClassLoader classLoader, String type, MappingAdapter adapter) {
      unregisterMapper(classLoader, type);
      registerMapper(classLoader, type, adapter);
    }
  }

}
