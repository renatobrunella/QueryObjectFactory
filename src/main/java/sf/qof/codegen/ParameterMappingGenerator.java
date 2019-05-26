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

import static sf.qof.codegen.Constants.SIG_booleanValue;
import static sf.qof.codegen.Constants.SIG_byteValue;
import static sf.qof.codegen.Constants.SIG_doubleValue;
import static sf.qof.codegen.Constants.SIG_floatValue;
import static sf.qof.codegen.Constants.SIG_getTimeLong;
import static sf.qof.codegen.Constants.SIG_intValue;
import static sf.qof.codegen.Constants.SIG_longValue;
import static sf.qof.codegen.Constants.SIG_setBoolean;
import static sf.qof.codegen.Constants.SIG_setByte;
import static sf.qof.codegen.Constants.SIG_setDate;
import static sf.qof.codegen.Constants.SIG_setDouble;
import static sf.qof.codegen.Constants.SIG_setFloat;
import static sf.qof.codegen.Constants.SIG_setInt;
import static sf.qof.codegen.Constants.SIG_setLong;
import static sf.qof.codegen.Constants.SIG_setNull;
import static sf.qof.codegen.Constants.SIG_setShort;
import static sf.qof.codegen.Constants.SIG_setString;
import static sf.qof.codegen.Constants.SIG_setTime;
import static sf.qof.codegen.Constants.SIG_setTimestamp;
import static sf.qof.codegen.Constants.SIG_shortValue;
import static sf.qof.codegen.Constants.TYPE_Boolean;
import static sf.qof.codegen.Constants.TYPE_Byte;
import static sf.qof.codegen.Constants.TYPE_Character;
import static sf.qof.codegen.Constants.TYPE_Date;
import static sf.qof.codegen.Constants.TYPE_Double;
import static sf.qof.codegen.Constants.TYPE_Float;
import static sf.qof.codegen.Constants.TYPE_Integer;
import static sf.qof.codegen.Constants.TYPE_Long;
import static sf.qof.codegen.Constants.TYPE_Short;
import static sf.qof.codegen.Constants.TYPE_String;
import static sf.qof.codegen.Constants.TYPE_boolean;
import static sf.qof.codegen.Constants.TYPE_byte;
import static sf.qof.codegen.Constants.TYPE_char;
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
import sf.qof.mapping.MethodParameterInfo;
import sf.qof.mapping.NumberMappingVisitor;
import sf.qof.mapping.ParameterMapping;
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
 * Internal - ParameterMappingGenerator is the generator class for parameter mappings. 
 */
