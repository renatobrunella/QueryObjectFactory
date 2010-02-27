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
package sf.qof.mapping;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractNumberMapping extends AbstractBaseMapping implements Mapping, ParameterMapping,
    ResultMapping {

  public void accept(Mapper mapper, MappingVisitor visitor) {
    visitor.visit(mapper, this);
  }

  public abstract void accept(Mapper mapper, NumberMappingVisitor visitor);

  public static class ByteMapping extends AbstractNumberMapping {
    private static final Set<Class<?>> types = new HashSet<Class<?>>();
    static {
      types.add(Byte.TYPE);
      types.add(Byte.class);
    }

    public static Set<Class<?>> getTypes() {
      return types;
    }

    public void accept(Mapper mapper, NumberMappingVisitor visitor) {
      visitor.visit(mapper, this);
    }
  }

  public static class ShortMapping extends AbstractNumberMapping {
    private static final Set<Class<?>> types = new HashSet<Class<?>>();
    static {
      types.add(Short.TYPE);
      types.add(Short.class);
    }

    public static Set<Class<?>> getTypes() {
      return types;
    }

    public void accept(Mapper mapper, NumberMappingVisitor visitor) {
      visitor.visit(mapper, this);
    }
  }

  public static class IntegerMapping extends AbstractNumberMapping {
    private static final Set<Class<?>> types = new HashSet<Class<?>>();
    static {
      types.add(Integer.TYPE);
      types.add(Integer.class);
    }

    public static Set<Class<?>> getTypes() {
      return types;
    }

    public void accept(Mapper mapper, NumberMappingVisitor visitor) {
      visitor.visit(mapper, this);
    }
  }

  public static class LongMapping extends AbstractNumberMapping {
    private static final Set<Class<?>> types = new HashSet<Class<?>>();
    static {
      types.add(Long.TYPE);
      types.add(Long.class);
    }

    public static Set<Class<?>> getTypes() {
      return types;
    }

    public void accept(Mapper mapper, NumberMappingVisitor visitor) {
      visitor.visit(mapper, this);
    }
  }

  public static class FloatMapping extends AbstractNumberMapping {
    private static final Set<Class<?>> types = new HashSet<Class<?>>();
    static {
      types.add(Float.TYPE);
      types.add(Float.class);
    }

    public static Set<Class<?>> getTypes() {
      return types;
    }

    public void accept(Mapper mapper, NumberMappingVisitor visitor) {
      visitor.visit(mapper, this);
    }
  }

  public static class DoubleMapping extends AbstractNumberMapping {
    private static final Set<Class<?>> types = new HashSet<Class<?>>();
    static {
      types.add(Double.TYPE);
      types.add(Double.class);
    }

    public static Set<Class<?>> getTypes() {
      return types;
    }

    public void accept(Mapper mapper, NumberMappingVisitor visitor) {
      visitor.visit(mapper, this);
    }
  }

  public static class BooleanMapping extends AbstractNumberMapping {
    private static final Set<Class<?>> types = new HashSet<Class<?>>();
    static {
      types.add(Boolean.TYPE);
      types.add(Boolean.class);
    }

    public static Set<Class<?>> getTypes() {
      return types;
    }

    public void accept(Mapper mapper, NumberMappingVisitor visitor) {
      visitor.visit(mapper, this);
    }
  }
}
