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

import static sf.qof.codegen.Constants.SIG_getBoolean;
import static sf.qof.codegen.Constants.SIG_getBooleanNamed;
import static sf.qof.codegen.Constants.SIG_getByte;
import static sf.qof.codegen.Constants.SIG_getByteNamed;
import static sf.qof.codegen.Constants.SIG_getDate;
import static sf.qof.codegen.Constants.SIG_getDateNamed;
import static sf.qof.codegen.Constants.SIG_getDouble;
import static sf.qof.codegen.Constants.SIG_getDoubleNamed;
import static sf.qof.codegen.Constants.SIG_getFloat;
import static sf.qof.codegen.Constants.SIG_getFloatNamed;
import static sf.qof.codegen.Constants.SIG_getInt;
import static sf.qof.codegen.Constants.SIG_getIntNamed;
import static sf.qof.codegen.Constants.SIG_getLong;
import static sf.qof.codegen.Constants.SIG_getLongNamed;
import static sf.qof.codegen.Constants.SIG_getShort;
import static sf.qof.codegen.Constants.SIG_getShortNamed;
import static sf.qof.codegen.Constants.SIG_getString;
import static sf.qof.codegen.Constants.SIG_getStringNamed;
import static sf.qof.codegen.Constants.SIG_getTime;
import static sf.qof.codegen.Constants.SIG_getTimeLong;
import static sf.qof.codegen.Constants.SIG_getTimeNamed;
import static sf.qof.codegen.Constants.SIG_getTimestamp;
import static sf.qof.codegen.Constants.SIG_getTimestampNamed;
import static sf.qof.codegen.Constants.SIG_wasNull;
import static sf.qof.codegen.Constants.TYPE_Character;
import static sf.qof.codegen.Constants.TYPE_Date;
import static sf.qof.codegen.Constants.TYPE_String;
import static sf.qof.codegen.Constants.TYPE_boolean;
import static sf.qof.codegen.Constants.TYPE_byte;
import static sf.qof.codegen.Constants.TYPE_double;
import static sf.qof.codegen.Constants.TYPE_float;
import static sf.qof.codegen.Constants.TYPE_int;
import static sf.qof.codegen.Constants.TYPE_long;
import static sf.qof.codegen.Constants.TYPE_short;
import static sf.qof.codegen.Constants.TYPE_sqlDate;
import static sf.qof.codegen.Constants.TYPE_sqlTime;
import static sf.qof.codegen.Constants.TYPE_sqlTimestamp;

import java.lang.reflect.Method;

import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import sf.qof.adapter.DynamicMappingAdapter;
import sf.qof.adapter.GeneratorMappingAdapter;
import sf.qof.mapping.AbstractCharacterMapping;
import sf.qof.mapping.AbstractDateTimeMapping;
import sf.qof.mapping.AbstractNumberMapping;
import sf.qof.mapping.AdapterMapping;
import sf.qof.mapping.CharacterMappingVisitor;
import sf.qof.mapping.DateTimeMappingVisitor;
import sf.qof.mapping.Mapper;
import sf.qof.mapping.MappingVisitor;
import sf.qof.mapping.NumberMappingVisitor;
import sf.qof.mapping.ResultMapping;
import sf.qof.mapping.AbstractCharacterMapping.CharacterMapping;
import sf.qof.mapping.AbstractCharacterMapping.StringMapping;
import sf.qof.mapping.AbstractDateTimeMapping.DateMapping;
import sf.qof.mapping.AbstractDateTimeMapping.TimeMapping;
import sf.qof.mapping.AbstractDateTimeMapping.TimestampMapping;
import sf.qof.mapping.AbstractNumberMapping.BooleanMapping;
import sf.qof.mapping.AbstractNumberMapping.ByteMapping;
import sf.qof.mapping.AbstractNumberMapping.DoubleMapping;
import sf.qof.mapping.AbstractNumberMapping.FloatMapping;
import sf.qof.mapping.AbstractNumberMapping.IntegerMapping;
import sf.qof.mapping.AbstractNumberMapping.LongMapping;
import sf.qof.mapping.AbstractNumberMapping.ShortMapping;
import sf.qof.util.ReflectionUtils;

/**
 * Internal - ResultMappingGenerator is the generator class for result mappings. 
 */
