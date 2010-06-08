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

import static sf.qof.codegen.Constants.EXCEPTION_EMPTY_RESULT;
import static sf.qof.codegen.Constants.EXCEPTION_MORE_THAN_ONE_RESULT;
import static sf.qof.codegen.Constants.FIELD_NAME_FETCH_SIZE;
import static sf.qof.codegen.Constants.FIELD_NAME_FIRST_RESULT;
import static sf.qof.codegen.Constants.FIELD_NAME_MAX_RESULTS;
import static sf.qof.codegen.Constants.SIG_add;
import static sf.qof.codegen.Constants.SIG_executeQuery;
import static sf.qof.codegen.Constants.SIG_getConnection;
import static sf.qof.codegen.Constants.SIG_next;
import static sf.qof.codegen.Constants.SIG_prepareStatement;
import static sf.qof.codegen.Constants.SIG_put;
import static sf.qof.codegen.Constants.SIG_setFetchSize;
import static sf.qof.codegen.Constants.SIG_setInt;
import static sf.qof.codegen.Constants.TYPE_Collection;
import static sf.qof.codegen.Constants.TYPE_Connection;
import static sf.qof.codegen.Constants.TYPE_Map;
import static sf.qof.codegen.Constants.TYPE_PreparedStatement;
import static sf.qof.codegen.Constants.TYPE_ResultSet;
import static sf.qof.codegen.Constants.TYPE_SQLException;
import static sf.qof.codegen.Constants.TYPE_Throwable;
import static sf.qof.codegen.Constants.TYPE_int;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.core.Block;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import sf.qof.ParameterReplacer;
import sf.qof.customizer.Customizer;
import sf.qof.dialect.SQLDialect;
import sf.qof.exception.ValidationException;
import sf.qof.mapping.Mapper;
import sf.qof.mapping.ParameterMapping;
import sf.qof.mapping.ResultMapping;

/**
 * Internal - SelectQueryMethodGenerator is the main generator class for select query methods.
 */
public class SelectQueryMethodGenerator {

  public static void addSelectQueryBody(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
    if (mapper.getResults() == null || mapper.getResults().size() == 0) {
      throw new ValidationException("No result mappers defined");
    }
    ResultMapping resultMapping = mapper.getResults().get(0);
    if (resultMapping.usesCollection()) {
      // check collection is List
     if (!Collection.class.isAssignableFrom(resultMapping.getCollectionType())
      && !Map.class.isAssignableFrom(resultMapping.getCollectionType())) {
        throw new ValidationException("Return collection type must be of type Collection or Map");
      }
      addSelectQueryBodyWithCollection(co, generator, mapper);
    } else {
      addSelectQueryBodyNoCollection(co, generator, mapper);
    }
  }

  private static void addSelectQueryBodyNoCollection(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
    Local localConnection = co.make_local(TYPE_Connection);
    Local localPreparedStatement = co.make_local(TYPE_PreparedStatement);
    Local localResultSet = co.make_local(TYPE_ResultSet);
    Local localException = co.make_local(TYPE_Throwable);
  
    // ResultSet rs = null;
    co.aconst_null();
    co.store_local(localResultSet);

    // connection = getConnection();
    co.load_this();
    co.invoke_virtual(Type.getType(generator.getClassNameType()), SIG_getConnection);
    co.store_local(localConnection);
    
    // try {
    Block tryBlockConnection = co.begin_block();

    // ps = connection.prepareStatement(sql);
    co.load_local(localConnection);
    co.push(mapper.getSql());
    co.invoke_interface(TYPE_Connection, SIG_prepareStatement);
    co.store_local(localPreparedStatement);
  
    // try {
    Block tryBlockStatement = co.begin_block();
  
    // set fetch size to two as we do not expect more than one row
    // ps.setFetchSize(2);
    co.load_local(localPreparedStatement);
    co.push(2);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setFetchSize);
  
    ParameterMappingGenerator pmg = new ParameterMappingGenerator(co, localPreparedStatement, null, null, null);
    mapper.acceptParameterMappers(pmg);
  
    // ps.executeQuery();
    co.load_local(localPreparedStatement);
    co.invoke_interface(TYPE_PreparedStatement, SIG_executeQuery);
    co.store_local(localResultSet);

    // try {
    Block tryBlockResultSet = co.begin_block();
    
    Class<?> returnType = mapper.getMethod().getReturnInfo().getType();
  
