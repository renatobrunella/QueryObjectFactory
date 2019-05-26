/*
 * Copyright 2007 - 2011 brunella ltd
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

import static sf.qof.codegen.Constants.EXCEPTION_COLLECTIONS_DIFFERENT_SIZE;
import static sf.qof.codegen.Constants.FIELD_NAME_BATCH_SIZE;
import static sf.qof.codegen.Constants.SIG_addBatch;
import static sf.qof.codegen.Constants.SIG_execute;
import static sf.qof.codegen.Constants.SIG_executeBatch;
import static sf.qof.codegen.Constants.SIG_executeUpdate;
import static sf.qof.codegen.Constants.SIG_hasNext;
import static sf.qof.codegen.Constants.SIG_iterator;
import static sf.qof.codegen.Constants.SIG_iterator_next;
import static sf.qof.codegen.Constants.SIG_prepareCall;
import static sf.qof.codegen.Constants.SIG_size;
import static sf.qof.codegen.Constants.TYPE_CallableStatement;
import static sf.qof.codegen.Constants.TYPE_Collection;
import static sf.qof.codegen.Constants.TYPE_Connection;
import static sf.qof.codegen.Constants.TYPE_Iterator;
import static sf.qof.codegen.Constants.TYPE_SQLException;
import static sf.qof.codegen.Constants.TYPE_Throwable;
import static sf.qof.codegen.Constants.TYPE_int;
import net.sf.cglib.core.Block;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import sf.qof.exception.ValidationException;
import sf.qof.mapping.Mapper;
import sf.qof.mapping.MethodParameterInfo;

/**
 * Internal - CallQueryMethodGenerator is the main generator class for call query methods.
 */
public class CallQueryMethodGenerator {

  public static void addCallQueryBody(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
    if (mapper.usesArray()) {
      throw new ValidationException("Array parameters are not allowed for call statements");
    }
    if (mapper.getMethod().getCollectionParameterInfos().length > 0) {
      addCallQueryBodyWithCollection(co, generator, mapper);
    } else {
      addCallQueryBodyNoCollection(co, generator, mapper);
    }
  }

  private static void addCallQueryBodyNoCollection(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
    Local localConnection = co.make_local(TYPE_Connection);
    Local localCallableStatement = co.make_local(TYPE_CallableStatement);
    Local localException = co.make_local(TYPE_Throwable);

    // connection = getConnection();
    EmitUtils.emitGetConnection(co, generator, localConnection);
    
    // try {
    Block tryBlockConnection = co.begin_block();
    
    // ps = connection.prepareCall("{ ? = call xyz (?,?) }");
    co.load_local(localConnection);
    co.push(mapper.getSql());
    co.invoke_interface(TYPE_Connection, SIG_prepareCall);
    co.store_local(localCallableStatement);

    // try {
    Block tryBlockStatement = co.begin_block();

    // set the parameters
    ParameterMappingGenerator pmg = new ParameterMappingGenerator(co, localCallableStatement, null, null, null);
    mapper.acceptParameterMappers(pmg);
  
    // register output parameters
    OutputParameterRegistrationMappingGenerator omg = new OutputParameterRegistrationMappingGenerator(co, localCallableStatement);
    mapper.acceptResultMappers(omg);
  
    // ps.execute();
    co.load_local(localCallableStatement);
    co.invoke_interface(TYPE_CallableStatement, SIG_execute);
    co.pop(); // not interested in result
  
    Class<?> returnType = mapper.getMethod().getReturnInfo().getType();
  
    Local localResult = null;
  
    if (returnType != void.class) {
      localResult = co.make_local(Type.getType(returnType));
      if (!(mapper.getResults().size() == 1 && mapper.getResults().get(0).usesAtomic())) {
        EmitUtils.createAndStoreNewResultObject(co, mapper, localCallableStatement, localResult);
      }
  
      // --- get results
      ResultMappingGenerator rmp = new ResultMappingGenerator(co, localCallableStatement, localResult, null, false, null);
      mapper.acceptResultMappers(rmp);
    }
  
    // finally
    tryBlockStatement.end();
    EmitUtils.emitClose(co, localCallableStatement);
    
    tryBlockConnection.end();
    EmitUtils.emitUngetConnection(co, generator, localConnection);
  
    // return result
    if (localResult != null) {
      co.load_local(localResult);
    }
    co.return_value();
    // }
  
    // exception handler
    EmitUtils.emitCatchException(co, tryBlockStatement, null);
    Block tryBlockStatement2 = co.begin_block();
    co.store_local(localException);
    EmitUtils.emitClose(co, localCallableStatement);
    co.load_local(localException);
    co.athrow();
    tryBlockStatement2.end();
    
    EmitUtils.emitCatchException(co, tryBlockConnection, null);
    EmitUtils.emitCatchException(co, tryBlockStatement2, null);
    co.store_local(localException);
    EmitUtils.emitUngetConnection(co, generator, localConnection);
    co.load_local(localException);
    co.athrow();
  }

