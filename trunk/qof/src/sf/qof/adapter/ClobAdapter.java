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

import static sf.qof.codegen.Constants.SIG_length;
import static sf.qof.codegen.Constants.SIG_registerOutParameter;
import static sf.qof.codegen.Constants.SIG_setNull;
import static sf.qof.codegen.Constants.TYPE_CallableStatement;
import static sf.qof.codegen.Constants.TYPE_PreparedStatement;
import static sf.qof.codegen.Constants.TYPE_ResultSet;
import static sf.qof.codegen.Constants.TYPE_String;
import static sf.qof.codegen.Constants.TYPE_int;

import java.io.StringReader;
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
 * ClobAdapter is a generator mapping adapter for SQL Clob data types.
 * 
 * <p>It maps <code>Clob</code> columns to <code>String</code> and vice versa.</p>
 * 
 * @see java.sql.Clob
 */
public class ClobAdapter implements GeneratorMappingAdapter {

  private static final Type TYPE_StringReader = Type.getType(StringReader.class);
  private static final Type TYPE_Blob = Type.getType(java.sql.Blob.class);
  private static final Type TYPE_ClobReader = Type.getType(ClobReader.class);
  private static final Signature SIG_setCharacterStream = new Signature("setCharacterStream", "(ILjava/io/Reader;I)V");
  private static final Signature SIG_ConstructorStringReader = new Signature("<init>", "(Ljava/lang/String;)V");
  private static final Signature SIG_getClob = new Signature("getClob", "(I)Ljava/sql/Clob;");
  private static final Signature SIG_getClobNamed = new Signature("getClob", "(Ljava/lang/String;)Ljava/sql/Clob;");
  private static final Signature SIG_readClob = new Signature("readClob", "(Ljava/sql/Clob;)Ljava/lang/String;");

  public void generateFromResult(ResultMapping resultMapping, CodeEmitter co, Local result, int[] indexes) {
    co.load_local(result);
    co.push(indexes[0]);
    co.invoke_interface(result.getType(), SIG_getClob);
    emitProcessResult(resultMapping, co);
  }

  public void generateFromResultSet(ResultMapping resultMapping, CodeEmitter co, Local resultSet, String[] columns) {
    co.load_local(resultSet);
    co.push(columns[0]);
    co.invoke_interface(TYPE_ResultSet, SIG_getClobNamed);
    emitProcessResult(resultMapping, co);
  }

  private void emitProcessResult(ResultMapping resultMapping, CodeEmitter co) {
    Local localBlob = co.make_local(TYPE_Blob);
    co.store_local(localBlob);
    Label labelNull = co.make_label();
    co.load_local(localBlob);
    co.ifnull(labelNull);
    // not null
    co.load_local(localBlob);
    co.invoke_static(TYPE_ClobReader, SIG_readClob);
    
    Label labelEnd = co.make_label();
    co.goTo(labelEnd);
    
    // null
    co.mark(labelNull);
    co.aconst_null();
    co.mark(labelEnd);
  }

  public void generateToPreparedStatement(ParameterMapping parameterMapping, CodeEmitter co, Local preparedStatement, int[] indexes, Local indexOffset) {
    Local localString = co.make_local(TYPE_String);
    Label labelNull = co.make_label();
    Label labelEnd = co.make_label();
    co.store_local(localString);
    co.load_local(localString);
    co.ifnull(labelNull);
    // not null
    co.load_local(preparedStatement);
    co.push(indexes[0]);
    if (indexOffset != null) {
      co.load_local(indexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
    }
    co.new_instance(TYPE_StringReader);
    co.dup();
    co.load_local(localString);
    co.invoke_constructor(TYPE_StringReader, SIG_ConstructorStringReader);
    co.load_local(localString);
    co.invoke_virtual(TYPE_String, SIG_length);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setCharacterStream);
    co.goTo(labelEnd);
    
    co.mark(labelNull);
    // null
    co.load_local(preparedStatement);
    co.push(indexes[0]);
    if (indexOffset != null) {
      co.load_local(indexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
    }
    co.push(java.sql.Types.CLOB);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setNull);
    
    co.mark(labelEnd);
  }

  public void generateRegisterOutputParameters(ResultMapping resultMapping, CodeEmitter co, Local callableStatement,
      int[] indexes) {
    co.load_local(callableStatement);
    co.push(indexes[0]);
    co.push(java.sql.Types.CLOB);
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
    typeSet.add(String.class);
  }
  
  private final static ClobAdapter generator = new ClobAdapter();
  
  /**
   * Register the mapping adapter with the default name "clob".
   */
  public static void register() {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    QueryObjectFactoryDelegator.registerMapper_(classLoader, "clob", generator);
  }
  
  /**
   * Register the mapping adapter with the specified name.
   * 
   * @param typeName type name
   */
  public static void register(String typeName) {
    ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
    QueryObjectFactoryDelegator.registerMapper_(classLoader, typeName, generator);
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