    Label labelThrowNoResult = co.make_label();
    Label labelThrowMoreThanOneResult = co.make_label();
    Label labelFinally = co.make_label();
  
    Local localResult = null;
  
    localResult = co.make_local(Type.getType(returnType));
    if (!returnType.isPrimitive()) {
      co.aconst_null();
      co.store_local(localResult);
    }
    // if (rs.next()) {
    co.load_local(localResultSet);
    co.invoke_interface(TYPE_ResultSet, SIG_next);
    if (returnType.isPrimitive()) {
      co.if_jump(CodeEmitter.EQ, labelThrowNoResult);
    } else {
      co.if_jump(CodeEmitter.EQ, labelFinally);
    }
  
    if (!(mapper.getResults().size() == 1 && mapper.getResults().get(0).usesAtomic())) {
      EmitUtils.createAndStoreNewResultObject(co, mapper, localResultSet, localResult);
    }
  
    // --- get results
    ResultMappingGenerator rmp = new ResultMappingGenerator(co, localResultSet, localResult, null, false, null);
    mapper.acceptResultMappers(rmp);
  
    // if (rs.next()) {
    co.load_local(localResultSet);
    co.invoke_interface(TYPE_ResultSet, SIG_next);
    co.if_jump(CodeEmitter.NE, labelThrowMoreThanOneResult);
    co.goTo(labelFinally);
  
    co.mark(labelThrowMoreThanOneResult);
    // throw new SQLException("More than one result in result set");
    co.throw_exception(TYPE_SQLException, EXCEPTION_MORE_THAN_ONE_RESULT);
    // throw new SQLException("Empty result set returned");
    co.mark(labelThrowNoResult);
    co.throw_exception(TYPE_SQLException, EXCEPTION_EMPTY_RESULT);
  
    // finally blocks
    co.mark(labelFinally);
    
    tryBlockResultSet.end();
    EmitUtils.emitClose(co, localResultSet);

    tryBlockStatement.end();
    EmitUtils.emitClose(co, localPreparedStatement);
    
    tryBlockConnection.end();
    EmitUtils.emitUngetConnection(co, Type.getType(generator.getClassNameType()), localConnection);
  
    // return result
    co.load_local(localResult);
    co.return_value();
    // }
  
    // exception handlers
    EmitUtils.emitCatchException(co, tryBlockResultSet, null);
    Block tryBlockResultSet2 = co.begin_block();
    Block tryBlockStatement2 = co.begin_block();
    co.store_local(localException);
    EmitUtils.emitClose(co, localResultSet);
    co.load_local(localException);
    co.athrow();
    tryBlockResultSet2.end();
    
    EmitUtils.emitCatchException(co, tryBlockStatement, null);
    EmitUtils.emitCatchException(co, tryBlockResultSet2, null);
    co.store_local(localException);
    EmitUtils.emitClose(co, localPreparedStatement);
    co.load_local(localException);
    co.athrow();
    tryBlockStatement2.end();
    
