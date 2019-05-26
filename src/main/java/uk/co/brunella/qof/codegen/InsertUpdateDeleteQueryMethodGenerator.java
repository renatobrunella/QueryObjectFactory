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
package uk.co.brunella.qof.codegen;

import net.sf.cglib.core.Block;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import uk.co.brunella.qof.ParameterReplacer;
import uk.co.brunella.qof.exception.ValidationException;
import uk.co.brunella.qof.mapping.Mapper;
import uk.co.brunella.qof.mapping.MethodParameterInfo;
import uk.co.brunella.qof.mapping.ParameterMapping;
import uk.co.brunella.qof.mapping.QueryType;

import java.util.List;

import static uk.co.brunella.qof.codegen.Constants.*;

/**
 * Internal - InsertUpdateDeleteQueryMethodGenerator is the main generator class for insert, update and delete query methods.
 */
public class InsertUpdateDeleteQueryMethodGenerator {

    public static void addInsertUpdateDeleteQueryBody(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
        if (mapper.getQueryType() == QueryType.INSERT && mapper.usesArray()) {
            throw new ValidationException("Array parameters are not allowed for insert statements");
        }
        if (mapper.getMethod().getCollectionParameterInfos().length > 0) {
            addInsertUpdateDeleteQueryBodyWithCollection(co, generator, mapper);
        } else {
            addInsertUpdateDeleteQueryBodyNoCollection(co, generator, mapper);
        }
    }