public class ResultMappingGenerator implements MappingVisitor, NumberMappingVisitor, CharacterMappingVisitor,
    DateTimeMappingVisitor {

  private CodeEmitter co;
  private Local resultSetOrCallableStatement;
  private Local result;
  private Local mapKey;
  private boolean constructorMappings;
  private Local[] constructorParameters; //NOPMD

  public ResultMappingGenerator(CodeEmitter co, Local resultSetOrCallableStatement, Local result, Local mapKey, 
      boolean constructorMappings, Local[] constructorParameters) {
    this.co = co;
    this.resultSetOrCallableStatement = resultSetOrCallableStatement;
    this.result = result;
    this.mapKey = mapKey;
    this.constructorMappings = constructorMappings;
    this.constructorParameters = constructorParameters;
  }

  // implementation of MappingVisitor

  public final void visit(Mapper mapper, AbstractNumberMapping mapping) {
    mapping.accept(mapper, (NumberMappingVisitor) this);
  }

  public final void visit(Mapper mapper, AbstractCharacterMapping mapping) {
    mapping.accept(mapper, (CharacterMappingVisitor) this);
  }

  public final void visit(Mapper mapper, AbstractDateTimeMapping mapping) {
    mapping.accept(mapper, (DateTimeMappingVisitor) this);
  }

  // implementation of NumberMappingVisitor

  public void visit(Mapper mapper, ByteMapping mapping) {
    generateResultMapping(mapping, TYPE_byte, SIG_getByte, SIG_getByteNamed);
  }

  public void visit(Mapper mapper, ShortMapping mapping) {
    generateResultMapping(mapping, TYPE_short, SIG_getShort, SIG_getShortNamed);
  }

  public void visit(Mapper mapper, IntegerMapping mapping) {
    generateResultMapping(mapping, TYPE_int, SIG_getInt, SIG_getIntNamed);
  }

  public void visit(Mapper mapper, LongMapping mapping) {
    generateResultMapping(mapping, TYPE_long, SIG_getLong, SIG_getLongNamed);
  }

  public void visit(Mapper mapper, FloatMapping mapping) {
    generateResultMapping(mapping, TYPE_float, SIG_getFloat, SIG_getFloatNamed);
  }

  public void visit(Mapper mapper, DoubleMapping mapping) {
    generateResultMapping(mapping, TYPE_double, SIG_getDouble, SIG_getDoubleNamed);
  }

  public void visit(Mapper mapper, BooleanMapping mapping) {
    generateResultMapping(mapping, TYPE_boolean, SIG_getBoolean, SIG_getBooleanNamed);
  }

  private void generateResultMapping(ResultMapping mapping, Type primitiveType, Signature signatureGet,
      Signature signatureGetNamed) {
    if ((constructorMappings && mapping.getConstructorParameter() == null)
        ||(!constructorMappings && mapping.getConstructorParameter() != null)) {
      return;
    }

    int sqlIndex = getSqlIndex(mapping);
    String sqlColumn = getSqlColumn(mapping);
    Method setter = mapping.getSetter();
    Class<?> type = mapping.getType();
    Local localResult = null;
    
    if (constructorMappings) {
      if (mapping.getConstructor() != null) {
        type = mapping.getConstructor().getParameterTypes()[mapping.getConstructorParameter() - 1];
      } else {
        type = mapping.getStaticFactoryMethod().getParameterTypes()[mapping.getConstructorParameter() - 1];
      }
      localResult = co.make_local(Type.getType(type));
      constructorParameters[mapping.getConstructorParameter() - 1] = localResult;
    } else {
      if (mapping.isMapKey()) {
        localResult = mapKey;
      } else {
        localResult = result;
      }
    }
    
    if (!type.isPrimitive()) {
      // --------
      Local localValue = co.make_local(primitiveType);
      Label labelWasNull = co.make_label();
      Label labelEnd = co.make_label();
      co.load_local(resultSetOrCallableStatement);
      if (sqlColumn == null) {
        co.push(sqlIndex);
        co.invoke_interface(resultSetOrCallableStatement.getType(), signatureGet);
      } else {
        co.push(sqlColumn);
        co.invoke_interface(resultSetOrCallableStatement.getType(), signatureGetNamed);
      }
      co.store_local(localValue);
      co.load_local(resultSetOrCallableStatement);
      co.invoke_interface(resultSetOrCallableStatement.getType(), SIG_wasNull);
      co.if_jump(CodeEmitter.NE, labelWasNull);

      if (setter != null) {
        co.load_local(localResult);
        co.load_local(localValue);
        EmitUtils.boxUsingValueOf(co, primitiveType);
        co.invoke_virtual(Type.getType(setter.getDeclaringClass()), ReflectionUtils.getMethodSignature(setter));
        co.goTo(labelEnd);

        co.mark(labelWasNull);
        co.load_local(localResult);
        co.aconst_null();
        co.invoke_virtual(Type.getType(setter.getDeclaringClass()), ReflectionUtils.getMethodSignature(setter));
      } else {
        co.load_local(localValue);
        EmitUtils.boxUsingValueOf(co, primitiveType);
        co.store_local(localResult);
        co.goTo(labelEnd);

        co.mark(labelWasNull);
        co.aconst_null();
        co.store_local(localResult);
      }
      co.mark(labelEnd);
    } else {
      // it's an int, etc
      // ... = getInt(index);
      if (setter != null) {
        co.load_local(localResult);
      }
      co.load_local(resultSetOrCallableStatement);
      if (sqlColumn == null) {
        co.push(sqlIndex);
        co.invoke_interface(resultSetOrCallableStatement.getType(), signatureGet);
      } else {
        co.push(sqlColumn);
        co.invoke_interface(resultSetOrCallableStatement.getType(), signatureGetNamed);
      }
      if (setter != null) {
        co.invoke_virtual(Type.getType(setter.getDeclaringClass()), ReflectionUtils.getMethodSignature(setter));
      } else {
        co.store_local(localResult);
      }
    }
  }

  // implementation of CharacterMappingVisitor

  public final void visit(Mapper mapper, StringMapping mapping) {
    if ((constructorMappings && mapping.getConstructorParameter() == null)
        ||(!constructorMappings && mapping.getConstructorParameter() != null)) {
      return;
    }

    generateGetString(mapping);
    generateStoreString(mapping);
  }

  public final void visit(Mapper mapper, CharacterMapping mapping) {
    if ((constructorMappings && mapping.getConstructorParameter() == null)
        ||(!constructorMappings && mapping.getConstructorParameter() != null)) {
      return;
    }

    generateGetString(mapping);
    
    Class<?> type;
    if (constructorMappings) {
      if (mapping.getConstructor() != null) {
        type = mapping.getConstructor().getParameterTypes()[mapping.getConstructorParameter() - 1];
      } else {
        type = mapping.getStaticFactoryMethod().getParameterTypes()[mapping.getConstructorParameter() - 1];
      }
    } else {
      type = mapping.getType();
    }
    // get first char:
    // Character c = (s == null) || (s.length() == 0) ? null :
    // Character.valueOf(s.charAt(0));
    // char c = (s == null) || (s.length() == 0) ? 0 : s.charAt(0);
    Local localValue = co.make_local(TYPE_String);
    co.store_local(localValue);

    Label labelNull = co.make_label();
    Label labelEnd = co.make_label();

    co.load_local(localValue);
    co.ifnull(labelNull);
    co.load_local(localValue);
    co.invoke_virtual(TYPE_String, new Signature("length", "()I"));
    co.if_jump(CodeEmitter.EQ, labelNull);
    co.load_local(localValue);
    co.push(0);
    co.invoke_virtual(TYPE_String, new Signature("charAt", "(I)C"));
    if (!type.isPrimitive()) {
      co.invoke_static(TYPE_Character, new Signature("valueOf", "(C)Ljava/lang/Character;"));
    }
    co.goTo(labelEnd);

    co.mark(labelNull);
    if (type.isPrimitive()) {
      co.push(0);
    } else {
      co.aconst_null();
    }
    co.mark(labelEnd);
    
    generateStoreString(mapping);
  }

  private void generateGetString(ResultMapping mapping) {
    int sqlIndex = getSqlIndex(mapping);
    String sqlColumn = getSqlColumn(mapping);
    Method setter = mapping.getSetter();

    // ... = getString(index);
    if (setter != null) {
      if (mapping.isMapKey()) {
        co.load_local(mapKey);
      } else {
        co.load_local(result);
      }
    }
    co.load_local(resultSetOrCallableStatement);
    if (sqlColumn == null) {
      co.push(sqlIndex);
      co.invoke_interface(resultSetOrCallableStatement.getType(), SIG_getString);
    } else {
      co.push(sqlColumn);
      co.invoke_interface(resultSetOrCallableStatement.getType(), SIG_getStringNamed);
    }
  }

  private void generateStoreString(ResultMapping mapping) {
    Method setter = mapping.getSetter();
    Class<?> type = mapping.getType();
    Local localResult = null;
    
    if (constructorMappings) {
      if (mapping.getConstructor() != null) {
        type = mapping.getConstructor().getParameterTypes()[mapping.getConstructorParameter() - 1];
      } else {
        type = mapping.getStaticFactoryMethod().getParameterTypes()[mapping.getConstructorParameter() - 1];
      }
      localResult = co.make_local(Type.getType(type));
      constructorParameters[mapping.getConstructorParameter() - 1] = localResult;
    } else {
      if (mapping.isMapKey()) {
        localResult = mapKey;
      } else {
        localResult = result;
      }
    }
    if (setter != null) {
      co.invoke_virtual(Type.getType(setter.getDeclaringClass()), ReflectionUtils.getMethodSignature(setter));
    } else {
      co.store_local(localResult);
    }
  }

  // implementation of DateTimeVisitor

  public void visit(Mapper mapper, DateMapping mapping) {
    generateParameterMapping(mapping, TYPE_sqlDate, SIG_getDate, SIG_getDateNamed);
  }

  public void visit(Mapper mapper, TimeMapping mapping) {
    generateParameterMapping(mapping, TYPE_sqlTime, SIG_getTime, SIG_getTimeNamed);
  }

  public void visit(Mapper mapper, TimestampMapping mapping) {
    generateParameterMapping(mapping, TYPE_sqlTimestamp, SIG_getTimestamp, SIG_getTimestampNamed);
  }

  private void generateParameterMapping(AbstractDateTimeMapping mapping, Type sqlType, Signature sqlTypeGet, Signature sqlTypeGetNamed) {
    if ((constructorMappings && mapping.getConstructorParameter() == null)
        ||(!constructorMappings && mapping.getConstructorParameter() != null)) {
      return;
    }

    int sqlIndex = getSqlIndex(mapping);
    String sqlColumn = getSqlColumn(mapping);
    Method setter = mapping.getSetter();
    Class<?> type = mapping.getType();
    Local localResult = null;
    
    if (constructorMappings) {
      if (mapping.getConstructor() != null) {
        type = mapping.getConstructor().getParameterTypes()[mapping.getConstructorParameter() - 1];
      } else {
        type = mapping.getStaticFactoryMethod().getParameterTypes()[mapping.getConstructorParameter() - 1];
      }
      localResult = co.make_local(Type.getType(type));
      constructorParameters[mapping.getConstructorParameter() - 1] = localResult;
    } else {
      if (mapping.isMapKey()) {
        localResult = mapKey;
      } else {
        localResult = result;
      }
    }

    // ... = getDate(index);
    if (setter != null) {
      if (mapping.isMapKey()) {
        co.load_local(mapKey);
      } else {
        co.load_local(localResult);
      }
    }
    co.load_local(resultSetOrCallableStatement);
    if (sqlColumn == null) {
      co.push(sqlIndex);
      co.invoke_interface(resultSetOrCallableStatement.getType(), sqlTypeGet);
    } else {
      co.push(sqlColumn);
      co.invoke_interface(resultSetOrCallableStatement.getType(), sqlTypeGetNamed);
    }
    // convert java.sql.Date to java.util.Date
    Local date = co.make_local(sqlType);
    co.store_local(date);
    Label labelNull = co.make_label();
    Label labelEnd = co.make_label();
    co.load_local(date);
    co.ifnull(labelNull);

    co.new_instance(TYPE_Date);
    co.dup();
    co.load_local(date);
    co.invoke_virtual(sqlType, SIG_getTimeLong);
    co.invoke_constructor(TYPE_Date, new Signature("<init>", "(J)V"));
    co.goTo(labelEnd);

    co.mark(labelNull);
    co.aconst_null();

    co.mark(labelEnd);
    if (setter != null) {
      co.invoke_virtual(Type.getType(setter.getDeclaringClass()), ReflectionUtils.getMethodSignature(setter));
    } else {
      if (mapping.isMapKey()) {
        co.store_local(mapKey);
      } else {
        co.store_local(localResult);
      }
    }
  }

  public final void visit(Mapper mapper, AdapterMapping mapping) {
    if ((constructorMappings && mapping.getConstructorParameter() == null)
        ||(!constructorMappings && mapping.getConstructorParameter() != null)) {
      return;
    }

    int[] sqlIndexes = mapping.getSqlIndexes();
    String[] sqlColumns = mapping.getSqlColumns();
    Method setter = mapping.getSetter();
    Class<?> type = mapping.getType();
    Local localResult = null;
    
    if (constructorMappings) {
      if (mapping.getConstructor() != null) {
        type = mapping.getConstructor().getParameterTypes()[mapping.getConstructorParameter() - 1];
      } else {
        type = mapping.getStaticFactoryMethod().getParameterTypes()[mapping.getConstructorParameter() - 1];
      }        
      localResult = co.make_local(Type.getType(type));
      constructorParameters[mapping.getConstructorParameter() - 1] = localResult;
    } else {
      if (mapping.isMapKey()) {
        localResult = mapKey;
      } else {
        localResult = result;
      }
    }

    if (setter != null) {
      if (mapping.isMapKey()) {
        co.load_local(mapKey);
      } else {
        co.load_local(localResult);
      }
    }
    if (mapping.getAdapter() instanceof GeneratorMappingAdapter) {
      if (sqlColumns != null) {
        ((GeneratorMappingAdapter) mapping.getAdapter()).generateFromResultSet(mapping, co, resultSetOrCallableStatement, sqlColumns);
      } else {
        ((GeneratorMappingAdapter) mapping.getAdapter()).generateFromResult(mapping, co, resultSetOrCallableStatement, sqlIndexes);
      }
      // type check of the result
      if (mapping.isMapKey()) {
        if (!mapping.getMapKeyType().isPrimitive()) {
          co.checkcast(Type.getType(mapping.getMapKeyType()));
        }
      } else {
        if (!type.isPrimitive()) {
          co.checkcast(Type.getType(type));
        }
      }
    } else if (mapping.getAdapter() instanceof DynamicMappingAdapter) {
      co.getfield(QueryObjectGenerator.getAdapterFieldName(mapping.getAdapter().getClass()));
      co.load_local(resultSetOrCallableStatement);
      if (sqlColumns != null) {
        // push columns
        co.push(sqlColumns.length);
        co.newarray(TYPE_String);
        for (int i = 0; i < sqlColumns.length; i++) {
          co.dup();
          co.push(i);
          co.push(sqlColumns[i]);
          co.array_store(TYPE_String);
        }
        co.invoke_interface(Type.getType(DynamicMappingAdapter.class), new Signature("get",
            "(Ljava/sql/ResultSet;[Ljava/lang/String;)Ljava/lang/Object;"));
      } else {
        // push indexes
        co.push(sqlIndexes.length);
        co.newarray(TYPE_int);
        for (int i = 0; i < sqlIndexes.length; i++) {
          co.dup();
          co.push(i);
          co.push(sqlIndexes[i]);
          co.array_store(TYPE_int);
        }
        // either get from a ResultSet or CallableStatement
        co.invoke_interface(Type.getType(DynamicMappingAdapter.class), new Signature("get",
            "("+ resultSetOrCallableStatement.getType().getDescriptor() + "[I)Ljava/lang/Object;"));
      }
      if (mapping.isMapKey()) {
        co.checkcast(Type.getType(mapping.getMapKeyType()));
      } else {
        co.checkcast(Type.getType(type));
      }
    } else {
      throw new RuntimeException("Unsupported adapter type " + mapping.getAdapter());
    }
    if (setter != null) {
      co.invoke_virtual(Type.getType(setter.getDeclaringClass()), ReflectionUtils.getMethodSignature(setter));
    } else {
      if (mapping.isMapKey()) {
        co.store_local(mapKey);
      } else {
        co.store_local(localResult);
      }
    }
  }

  private String getSqlColumn(ResultMapping mapping) {
    return mapping.getSqlColumns() != null && mapping.getSqlColumns().length == 1 ? mapping.getSqlColumns()[0] : null; //NOPMD
  }

  private int getSqlIndex(ResultMapping mapping) {
    return mapping.getSqlIndexes() != null && mapping.getSqlIndexes().length == 1 ? mapping.getSqlIndexes()[0] : -1;
  }
}
