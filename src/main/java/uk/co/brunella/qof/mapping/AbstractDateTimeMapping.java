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
package uk.co.brunella.qof.mapping;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractDateTimeMapping extends AbstractBaseMapping implements Mapping, ParameterMapping,
        ResultMapping {

    private static final Set<Class<?>> types = new HashSet<>();

    static {
        types.add(Date.class);
    }

    public static Set<Class<?>> getTypes() {
        return types;
    }

    public void accept(Mapper mapper, MappingVisitor visitor) {
        visitor.visit(mapper, this);
    }

    public abstract void accept(Mapper mapper, DateTimeMappingVisitor visitor);

    public static class DateMapping extends AbstractDateTimeMapping {
        public void accept(Mapper mapper, DateTimeMappingVisitor visitor) {
            visitor.visit(mapper, this);
        }
    }

    public static class TimeMapping extends AbstractDateTimeMapping {
        public void accept(Mapper mapper, DateTimeMappingVisitor visitor) {
            visitor.visit(mapper, this);
        }
    }

    public static class TimestampMapping extends AbstractDateTimeMapping {
        public void accept(Mapper mapper, DateTimeMappingVisitor visitor) {
            visitor.visit(mapper, this);
        }
    }
}
