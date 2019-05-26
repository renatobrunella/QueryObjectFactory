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

import java.util.HashSet;
import java.util.Set;

/**
 * BooleanAdapter is a generator mapping adapter for boolean data types.
 *
 * <p>It maps <code>VARCHAR</code> columns to <code>boolean</code> and <code>Boolean</code> and vice versa.</p>
 *
 * <p>Examples:</p>
 * <pre>
 *   (1) BooleanAdapter.register("yesno", "Y", "N", false, true);
 *
 *   (2) BooleanAdapter.register("true-false", "true", "false", true, false);
 *
 *   (3) BooleanAdapter.register("bigX", "X", null, true, true);
 * </pre>
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
    private static Set<Class<?>> typeSet;

    static {
        typeSet = new HashSet<Class<?>>();
        typeSet.add(boolean.class);
        typeSet.add(Boolean.class);
    }

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

    /**
     * Registers a yes/no adapter with name "yesno".
     * <p>
     * It maps "Y" to true, "N" to false, is not case-sensitive and allows null values.
     */
    public static void registerYesNo() {
        QueryObjectFactoryDelegator.registerMapper_(null, "yesno", new BooleanAdapter("yesno", "Y", "N", false, true));
    }

    /**
     * Registers a <code>BooleanAdapter</code> with a given name.
     *
     * @param typeName      the type name to be used in parameter and result definitions
     * @param trueString    a string representing the true value
     * @param falseString   a string representing the false value. Can be null
     * @param caseSensitive true if the mapping is case-sensitive
     * @param allowNull     true if null values are allowed
     */
    public static void register(String typeName, String trueString, String falseString, boolean caseSensitive, boolean allowNull) {
        ClassLoader classLoader = CallStackIntrospector.getCaller().getClassLoader();
        QueryObjectFactoryDelegator.registerMapper_(classLoader, typeName, new BooleanAdapter(typeName, trueString, falseString, caseSensitive, allowNull));
    }

    public void generateFromResult(ResultMapping resultMapping, CodeEmitter co, Local result, int[] indexes) {
        co.load_local(result);
        co.push(indexes[0]);
        co.invoke_interface(result.getType(), Constants.SIG_getString);
        emitProcessResult(resultMapping, co);
    }

    public void generateFromResultSet(ResultMapping resultMapping, CodeEmitter co, Local resultSet, String[] columns) {
        co.load_local(resultSet);
        co.push(columns[0]);
        co.invoke_interface(Constants.TYPE_ResultSet, Constants.SIG_getStringNamed);
        emitProcessResult(resultMapping, co);
    }

    private void emitProcessResult(ResultMapping resultMapping, CodeEmitter co) {
        Label labelNull = co.make_label();
        Label labelNotFalse = co.make_label();
        Label labelEnd = co.make_label();
        Local localString = co.make_local(Constants.TYPE_String);
        Local localTrueFalse = co.make_local(Constants.TYPE_boolean);
        Local localResult = null;
        if (resultMapping.getType() == Boolean.class) {
            localResult = co.make_local(Constants.TYPE_Boolean);
        }
        co.store_local(localString);
        co.load_local(localString);
        co.ifnull(labelNull);
        // not null
        co.load_local(localString);
        if (!caseSensitive) {
            co.invoke_virtual(Constants.TYPE_String, SIG_toUpperCase);
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
                co.invoke_virtual(Constants.TYPE_String, SIG_toUpperCase);
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
            co.throw_exception(Constants.TYPE_SQLException, "invalid value for mapper \"" + typeName + "\"");
            co.mark(labelNotFalse);
        }
        if (resultMapping.getType() == Boolean.class) {
            co.load_local(localTrueFalse);
            co.invoke_static(Constants.TYPE_Boolean, Constants.SIG_Boolean_valueOf);
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
                co.throw_exception(Constants.TYPE_SQLException, "null value not allowed for mapper \"" + typeName + "\"");
            }
        } else {
            if (resultMapping.getType() == Boolean.class) {
                co.push(false);
                co.invoke_static(Constants.TYPE_Boolean, Constants.SIG_Boolean_valueOf);
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
            Local localBoolean = co.make_local(Constants.TYPE_Boolean);
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
                    co.math(CodeEmitter.ADD, Constants.TYPE_int);
                }
                co.load_local(localBoolean);
                co.invoke_virtual(Constants.TYPE_Boolean, Constants.SIG_booleanValue);
                co.if_jump(CodeEmitter.EQ, labelFalse);
                // true
                co.push(trueString);
                co.goTo(labelTrueFalseEnd);
                //false
                co.mark(labelFalse);
                co.push(falseString);
                co.mark(labelTrueFalseEnd);
                co.invoke_interface(Constants.TYPE_PreparedStatement, Constants.SIG_setString);
                co.goTo(labelEnd);
            } else {
                co.load_local(localBoolean);
                co.invoke_virtual(Constants.TYPE_Boolean, Constants.SIG_booleanValue);
                co.if_jump(CodeEmitter.EQ, labelFalse);
                // true
                co.load_local(preparedStatement);
                co.push(indexes[0]);
                if (indexOffset != null) {
                    co.load_local(indexOffset);
                    co.math(CodeEmitter.ADD, Constants.TYPE_int);
                }
                co.push(trueString);
                co.invoke_interface(Constants.TYPE_PreparedStatement, Constants.SIG_setString);
                co.goTo(labelTrueFalseEnd);
                //false
                co.mark(labelFalse);
                co.load_local(preparedStatement);
                co.push(indexes[0]);
                if (indexOffset != null) {
                    co.load_local(indexOffset);
                    co.math(CodeEmitter.ADD, Constants.TYPE_int);
                }
                co.push(java.sql.Types.VARCHAR);
                co.invoke_interface(Constants.TYPE_PreparedStatement, Constants.SIG_setNull);

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
                    co.math(CodeEmitter.ADD, Constants.TYPE_int);
                }
                co.push(java.sql.Types.VARCHAR);
                co.invoke_interface(Constants.TYPE_PreparedStatement, Constants.SIG_setNull);
            } else {
                co.throw_exception(Constants.TYPE_SQLException, "null value not allowed for mapper \"" + typeName + "\"");
            }
            co.mark(labelEnd);
        } else {
            // boolean type
            Local localBoolean = co.make_local(Constants.TYPE_boolean);
            co.store_local(localBoolean);
            if (falseString != null) {
                co.load_local(preparedStatement);
                co.push(indexes[0]);
                if (indexOffset != null) {
                    co.load_local(indexOffset);
                    co.math(CodeEmitter.ADD, Constants.TYPE_int);
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
                co.invoke_interface(Constants.TYPE_PreparedStatement, Constants.SIG_setString);
            } else {
                co.load_local(localBoolean);
                co.if_jump(CodeEmitter.EQ, labelFalse);
                // true
                co.load_local(preparedStatement);
                co.push(indexes[0]);
                if (indexOffset != null) {
                    co.load_local(indexOffset);
                    co.math(CodeEmitter.ADD, Constants.TYPE_int);
                }
                co.push(trueString);
                co.invoke_interface(Constants.TYPE_PreparedStatement, Constants.SIG_setString);
                co.goTo(labelTrueFalseEnd);
                //false
                co.mark(labelFalse);
                co.load_local(preparedStatement);
                co.push(indexes[0]);
                if (indexOffset != null) {
                    co.load_local(indexOffset);
                    co.math(CodeEmitter.ADD, Constants.TYPE_int);
                }
                co.push(java.sql.Types.VARCHAR);
                co.invoke_interface(Constants.TYPE_PreparedStatement, Constants.SIG_setNull);
                co.mark(labelTrueFalseEnd);
            }
        }
    }

    public void generateRegisterOutputParameters(ResultMapping resultMapping, CodeEmitter co, Local callableStatement,
                                                 int[] indexes) {
        co.load_local(callableStatement);
        co.push(indexes[0]);
        co.push(java.sql.Types.VARCHAR);
        co.invoke_interface(Constants.TYPE_CallableStatement, Constants.SIG_registerOutParameter);
    }

    public int getNumberOfColumns() {
        return 1;
    }

    public Set<Class<?>> getTypes() {
        return typeSet;
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