    EmitUtils.emitCatchException(co, tryBlockConnection, null);
    EmitUtils.emitCatchException(co, tryBlockStatement2, null);
    co.store_local(localException);
    EmitUtils.emitUngetConnection(co, Type.getType(generator.getClassNameType()), localConnection);
    co.load_local(localException);
    co.athrow();
  }

  private static void addSelectQueryBodyWithCollection(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
    Local localConnection = co.make_local(TYPE_Connection);
    Local localPreparedStatement = co.make_local(TYPE_PreparedStatement);
    Local localResultSet = co.make_local(TYPE_ResultSet);
    Local localResultCollection = co.make_local(TYPE_Collection);
    Local localException = co.make_local(TYPE_Throwable);
    Local localMapKey;
    boolean usesMap = false;
    Block tryBlockConnection;
    Block tryBlockStatement;
    Block tryBlockResultSet;
    
    SQLDialect sqlDialect = generator.getSqlDialect();
    boolean implementPaging = generator.getImplementPaging();
    String classNameType = generator.getClassNameType();
    Customizer customizer = generator.getCustomizer();
    
    Class<?> resultMapKeyType = mapper.getMethod().getReturnInfo().getMapKeyType();
    if (resultMapKeyType == null) {
      localMapKey = null; //NOPMD
    } else {
      localMapKey = co.make_local(Type.getType(resultMapKeyType));
    }
    
    // ResultSet rs = null;
    co.aconst_null();
    co.store_local(localResultSet);
    // list = new ArrayList();
    Type collectionType = null;
    Class<?> resultCollectionType = mapper.getMethod().getReturnInfo().getCollectionType();
    if (resultCollectionType == List.class) {
    	collectionType = customizer.getListType();
    } else if (resultCollectionType == Set.class) {
    	collectionType = customizer.getSetType();
    } else if (resultCollectionType == Map.class) {
      usesMap = true;
      collectionType = customizer.getMapType();
    } else {
    	throw new ValidationException("Collection type " + resultCollectionType + " is not allowed");
    }
    co.new_instance(collectionType);
    co.dup();
    co.invoke_constructor(collectionType);
    co.store_local(localResultCollection);
  
    Local localParameterIndexOffset = null;
    if (mapper.usesArray() || 
        (implementPaging && sqlDialect.limitParametersBeforeQueryParameters())) {
      localParameterIndexOffset = co.make_local(TYPE_int);
    }
    if (mapper.usesArray()) {
      co.push(0);
      co.store_local(localParameterIndexOffset);
    }
  
    if (implementPaging) {
      String sql = mapper.getSql();
      
      Label label1 = co.make_label();
      Label label2 = co.make_label();
      Label label3 = co.make_label();
      co.load_this();
      co.getfield(FIELD_NAME_FIRST_RESULT);
      co.if_jump(CodeEmitter.NE, label1);
      co.load_this();
      co.getfield(FIELD_NAME_MAX_RESULTS);
      co.if_jump(CodeEmitter.NE, label1);
      
      // connection = getConnection();
      co.load_this();
      co.invoke_virtual(Type.getType(classNameType), SIG_getConnection);
      co.store_local(localConnection);
      // ps = connection.prepareStatement(sql);
      co.load_local(localConnection);
      pushSql(co, mapper, sql);
      co.invoke_interface(TYPE_Connection, SIG_prepareStatement);
      co.store_local(localPreparedStatement);
      
      if (sqlDialect.limitParametersBeforeQueryParameters()) {
      	co.push(0);
      	co.store_local(localParameterIndexOffset);
      }
  
      co.goTo(label2);
      
      co.mark(label1);
      co.load_this();
      co.getfield(FIELD_NAME_FIRST_RESULT);
      co.if_jump(CodeEmitter.NE, label3);
  
      co.load_this();
      co.invoke_virtual(Type.getType(classNameType), SIG_getConnection);
      co.store_local(localConnection);
      
      // try {
      tryBlockConnection = co.begin_block();

      co.load_local(localConnection);
      //co.push(sqlDialect.getLimitString(sql, false));
      pushSql(co, mapper, sqlDialect.getLimitString(sql, false));
      co.invoke_interface(TYPE_Connection, SIG_prepareStatement);
      co.store_local(localPreparedStatement);
  
      // try {
      tryBlockStatement = co.begin_block();

      if (sqlDialect.limitParametersBeforeQueryParameters()) {
        co.push(1);
        co.store_local(localParameterIndexOffset);
        
        co.load_local(localPreparedStatement);
        co.push(1);
        co.load_this();
        co.getfield(FIELD_NAME_MAX_RESULTS);
        co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
        
      } else {
        co.load_local(localPreparedStatement);
        co.push(1 + mapper.getMaxParameterSqlIndex());
        co.load_this();
        co.getfield(FIELD_NAME_MAX_RESULTS);
        co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
      }
      co.goTo(label2);
  
      co.mark(label3);
  
      co.load_this();
      co.invoke_virtual(Type.getType(classNameType), SIG_getConnection);
      co.store_local(localConnection);
      
      // try {
      tryBlockConnection = co.begin_block();

      co.load_local(localConnection);

      pushSql(co, mapper, sqlDialect.getLimitString(sql, true));
      co.invoke_interface(TYPE_Connection, SIG_prepareStatement);
      co.store_local(localPreparedStatement);
      
      // try {
      tryBlockStatement = co.begin_block();

  
      if (sqlDialect.limitParametersBeforeQueryParameters()) {
        co.push(2);
        co.store_local(localParameterIndexOffset);
  
        if (sqlDialect.limitOffsetFirst()) {
          co.load_local(localPreparedStatement);
          co.push(1);
          co.load_this();
          co.getfield(FIELD_NAME_FIRST_RESULT);
          co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
  
          co.load_local(localPreparedStatement);
          co.push(2);
          if (sqlDialect.limitAddOffset()) {
            co.load_this();
            co.getfield(FIELD_NAME_FIRST_RESULT);
            co.load_this();
            co.getfield(FIELD_NAME_MAX_RESULTS);
            co.math(CodeEmitter.ADD, TYPE_int);
          } else {
            co.load_this();
            co.getfield(FIELD_NAME_MAX_RESULTS);
          }
          co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
        } else {
          co.load_local(localPreparedStatement);
          co.push(1);
          if (sqlDialect.limitAddOffset()) {
            co.load_this();
            co.getfield(FIELD_NAME_FIRST_RESULT);
            co.load_this();
            co.getfield(FIELD_NAME_MAX_RESULTS);
            co.math(CodeEmitter.ADD, TYPE_int);
          } else {
            co.load_this();
            co.getfield(FIELD_NAME_MAX_RESULTS);
          }
          co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
  
          co.load_local(localPreparedStatement);
          co.push(2);
          co.load_this();
          co.getfield(FIELD_NAME_FIRST_RESULT);
          co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
        }
      } else {
        if (sqlDialect.limitOffsetFirst()) {
          co.load_local(localPreparedStatement);
          co.push(1 + mapper.getMaxParameterSqlIndex());
          co.load_this();
          co.getfield(FIELD_NAME_FIRST_RESULT);
          co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
  
          if (sqlDialect.limitAddOffset()) {
            co.load_local(localPreparedStatement);
            co.push(2 + mapper.getMaxParameterSqlIndex());
            co.load_this();
            co.getfield(FIELD_NAME_FIRST_RESULT);
            co.load_this();
            co.getfield(FIELD_NAME_MAX_RESULTS);
            co.math(CodeEmitter.ADD, TYPE_int);
          } else {
            co.load_local(localPreparedStatement);
            co.push(2 + mapper.getMaxParameterSqlIndex());
            co.load_this();
            co.getfield(FIELD_NAME_MAX_RESULTS);
          }
          co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
        } else {
          if (sqlDialect.limitAddOffset()) {
            co.load_local(localPreparedStatement);
            co.push(1 + mapper.getMaxParameterSqlIndex());
            co.load_this();
            co.getfield(FIELD_NAME_FIRST_RESULT);
            co.load_this();
            co.getfield(FIELD_NAME_MAX_RESULTS);
            co.math(CodeEmitter.ADD, TYPE_int);
          } else {
            co.load_local(localPreparedStatement);
            co.push(1 + mapper.getMaxParameterSqlIndex());
            co.load_this();
            co.getfield(FIELD_NAME_MAX_RESULTS);
          }
          co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
  
          co.load_local(localPreparedStatement);
          co.push(2 + mapper.getMaxParameterSqlIndex());
          co.load_this();
          co.getfield(FIELD_NAME_FIRST_RESULT);
          co.invoke_interface(TYPE_PreparedStatement, SIG_setInt);
        }
      }
      
      co.mark(label2);
  	
      // reset firstResult, maxResults
  
      // firstResult = 0;
      co.load_this();
      co.push(0);
      co.putfield(FIELD_NAME_FIRST_RESULT);
      // maxResults = 0;
      co.load_this();
      co.push(0);
      co.putfield(FIELD_NAME_MAX_RESULTS);
  
    } else {
      // ps = connection.prepareStatement(sql);
      co.load_this();
      co.invoke_virtual(Type.getType(classNameType), SIG_getConnection);
      co.store_local(localConnection);
      
      // try {
      tryBlockConnection = co.begin_block();

      co.load_local(localConnection);
      pushSql(co, mapper, mapper.getSql());
      co.invoke_interface(TYPE_Connection, SIG_prepareStatement);
      co.store_local(localPreparedStatement);
      
      // try {
      tryBlockStatement = co.begin_block();
    }

    // ps.setFetchSize(fetchSize);
    co.load_local(localPreparedStatement);
    co.load_this();
    generator.emitGetField(co, FIELD_NAME_FETCH_SIZE);
    co.invoke_interface(TYPE_PreparedStatement, SIG_setFetchSize);
  
    ParameterMappingGenerator pmg = new ParameterMappingGenerator(co, localPreparedStatement, 
    	null, null, localParameterIndexOffset);
    mapper.acceptParameterMappers(pmg);
  
    // ps.executeQuery();
    co.load_local(localPreparedStatement);
    co.invoke_interface(TYPE_PreparedStatement, SIG_executeQuery);
    co.store_local(localResultSet);
    
    // try {
    tryBlockResultSet = co.begin_block();
  
    ResultMapping resultMapping = null;
    for (ResultMapping rm : mapper.getResults()) {
      if (!rm.isMapKey()) {
        resultMapping = rm;
        break;
      }
    }
  
    Label labelWhile = co.make_label();
    Label labelFinally = co.make_label();
  
    Local localResult = co.make_local(Type.getType(resultMapping.getBeanType() != null ? resultMapping
        .getBeanType() : resultMapping.getType()));
    // while (rs.next()) {
    co.mark(labelWhile);
    co.load_local(localResultSet);
    co.invoke_interface(TYPE_ResultSet, SIG_next);
    co.if_jump(CodeEmitter.EQ, labelFinally);
  
    if (!(mapper.getResults().size() == 1 && resultMapping.usesAtomic())
        && !(usesMap && mapper.getResults().size() == 2 && resultMapping.usesAtomic())) {
      EmitUtils.createAndStoreNewResultObject(co, mapper, localResultSet, localResult);
    }
  
    // get results
    ResultMappingGenerator rmp = new ResultMappingGenerator(co, localResultSet, localResult, localMapKey, false, null);
    mapper.acceptResultMappers(rmp);
  
    if (usesMap) {
      co.load_local(localResultCollection);
      co.load_local(localMapKey);
      co.load_local(localResult);
      co.invoke_interface(TYPE_Map, SIG_put);
      co.pop();
    } else {
      // add result
      co.load_local(localResultCollection);
      co.load_local(localResult);
      co.invoke_interface(TYPE_Collection, SIG_add);
      co.pop();
    }
  
    // } // end while
    co.goTo(labelWhile);
  
    // finally block
    co.mark(labelFinally);

    tryBlockResultSet.end();
    EmitUtils.emitClose(co, localResultSet);

    tryBlockStatement.end();
    EmitUtils.emitClose(co, localPreparedStatement);
    
    tryBlockConnection.end();
    EmitUtils.emitUngetConnection(co, Type.getType(generator.getClassNameType()), localConnection);
  
    // return result
    co.load_local(localResultCollection);
    co.return_value();
    // }
    
    // exception handlers
    EmitUtils.emitCatchException(co, tryBlockResultSet, null);
    Block tryBlockResultSet2 = co.begin_block();
    Block tryBlockStatement2 = co.begin_block();
    co.store_local(localException);
    EmitUtils.emitClose(co, localResultSet);
    co.load_local(localException);
    co.athrow();
    tryBlockResultSet2.end();
    
    EmitUtils.emitCatchException(co, tryBlockStatement, null);
    EmitUtils.emitCatchException(co, tryBlockResultSet2, null);
    co.store_local(localException);
    EmitUtils.emitClose(co, localPreparedStatement);
    co.load_local(localException);
    co.athrow();
    tryBlockStatement2.end();
    
    EmitUtils.emitCatchException(co, tryBlockConnection, null);
    EmitUtils.emitCatchException(co, tryBlockStatement2, null);
    co.store_local(localException);
    EmitUtils.emitUngetConnection(co, Type.getType(generator.getClassNameType()), localConnection);
    co.load_local(localException);
    co.athrow();

  }

  private static void pushSql(CodeEmitter co, Mapper mapper, String sql) {
    if (mapper.usesArray()) {
      co.push(sql);
      List<ParameterMapping> mappings = mapper.getParameters(); 
      for (int i = mappings.size() - 1; i >= 0; i--) {
        ParameterMapping mapping = mappings.get(i);
        if (mapping.usesArray()) {
          co.push(mapping.getSqlIndexes()[0]); // index
          co.load_arg(mapping.getIndex());
          co.arraylength(); // numArgs
          if (mapping.getParameterSeparator() == null) {
            co.push(",");
          } else {
            co.push(mapping.getParameterSeparator());
          }
          co.invoke_static(Type.getType(ParameterReplacer.class), 
              new Signature("replace", "(Ljava/lang/String;IILjava/lang/String;)Ljava/lang/String;"));
        }
      }
    } else {
      co.push(sql);
    }
  }

}
