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
package uk.co.brunella.qof.adapter;

import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import uk.co.brunella.qof.QueryObjectFactory;
import uk.co.brunella.qof.codegen.Constants;
import uk.co.brunella.qof.mapping.ParameterMapping;
import uk.co.brunella.qof.mapping.ResultMapping;
import uk.co.brunella.qof.util.CallStackIntrospector;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * BlobAdapter is a generator mapping adapter for SQL Blob data types.
 *
 * <p>It maps <code>Blob</code> columns to <code>byte[]</code> and vice versa.</p>
 *
 * @see java.sql.Blob
 */
public class BlobAdapter implements GeneratorMappingAdapter {

    private static final Type TYPE_ByteArrayInputStream = Type.getType(ByteArrayInputStream.class);
    private static final Type TYPE_Blob = Type.getType(java.sql.Blob.class);
    private static final Type TYPE_BlobReader = Type.getType(BlobReader.class);
    private static final Type TYPE_ByteArray = Type.getType("[B");
    private static final Signature SIG_setBinaryStream = new Signature("setBinaryStream", "(ILjava/io/InputStream;I)V");
    private static final Signature SIG_ConstructorByteArrayInputStream = new Signature("<init>", "([B)V");
    private static final Signature SIG_getBlob = new Signature("getBlob", "(I)Ljava/sql/Blob;");
    private static final Signature SIG_getBlobNamed = new Signature("getBlob", "(Ljava/lang/String;)Ljava/sql/Blob;");
    private static final Signature SIG_readBlob = new Signature("readBlob", "(Ljava/sql/Blob;)[B");
    private final static BlobAdapter generator = new BlobAdapter();
    private static Set<Class<?>> typeSet;

    static {
        typeSet = new HashSet<Class<?>>();
        typeSet.add(byte[].class);
    }

    /**
     * Register the mapping adapter with the default name "blob".
     */
    public static void register() {
        QueryObjectFactoryDelegator.registerMapper_(null, "blob", generator);
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

    public void generateFromResult(ResultMapping resultMapping, CodeEmitter co, Local result, int[] indexes) {
        co.load_local(result);
        co.push(indexes[0]);
        co.invoke_interface(result.getType(), SIG_getBlob);
        emitProcessResult(co);
    }

    public void generateFromResultSet(ResultMapping resultMapping, CodeEmitter co, Local resultSet, String[] columns) {
        co.load_local(resultSet);
        co.push(columns[0]);
        co.invoke_interface(Constants.TYPE_ResultSet, SIG_getBlobNamed);
        emitProcessResult(co);
    }

    private void emitProcessResult(CodeEmitter co) {
        Local localBlob = co.make_local(TYPE_Blob);
        co.store_local(localBlob);
        Label labelNull = co.make_label();
        co.load_local(localBlob);
        co.ifnull(labelNull);
        // not null
        co.load_local(localBlob);
        co.invoke_static(TYPE_BlobReader, SIG_readBlob);

        Label labelEnd = co.make_label();
        co.goTo(labelEnd);

        // null
        co.mark(labelNull);
        co.aconst_null();
        co.mark(labelEnd);
    }

    public void generateToPreparedStatement(ParameterMapping parameterMapping, CodeEmitter co, Local preparedStatement, int[] indexes, Local indexOffset) {
        Local localByteArray = co.make_local(TYPE_ByteArray);
        Label labelNull = co.make_label();
        Label labelEnd = co.make_label();
        co.store_local(localByteArray);
        co.load_local(localByteArray);
        co.ifnull(labelNull);
        // not null
        co.load_local(preparedStatement);
        co.push(indexes[0]);
        if (indexOffset != null) {
            co.load_local(indexOffset);
            co.math(CodeEmitter.ADD, Constants.TYPE_int);
        }
        co.new_instance(TYPE_ByteArrayInputStream);
        co.dup();
        co.load_local(localByteArray);
        co.invoke_constructor(TYPE_ByteArrayInputStream, SIG_ConstructorByteArrayInputStream);
        co.load_local(localByteArray);
        co.arraylength();
        co.invoke_interface(Constants.TYPE_PreparedStatement, SIG_setBinaryStream);
        co.goTo(labelEnd);

        co.mark(labelNull);
        // null
        co.load_local(preparedStatement);
        co.push(indexes[0]);
        if (indexOffset != null) {
            co.load_local(indexOffset);
            co.math(CodeEmitter.ADD, Constants.TYPE_int);
        }
        co.push(java.sql.Types.BLOB);
        co.invoke_interface(Constants.TYPE_PreparedStatement, Constants.SIG_setNull);

        co.mark(labelEnd);
    }

    public int getNumberOfColumns() {
        return 1;
    }

    public Set<Class<?>> getTypes() {
        return typeSet;
    }

    public void generateRegisterOutputParameters(ResultMapping resultMapping, CodeEmitter co, Local callableStatement,
                                                 int[] indexes) {
        co.load_local(callableStatement);
        co.push(indexes[0]);
        co.push(java.sql.Types.BLOB);
        co.invoke_interface(Constants.TYPE_CallableStatement, Constants.SIG_registerOutParameter);
    }

    /*
     * Delegator object to access protected static methods in QueryObjectFactory
     */
    private static class QueryObjectFactoryDelegator extends QueryObjectFactory {
        public static void registerMapper_(ClassLoader classLoader, String type, MappingAdapter adapter) {
            unregisterMapper(classLoader, type);
            registerMapper(classLoader, type, adapter);
        }
    }

}