    private static void addInsertUpdateDeleteQueryBodyNoCollection(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
        Local localConnection = co.make_local(TYPE_Connection);
        Local localPreparedStatement = co.make_local(TYPE_PreparedStatement);
        Local localException = co.make_local(TYPE_Throwable);

        // check for valid return type
        Class<?> returnType = mapper.getMethod().getReturnInfo().getType();
        if (returnType != Integer.TYPE && returnType != Void.TYPE) {
            throw new ValidationException("Only int or void is allowed as return type");
        }

        // connection = getConnection();
        EmitUtils.emitGetConnection(co, generator, localConnection);

        // try {
        Block tryBlockConnection = co.begin_block();

        // ps = connection.prepareStatement("select count(*) from person");
        co.load_local(localConnection);
        pushSql(co, mapper, mapper.getSql());
        co.invoke_interface(TYPE_Connection, SIG_prepareStatement);
        co.store_local(localPreparedStatement);

        // try {
        Block tryBlockStatement = co.begin_block();

        Local localParameterIndexOffset = null;
        if (mapper.usesArray()) {
            localParameterIndexOffset = co.make_local(TYPE_int);
            co.push(0);
            co.store_local(localParameterIndexOffset);
        }

        ParameterMappingGenerator pmg = new ParameterMappingGenerator(co, localPreparedStatement, null, null, localParameterIndexOffset);
        mapper.acceptParameterMappers(pmg);

        // ps.executeUpdate();
        co.load_local(localPreparedStatement);
        co.invoke_interface(TYPE_PreparedStatement, SIG_executeUpdate);
        Local localResult = null;
        if (returnType == Integer.TYPE) {
            localResult = co.make_local(TYPE_int);
            co.store_local(localResult);
        } else {
            co.pop();
        }

        // finally
        tryBlockStatement.end();
        EmitUtils.emitClose(co, localPreparedStatement);

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
        EmitUtils.emitClose(co, localPreparedStatement);
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

    private static void addInsertUpdateDeleteQueryBodyWithCollection(CodeEmitter co, QueryObjectGenerator generator, Mapper mapper) {
        MethodParameterInfo[] collectionParameterInfos = mapper.getMethod().getCollectionParameterInfos();
        int numParameterCollections = collectionParameterInfos.length;


        // check for valid return type
        Class<?> returnType = mapper.getMethod().getReturnInfo().getType();
        if (!(returnType == Void.TYPE)
                && !(returnType.isArray() && returnType.getComponentType() == Integer.TYPE)) {
            throw new ValidationException("Only int[] or void is allowed as return type");
        }

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
        if (returnType.isArray()) {
            // push new int[0]
            co.push(0);
            co.newarray(TYPE_int);
        }
        co.return_value();
        co.mark(labelNotZero);

        Local localConnection = co.make_local(TYPE_Connection);
        Local localPreparedStatement = co.make_local(TYPE_PreparedStatement);
        Local localException = co.make_local(TYPE_Throwable);

        // connection = getConnection();
        EmitUtils.emitGetConnection(co, generator, localConnection);

        // try {
        Block tryBlockConnection = co.begin_block();

        // ps = connection.prepareStatement("select count(*) from person");
        co.load_local(localConnection);
        pushSql(co, mapper, mapper.getSql());
        co.invoke_interface(TYPE_Connection, SIG_prepareStatement);
        co.store_local(localPreparedStatement);

        // try {
        Block tryBlockStatement = co.begin_block();

        Local localResult = null;
        Local localPartResult = null;
        Local localIndex = null;

        if (returnType.isArray()) {
            // result = new int[collection.size()];
            localResult = co.make_local(TYPE_intArray);
            localPartResult = co.make_local(TYPE_intArray);
            co.load_arg(collectionParameterInfos[0].getIndex());
            co.invoke_interface(TYPE_Collection, SIG_size);
            co.newarray(TYPE_int);
            co.store_local(localResult);
            // int index = 0;
            localIndex = co.make_local(TYPE_int);
            co.push(0);
            co.store_local(localIndex);
        }

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

        Local localParameterIndexOffset = null;
        if (mapper.usesArray()) {
            localParameterIndexOffset = co.make_local(TYPE_int);
            co.push(0);
            co.store_local(localParameterIndexOffset);
        }

        ParameterMappingGenerator pmg = new ParameterMappingGenerator(co, localPreparedStatement, localObjects,
                collectionParameterInfos, localParameterIndexOffset);
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

        co.load_local(localPreparedStatement);
        co.invoke_interface(TYPE_PreparedStatement, SIG_addBatch);

        // if (i >= batchSize) {
        // ps.executeBatch();
        // i = 0;
        // }
        co.load_local(localCounter);
        co.load_this();
        co.getfield(FIELD_NAME_BATCH_SIZE);
        Label labelAfter = co.make_label();
        co.if_icmp(CodeEmitter.LT, labelAfter);

        co.load_local(localPreparedStatement);
        co.invoke_interface(TYPE_PreparedStatement, SIG_executeBatch);
        if (returnType.isArray()) {
            co.store_local(localPartResult);
            // System.arraycopy(partResult, 0, result, index, partResult.length);
            co.load_local(localPartResult);
            co.push(0);
            co.load_local(localResult);
            co.load_local(localIndex);
            co.load_local(localPartResult);
            co.arraylength();
            co.invoke_static(TYPE_System, SIG_arraycopy);

            // index += partResult.length;
            co.load_local(localIndex);
            co.load_local(localPartResult);
            co.arraylength();
            co.math(CodeEmitter.ADD, TYPE_int);
            co.store_local(localIndex);

        } else {
            co.pop();
        }
        co.push(0);
        co.store_local(localCounter);

        co.mark(labelAfter);

        co.goTo(labelBeginWhile);

        co.mark(labelNoBatching);

        if (returnType.isArray()) {
            // result[index++] = ps.executeUpdate();
            co.load_local(localResult);
            co.load_local(localIndex);
            co.iinc(localIndex, 1);
            co.load_local(localPreparedStatement);
            co.invoke_interface(TYPE_PreparedStatement, SIG_executeUpdate);
            co.array_store(TYPE_int);
        } else {
            co.load_local(localPreparedStatement);
            co.invoke_interface(TYPE_PreparedStatement, SIG_executeUpdate);
            co.pop();
        }

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

        co.load_local(localPreparedStatement);
        co.invoke_interface(TYPE_PreparedStatement, SIG_executeBatch);
        if (returnType.isArray()) {
            co.store_local(localPartResult);
            // System.arraycopy(partResult, 0, result, index, partResult.length);
            co.load_local(localPartResult);
            co.push(0);
            co.load_local(localResult);
            co.load_local(localIndex);
            co.load_local(localPartResult);
            co.arraylength();
            co.invoke_static(TYPE_System, SIG_arraycopy);
        } else {
            co.pop();
        }

        co.mark(labelAfter2);

        // finally
        tryBlockStatement.end();
        EmitUtils.emitClose(co, localPreparedStatement);

        tryBlockConnection.end();
        EmitUtils.emitUngetConnection(co, generator, localConnection);

        // return result
        if (returnType.isArray()) {
            co.load_local(localResult);
        }
        co.return_value();
        // }

        // exception handler
        EmitUtils.emitCatchException(co, tryBlockStatement, null);
        Block tryBlockStatement2 = co.begin_block();
        co.store_local(localException);
        EmitUtils.emitClose(co, localPreparedStatement);
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