public class ParameterMappingGenerator implements MappingVisitor, NumberMappingVisitor, CharacterMappingVisitor,
    DateTimeMappingVisitor {

  private CodeEmitter co;
  private Local preparedStatement;
  private Local[] currentCollectionObj;
  private int[] collectionIndexes;
  private Local parameterIndexOffset;

  public ParameterMappingGenerator(CodeEmitter co, Local statement, Local[] currentCollectionObjs,
      MethodParameterInfo[] parameterInfos, Local parameterIndexOffset) {
    this.co = co;
    this.preparedStatement = statement;
    this.currentCollectionObj = currentCollectionObjs;
    this.parameterIndexOffset = parameterIndexOffset;
    if (parameterInfos != null) {
      collectionIndexes = new int[parameterInfos.length];
      for (int i = 0; i < parameterInfos.length; i++) {
        collectionIndexes[i] = parameterInfos[i].getIndex();
      }
    }
  }

  private void loadValue(int argIndex, Method[] getters, boolean usesCollection, boolean checkForNulls) {
    // is the parameter already on the stack?
    if (argIndex >= 0) {
      // if (currentCollectionObj == null) {
      if (!usesCollection) {
        // load value from a method parameter
        co.load_arg(argIndex);
      } else {
        // load value from the current object of the iterator
        co.load_local(currentCollectionObj[findIndex(argIndex)]);
        // co.checkcast(Type.getType(getter.getDeclaringClass()));
      }
    }
    // invoke the getters
    if (getters != null) {
      // does it return a primitive or is there no chaining?
      if (!checkForNulls || getters.length == 1 || getters[getters.length - 1].getReturnType().isPrimitive()) {
        // cannot return null - might throw NullPointerException
        for (Method getter : getters) {
          Type owner = Type.getType(getter.getDeclaringClass());
          Signature signature = ReflectionUtils.getMethodSignature(getter);
          if (getter.getDeclaringClass().isInterface()) {
            co.invoke_interface(owner, signature);
          } else {
            co.invoke_virtual(owner, signature);
          }
        }
      } else {
        Label labelNull = co.make_label();
        Local temp = null;
        for (int i = 0; i < getters.length; i++) {
          boolean isLast = i == getters.length - 1;
            Method getter = getters[i];
          if (temp != null) {
            co.load_local(temp);
          }
          Type owner = Type.getType(getter.getDeclaringClass());
          Signature signature = ReflectionUtils.getMethodSignature(getter);
          if (getter.getDeclaringClass().isInterface()) {
            co.invoke_interface(owner, signature);
          } else {
            co.invoke_virtual(owner, signature);
          }
          temp = co.make_local();
          co.store_local(temp);
          if (!isLast) {
            co.load_local(temp);
            co.ifnull(labelNull);
          }
        }
        Label labelNotNull = co.make_label();
        co.goTo(labelNotNull);
        co.mark(labelNull);
        co.aconst_null();
        co.store_local(temp);
        co.mark(labelNotNull);
        co.load_local(temp);
      }
    }
  }

  private int findIndex(int argIndex) {
    for (int i = 0; i < collectionIndexes.length; i++) {
      if (collectionIndexes[i] == argIndex) {
        return i;
      }
    }
    throw new RuntimeException("Inconsistent state");
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
    generateParameterMapping(mapping, TYPE_Byte, TYPE_byte, SIG_byteValue, SIG_setByte, java.sql.Types.TINYINT);
  }

  public void visit(Mapper mapper, ShortMapping mapping) {
    generateParameterMapping(mapping, TYPE_Short, TYPE_short, SIG_shortValue, SIG_setShort, java.sql.Types.SMALLINT);
  }

  public void visit(Mapper mapper, IntegerMapping mapping) {
    generateParameterMapping(mapping, TYPE_Integer, TYPE_int, SIG_intValue, SIG_setInt, java.sql.Types.INTEGER);
  }

  public void visit(Mapper mapper, LongMapping mapping) {
    generateParameterMapping(mapping, TYPE_Long, TYPE_long, SIG_longValue, SIG_setLong, java.sql.Types.BIGINT);
  }

  public void visit(Mapper mapper, FloatMapping mapping) {
    generateParameterMapping(mapping, TYPE_Float, TYPE_float, SIG_floatValue, SIG_setFloat, java.sql.Types.REAL);
  }

  public void visit(Mapper mapper, DoubleMapping mapping) {
    generateParameterMapping(mapping, TYPE_Double, TYPE_double, SIG_doubleValue, SIG_setDouble, java.sql.Types.DOUBLE);
  }

  public void visit(Mapper mapper, BooleanMapping mapping) {
    generateParameterMapping(mapping, TYPE_Boolean, TYPE_boolean, SIG_booleanValue, SIG_setBoolean, java.sql.Types.BOOLEAN);
  }

  private void generateParameterMapping(ParameterMapping mapping, Type boxedType, Type unboxedType, 
      Signature signatureUnbox, Signature signatureSet, int sqlType) {
    int argIndex = mapping.getIndex();
    int sqlIndex = mapping.getSqlIndexes()[0];
    Method[] getters = mapping.getGetters();
    Class<?> objectType = mapping.getType();

    if (mapping.usesArray()) {
      Local localIndex = co.make_local(TYPE_int);
      co.push(0);
      co.store_local(localIndex);
      Label labelLoopTest = co.make_label();
      co.goTo(labelLoopTest);
      Label labelLoopStart = co.make_label();
      co.mark(labelLoopStart);
      
      co.load_local(preparedStatement);
      co.push(sqlIndex);
      co.load_local(parameterIndexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
      co.load_arg(argIndex);
      co.load_local(localIndex);
      if (objectType.isPrimitive()) {
        if (getters != null) {
          co.array_load(Type.getType(getters[0].getDeclaringClass()));
          loadValue(-1, getters, mapping.usesCollection(), false);
        } else {
          co.array_load(unboxedType);
        }
      } else {
        // should we check null values?
        if (getters != null) {
          co.array_load(Type.getType(getters[0].getDeclaringClass()));
          loadValue(-1, getters, mapping.usesCollection(), false);
        } else {
          co.array_load(boxedType);
        }
        co.invoke_virtual(boxedType, signatureUnbox);
      }
      
      co.invoke_interface(preparedStatement.getType(), signatureSet);
      
      co.iinc(parameterIndexOffset, 1);
      
      co.iinc(localIndex, 1);
      co.mark(labelLoopTest);
      co.load_local(localIndex);
      co.load_arg(argIndex);
      co.arraylength();
      co.if_icmp(CodeEmitter.LT, labelLoopStart);
      
      co.iinc(parameterIndexOffset, -1);
      
    } else {

      if (!objectType.isPrimitive()) {
        // it's an Integer
        Label lIsNull = co.make_label();
        Label lEnd = co.make_label();
        // if (value != null) ps.setInt(index, value.intValue);
        loadValue(argIndex, getters, mapping.usesCollection(), true);
        co.ifnull(lIsNull);
        co.load_local(preparedStatement);
        co.push(sqlIndex);
        if (parameterIndexOffset != null) {
        	co.load_local(parameterIndexOffset);
        	co.math(CodeEmitter.ADD, TYPE_int);
        }
        loadValue(argIndex, getters, mapping.usesCollection(), false);
        co.invoke_virtual(boxedType, signatureUnbox);
        co.invoke_interface(preparedStatement.getType(), signatureSet);
        co.goTo(lEnd);
        // else ps.setNull(index, java.sql.Type.NUMERIC);
        co.mark(lIsNull);
        co.load_local(preparedStatement);
        co.push(sqlIndex);
        // co.push(java.sql.Types.NUMERIC);
        co.push(sqlType);
        co.invoke_interface(preparedStatement.getType(), SIG_setNull);
  
        co.mark(lEnd);
  
      } else {
        // it's an int
        // setInt(index, value);
        co.load_local(preparedStatement);
        co.push(sqlIndex);
        if (parameterIndexOffset != null) {
        	co.load_local(parameterIndexOffset);
        	co.math(CodeEmitter.ADD, TYPE_int);
        }
        loadValue(argIndex, getters, mapping.usesCollection(), false);
        co.invoke_interface(preparedStatement.getType(), signatureSet);
      }
    }
  }

  // implementation of CharacterMappingVisitor

  public final void visit(Mapper mapper, StringMapping mapping) {
    int argIndex = mapping.getIndex();
    int sqlIndex = mapping.getSqlIndexes()[0];
    Method[] getters = mapping.getGetters();

    if (mapping.usesArray()) {
      Local localIndex = co.make_local(TYPE_int);
      co.push(0);
      co.store_local(localIndex);
      Label labelLoopTest = co.make_label();
      co.goTo(labelLoopTest);
      Label labelLoopStart = co.make_label();
      co.mark(labelLoopStart);
      
      co.load_local(preparedStatement);
      co.push(sqlIndex);
      co.load_local(parameterIndexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
      co.load_arg(argIndex);
      co.load_local(localIndex);
      co.array_load(TYPE_String);
      
      co.invoke_interface(preparedStatement.getType(), SIG_setString);
      
      co.iinc(parameterIndexOffset, 1);
      
      co.iinc(localIndex, 1);
      co.mark(labelLoopTest);
      co.load_local(localIndex);
      co.load_arg(argIndex);
      co.arraylength();
      co.if_icmp(CodeEmitter.LT, labelLoopStart);
      
      co.iinc(parameterIndexOffset, -1);
      
    } else {
      // setString(index, value);
      co.load_local(preparedStatement);
      co.push(sqlIndex);
      if (parameterIndexOffset != null) {
        co.load_local(parameterIndexOffset);
    		co.math(CodeEmitter.ADD, TYPE_int);
      }
      loadValue(argIndex, getters, mapping.usesCollection(), true);
      co.invoke_interface(preparedStatement.getType(), SIG_setString);
    }
  }

  public final void visit(Mapper mapper, CharacterMapping mapping) {
    int argIndex = mapping.getIndex();
    int sqlIndex = mapping.getSqlIndexes()[0];
    Method[] getters = mapping.getGetters();
    Class<?> objectType = mapping.getType();

    if (mapping.usesArray()) {
      Local localIndex = co.make_local(TYPE_int);
      co.push(0);
      co.store_local(localIndex);
      Label labelLoopTest = co.make_label();
      co.goTo(labelLoopTest);
      Label labelLoopStart = co.make_label();
      co.mark(labelLoopStart);
      
      co.load_local(preparedStatement);
      co.push(sqlIndex);
      co.load_local(parameterIndexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
      co.load_arg(argIndex);
      co.load_local(localIndex);
      
      if (objectType.isPrimitive()) {
        // it's a char
        // Character.toString(char)
        co.array_load(TYPE_char);
        co.invoke_static(TYPE_Character, new Signature("toString", "(C)Ljava/lang/String;"));
      } else {
        // it's a Character
        co.array_load(TYPE_Character);
        co.dup();
        Label labelEnd = co.make_label();
        Label labelNull = co.make_label();
        co.ifnull(labelNull);
        co.invoke_virtual(TYPE_Character, new Signature("toString", "()Ljava/lang/String;"));
        co.goTo(labelEnd);
        co.mark(labelNull);
        co.pop();
        co.aconst_null();
        co.mark(labelEnd);
      }
      co.invoke_interface(preparedStatement.getType(), SIG_setString);
      
      co.iinc(parameterIndexOffset, 1);
      
      co.iinc(localIndex, 1);
      co.mark(labelLoopTest);
      co.load_local(localIndex);
      co.load_arg(argIndex);
      co.arraylength();
      co.if_icmp(CodeEmitter.LT, labelLoopStart);
      
      co.iinc(parameterIndexOffset, -1);
      
    } else {
      // setString(index, value);
      co.load_local(preparedStatement);
      co.push(sqlIndex);
      if (parameterIndexOffset != null) {
        co.load_local(parameterIndexOffset);
    	  co.math(CodeEmitter.ADD, TYPE_int);
      }
      if (objectType.isPrimitive()) {
        // it's a char
        // Character.toString(char)
        loadValue(argIndex, getters, mapping.usesCollection(), false);
        co.invoke_static(TYPE_Character, new Signature("toString", "(C)Ljava/lang/String;"));
      } else {
        // it's a Character
        loadValue(argIndex, getters, mapping.usesCollection(), true);
        co.dup();
        Label labelEnd = co.make_label();
        Label labelNull = co.make_label();
        co.ifnull(labelNull);
        co.invoke_virtual(TYPE_Character, new Signature("toString", "()Ljava/lang/String;"));
        co.goTo(labelEnd);
        co.mark(labelNull);
        co.pop();
        co.aconst_null();
        co.mark(labelEnd);
      }
      co.invoke_interface(preparedStatement.getType(), SIG_setString);
    }
  }

  // implementation of DateTimeVisitor

  public void visit(Mapper mapper, DateMapping mapping) {
    generateParameterMapping(mapping, TYPE_sqlDate, SIG_setDate, java.sql.Types.DATE);
  }

  public void visit(Mapper mapper, TimeMapping mapping) {
    generateParameterMapping(mapping, TYPE_sqlTime, SIG_setTime, java.sql.Types.TIME);
  }

  public void visit(Mapper mapper, TimestampMapping mapping) {
    generateParameterMapping(mapping, TYPE_sqlTimestamp, SIG_setTimestamp, java.sql.Types.TIMESTAMP);
  }

  public final void generateParameterMapping(AbstractDateTimeMapping mapping, Type sqlType, Signature sqlTypeSet,
      int sqlTypeCode) {
    int argIndex = mapping.getIndex();
    int sqlIndex = mapping.getSqlIndexes()[0];
    Method[] getters = mapping.getGetters();

    if (mapping.usesArray()) {
      Local localIndex = co.make_local(TYPE_int);
      co.push(0);
      co.store_local(localIndex);
      Label labelLoopTest = co.make_label();
      co.goTo(labelLoopTest);
      Label labelLoopStart = co.make_label();
      co.mark(labelLoopStart);
      
      co.load_local(preparedStatement);
      co.push(sqlIndex);
      co.load_local(parameterIndexOffset);
      co.math(CodeEmitter.ADD, TYPE_int);
      
      co.new_instance(sqlType);
      co.dup();

      co.load_arg(argIndex);
      co.load_local(localIndex);
      co.aaload();
      
      co.invoke_virtual(TYPE_Date, SIG_getTimeLong);
      co.invoke_constructor(sqlType, new Signature("<init>", "(J)V"));
      co.invoke_interface(preparedStatement.getType(), sqlTypeSet);
      
      co.iinc(parameterIndexOffset, 1);
      
      co.iinc(localIndex, 1);
      co.mark(labelLoopTest);
      co.load_local(localIndex);
      co.load_arg(argIndex);
      co.arraylength();
      co.if_icmp(CodeEmitter.LT, labelLoopStart);
      
      co.iinc(parameterIndexOffset, -1);
      
    } else {

      Label lIsNull = co.make_label();
      Label lEnd = co.make_label();
      // if (value != null) setDate(index, new java.sql.Date(value.getTime()));
      loadValue(argIndex, getters, mapping.usesCollection(), true);
      co.ifnull(lIsNull);
      co.load_local(preparedStatement);
      co.push(sqlIndex);
      if (parameterIndexOffset != null) {
        co.load_local(parameterIndexOffset);
    	  co.math(CodeEmitter.ADD, TYPE_int);
      }
      co.new_instance(sqlType);
      co.dup();
      loadValue(argIndex, getters, mapping.usesCollection(), false);
      co.invoke_virtual(TYPE_Date, SIG_getTimeLong);
      co.invoke_constructor(sqlType, new Signature("<init>", "(J)V"));
      co.invoke_interface(preparedStatement.getType(), sqlTypeSet);
      co.goTo(lEnd);
      // else ps.setNull(index, java.sql.Type.DATE);
      co.mark(lIsNull);
      co.load_local(preparedStatement);
      co.push(sqlIndex);
      if (parameterIndexOffset != null) {
        co.load_local(parameterIndexOffset);
    	  co.math(CodeEmitter.ADD, TYPE_int);
      }
      co.push(sqlTypeCode);
      co.invoke_interface(preparedStatement.getType(), SIG_setNull);
  
      co.mark(lEnd);
    }
  }

  public final void visit(Mapper mapper, AdapterMapping mapping) {
    int argIndex = mapping.getIndex();
    int[] sqlIndexes = mapping.getSqlIndexes();
    Method[] getters = mapping.getGetters();

    if (mapping.usesArray()) {
      if (mapping.getAdapter().getNumberOfColumns() != 1) {
        throw new RuntimeException("Only adapters for one column can be used for in-clauses");
      }
      
      Local localIndex = co.make_local(TYPE_int);
      co.push(0);
      co.store_local(localIndex);
      Label labelLoopTest = co.make_label();
      co.goTo(labelLoopTest);
      Label labelLoopStart = co.make_label();
      co.mark(labelLoopStart);
      
      if (mapping.getAdapter() instanceof GeneratorMappingAdapter) {
        // load the argument on top of the stack
        co.load_arg(argIndex);
        co.load_local(localIndex);
        co.aaload();
        ((GeneratorMappingAdapter) mapping.getAdapter()).generateToPreparedStatement(
            mapping, co, preparedStatement, sqlIndexes, parameterIndexOffset);
      } else if (mapping.getAdapter() instanceof DynamicMappingAdapter) {
        co.getfield(QueryObjectGenerator.getAdapterFieldName(mapping.getAdapter().getClass()));
        // set(PreparedStatement ps, Object value, int[] indexes)
        co.load_local(preparedStatement);
        co.load_arg(argIndex);
        co.load_local(localIndex);
        co.aaload();
        // new int[] {...}
        co.push(sqlIndexes.length);
        co.newarray(TYPE_int);
        for (int i = 0; i < sqlIndexes.length; i++) {
          co.dup();
          co.push(i);
          co.push(sqlIndexes[i]);
          if (parameterIndexOffset != null) {
            co.load_local(parameterIndexOffset);
            co.math(CodeEmitter.ADD, TYPE_int);
          }
          co.array_store(TYPE_int);
        }
        co.invoke_interface(Type.getType(DynamicMappingAdapter.class), 
            new Signature("set", "(Ljava/sql/PreparedStatement;Ljava/lang/Object;[I)V"));
      } else {
        throw new RuntimeException("Unsupported adapter type " + mapping.getAdapter());
      }

      co.iinc(parameterIndexOffset, 1);
      
      co.iinc(localIndex, 1);
      co.mark(labelLoopTest);
      co.load_local(localIndex);
      co.load_arg(argIndex);
      co.arraylength();
      co.if_icmp(CodeEmitter.LT, labelLoopStart);
      
      co.iinc(parameterIndexOffset, -1);
      
    } else {
      if (mapping.getAdapter() instanceof GeneratorMappingAdapter) {
        loadValue(argIndex, getters, mapping.usesCollection(), true);
        ((GeneratorMappingAdapter) mapping.getAdapter()).generateToPreparedStatement(
            mapping, co, preparedStatement, sqlIndexes, parameterIndexOffset);
  
      } else if (mapping.getAdapter() instanceof DynamicMappingAdapter) {
        co.getfield(QueryObjectGenerator.getAdapterFieldName(mapping.getAdapter().getClass()));
        // set(PreparedStatement ps, Object value, int[] indexes)
        co.load_local(preparedStatement);
        loadValue(argIndex, getters, mapping.usesCollection(), true);
        // new int[] {...}
        co.push(sqlIndexes.length);
        co.newarray(TYPE_int);
        for (int i = 0; i < sqlIndexes.length; i++) {
          co.dup();
          co.push(i);
          co.push(sqlIndexes[i]);
          if (parameterIndexOffset != null) {
            co.load_local(parameterIndexOffset);
        	  co.math(CodeEmitter.ADD, TYPE_int);
          }
          co.array_store(TYPE_int);
        }
        co.invoke_interface(Type.getType(DynamicMappingAdapter.class), 
            new Signature("set", "(Ljava/sql/PreparedStatement;Ljava/lang/Object;[I)V"));
      } else {
        throw new RuntimeException("Unsupported adapter type " + mapping.getAdapter());
      }
    }
  }

}
