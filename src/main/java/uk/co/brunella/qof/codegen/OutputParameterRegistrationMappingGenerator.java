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

import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.Local;
import net.sf.cglib.core.Signature;
import org.objectweb.asm.Type;
import uk.co.brunella.qof.adapter.DynamicMappingAdapter;
import uk.co.brunella.qof.adapter.GeneratorMappingAdapter;
import uk.co.brunella.qof.mapping.*;

import static uk.co.brunella.qof.codegen.Constants.*;

/**
 * Internal - OutputParameterRegistrationMappingGenerator is the generator class for output parameter registration.
 */
public class OutputParameterRegistrationMappingGenerator implements MappingVisitor, NumberMappingVisitor,
        CharacterMappingVisitor, DateTimeMappingVisitor {

    private CodeEmitter co;
    private Local callableStatement;

    public OutputParameterRegistrationMappingGenerator(CodeEmitter co, Local callableStatement) {
        this.co = co;
        this.callableStatement = callableStatement;
    }

    public void visit(Mapper mapper, AbstractCharacterMapping mapping) {
        mapping.accept(mapper, (CharacterMappingVisitor) this);
    }

    public void visit(Mapper mapper, AbstractNumberMapping mapping) {
        mapping.accept(mapper, (NumberMappingVisitor) this);
    }

    public void visit(Mapper mapper, AbstractDateTimeMapping mapping) {
        mapping.accept(mapper, (DateTimeMappingVisitor) this);
    }

    public void visit(Mapper mapper, AdapterMapping mapping) {
        int[] sqlIndexes = mapping.getSqlIndexes();

        if (mapping.getAdapter() instanceof GeneratorMappingAdapter) {
            ((GeneratorMappingAdapter) mapping.getAdapter()).generateRegisterOutputParameters(mapping, co, callableStatement, sqlIndexes);
        } else if (mapping.getAdapter() instanceof DynamicMappingAdapter) {
            co.getfield(QueryObjectGenerator.getAdapterFieldName(mapping.getAdapter().getClass()));
            co.load_local(callableStatement);
            // push indexes
            co.push(sqlIndexes.length);
            co.newarray(TYPE_int);
            for (int i = 0; i < sqlIndexes.length; i++) {
                co.dup();
                co.push(i);
                co.push(sqlIndexes[i]);
                co.array_store(TYPE_int);
            }
            co.invoke_interface(Type.getType(DynamicMappingAdapter.class), new Signature("registerOutputParameter",
                    "(Ljava/sql/CallableStatement;[I)V"));
        } else {
            throw new RuntimeException("Unsupported adapter type " + mapping.getAdapter());
        }
    }

    // NumberMappingVisitor

    public void visit(Mapper mapper, AbstractNumberMapping.ByteMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.TINYINT);
    }

    public void visit(Mapper mapper, AbstractNumberMapping.ShortMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.SMALLINT);
    }

    public void visit(Mapper mapper, AbstractNumberMapping.IntegerMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.INTEGER);
    }

    public void visit(Mapper mapper, AbstractNumberMapping.LongMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.BIGINT);
    }

    public void visit(Mapper mapper, AbstractNumberMapping.FloatMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.REAL);
    }

    public void visit(Mapper mapper, AbstractNumberMapping.DoubleMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.DOUBLE);
    }

    public void visit(Mapper mapper, AbstractNumberMapping.BooleanMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.BOOLEAN);
    }

    // CharacterMappingVisitor

    public void visit(Mapper mapper, AbstractCharacterMapping.StringMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.VARCHAR);
    }

    public void visit(Mapper mapper, AbstractCharacterMapping.CharacterMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.VARCHAR);
    }

    // DateTimeMappingVisitor

    public void visit(Mapper mapper, AbstractDateTimeMapping.DateMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.DATE);
    }

    public void visit(Mapper mapper, AbstractDateTimeMapping.TimeMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.TIME);
    }

    public void visit(Mapper mapper, AbstractDateTimeMapping.TimestampMapping mapping) {
        emitRegisterOutputParameter(mapping, java.sql.Types.TIMESTAMP);
    }

    private void emitRegisterOutputParameter(ResultMapping resultMapping, int sqlType) {
        co.load_local(callableStatement);
        co.push(resultMapping.getSqlIndexes()[0]);
        co.push(sqlType);
        co.invoke_interface(TYPE_CallableStatement, SIG_registerOutParameter);
    }
}