  private static void addCallQueryBodyWithCollection(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
    if (mapper.getResults() != null && mapper.getResults().size() > 0) {
      throw new ValidationException("No results allowed for call with collection");
    }
  
    // check for valid return type
    Class<?> returnType = mapper.getMethod().getReturnInfo().getType();
    if (returnType != Void.TYPE) {
    		throw new ValidationException("Only void is allowed as return type");
    }
    MethodParameterInfo[] collectionParameterInfos = mapper.getMethod().getCollectionParameterInfos();
    int numParameterCollections = collectionParameterInfos.length;
  
    // check for different sizes
    if (numParameterCollections > 1) {
      Label labelException = co.make_label();
      Label labelNoException = co.make_label();
  
      for (int i = 0; i < numParameterCollections - 1; i++) {
        co.load_arg(collectionParameterInfos[i].getIndex());
        co.invoke_interface(TYPE_Collection, SIG_size);
        co.load_arg(collectionParameterInfos[i + 1].getIndex());
        co.invoke_interface(TYPE_Collection, SIG_size);
        co.if_icmp(CodeEmitter.NE, labelException);
      }
      co.goTo(labelNoException);
  
      co.mark(labelException);
      co.throw_exception(TYPE_SQLException, EXCEPTION_COLLECTIONS_DIFFERENT_SIZE);
  
      co.mark(labelNoException);
    }
  
    // if (parameter.size() == 0) return;
    co.load_arg(collectionParameterInfos[0].getIndex());
    co.invoke_interface(TYPE_Collection, SIG_size);
    Label labelNotZero = co.make_label();
    co.if_jump(CodeEmitter.NE, labelNotZero);
    co.return_value();
    co.mark(labelNotZero);
  
    Local localConnection = co.make_local(TYPE_Connection);
    Local localCallableStatement = co.make_local(TYPE_CallableStatement);
    Local localException = co.make_local(TYPE_Throwable);

    // connection = getConnection();
    EmitUtils.emitGetConnection(co, generator, localConnection);
    
    // try {
    Block tryBlockConnection = co.begin_block();

    // ps = connection.prepareStatement("select count(*) from person");
    co.load_local(localConnection);
    co.push(mapper.getSql());
    co.invoke_interface(TYPE_Connection, SIG_prepareCall);
    co.store_local(localCallableStatement);
    
    // try {
    Block tryBlockStatement = co.begin_block();
  
    // start the loop
    // Iterator<Person> iter = list.iterator();
    // int i = 0;
    // while (iter.hasNext()) {
    // Person person = iter.next();
    // i++;
    Local localCounter = co.make_local(TYPE_int);
    co.push(0);
    co.store_local(localCounter);
    Local[] localIterators = new Local[numParameterCollections];
    Local[] localObjects = new Local[numParameterCollections];
    for (int i = 0; i < numParameterCollections; i++) {
      localIterators[i] = co.make_local(TYPE_Iterator);
      co.load_arg(collectionParameterInfos[i].getIndex());
      co.invoke_interface(TYPE_Collection, SIG_iterator);
      co.store_local(localIterators[i]);
  
      localObjects[i] = co.make_local(Type.getType(collectionParameterInfos[i].getCollectionElementType()));
    }
  
    // while
    Label labelBeginWhile = co.make_label();
    Label labelEndWhile = co.make_label();
    co.mark(labelBeginWhile);
    co.load_local(localIterators[0]);
    co.invoke_interface(TYPE_Iterator, SIG_hasNext);
    co.if_jump(CodeEmitter.EQ, labelEndWhile);
  
    for (int i = 0; i < numParameterCollections; i++) {
      co.load_local(localIterators[i]);
      co.invoke_interface(TYPE_Iterator, SIG_iterator_next);
      co.checkcast(Type.getType(collectionParameterInfos[i].getCollectionElementType()));
      co.store_local(localObjects[i]);
    }
    co.iinc(localCounter, 1);
  
    ParameterMappingGenerator pmg = new ParameterMappingGenerator(co, localCallableStatement, localObjects,
        collectionParameterInfos, null);
    mapper.acceptParameterMappers(pmg);
  
    // if (batchSize > 0) {
    // ps.addBatch();
    // if (i >= batchSize) {
    // ps.executeBatch();
    // i = 0;
    // }
    // } else {
    // ps.execute();
    // }
    co.load_this();
    co.getfield(FIELD_NAME_BATCH_SIZE);
    Label labelNoBatching = co.make_label();
    co.if_jump(CodeEmitter.LE, labelNoBatching);
  
    co.load_local(localCallableStatement);
    co.invoke_interface(TYPE_CallableStatement, SIG_addBatch);
  
    // if (i >= batchSize) {
    // ps.executeBatch();
    // i = 0;
    // }
    co.load_local(localCounter);
    co.load_this();
    co.getfield(FIELD_NAME_BATCH_SIZE);
    Label labelAfter = co.make_label();
    co.if_icmp(CodeEmitter.LT, labelAfter);
  
    co.load_local(localCallableStatement);
    co.invoke_interface(TYPE_CallableStatement, SIG_executeBatch);
    co.pop();
    co.push(0);
    co.store_local(localCounter);
  
    co.mark(labelAfter);
  
    co.goTo(labelBeginWhile);
  
    co.mark(labelNoBatching);
  
    co.load_local(localCallableStatement);
    co.invoke_interface(TYPE_CallableStatement, SIG_executeUpdate);
    co.pop();
  
    co.goTo(labelBeginWhile);
  
    co.mark(labelEndWhile);
  
    // if (batchSize > 0 && i > 0) {
    // ps.executeBatch();
    // }
    Label labelAfter2 = co.make_label();
    co.load_local(localCounter);
    co.if_jump(CodeEmitter.LE, labelAfter2);
    co.load_this();
    co.getfield(FIELD_NAME_BATCH_SIZE);
    co.if_jump(CodeEmitter.LE, labelAfter2);
  
    co.load_local(localCallableStatement);
    co.invoke_interface(TYPE_CallableStatement, SIG_executeBatch);
    co.pop();
  
    co.mark(labelAfter2);
  
    // finally
    tryBlockStatement.end();
    EmitUtils.emitClose(co, localCallableStatement);
    
    tryBlockConnection.end();
    EmitUtils.emitUngetConnection(co, generator, localConnection);
  
    // return result
    co.return_value();
    // }
  
    // exception handler
    EmitUtils.emitCatchException(co, tryBlockStatement, null);
    Block tryBlockStatement2 = co.begin_block();
    co.store_local(localException);
    EmitUtils.emitClose(co, localCallableStatement);
    co.load_local(localException);
    co.athrow();
    tryBlockStatement2.end();
    
    EmitUtils.emitCatchException(co, tryBlockConnection, null);
    EmitUtils.emitCatchException(co, tryBlockStatement2, null);
    co.store_local(localException);
    EmitUtils.emitUngetConnection(co, generator, localConnection);
    co.load_local(localException);
    co.athrow();
  }

}
