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
package sf.qof.mapping;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class Mapper {

  private MethodInfo methodInfo;
  private QueryType queryType;
  private String sql;
  private List<ParameterMapping> parameters;
  private List<ResultMapping> results;

  public Mapper(MethodInfo methodInfo, QueryType type, String sql, List<ParameterMapping> parameters,
      List<ResultMapping> results) {
    this.methodInfo = methodInfo;
    this.queryType = type;
    this.sql = sql;
    this.parameters = parameters;
    this.results = results;
  }

  public MethodInfo getMethod() {
    return methodInfo;
  }

  public String getSql() {
    return sql;
  }

  public QueryType getQueryType() {
    return queryType;
  }

  public List<ParameterMapping> getParameters() {
    return parameters;
  }

  public List<ResultMapping> getResults() {
    return results;
  }

  public int getNumberOfConstructorParameters() {
    int num = 0;
    for (ResultMapping mapping : results) {
      if (mapping.getConstructorParameter() != null) {
        num++;
      }
    }
    return num;
  }

  public Constructor<?> getConstructor() {
    for (ResultMapping mapping : results) {
      if (mapping.getConstructor() != null) {
        return mapping.getConstructor();
      }
    }
    return null;
  }
  
  public Method getStaticFactoryMethod() {
    for (ResultMapping mapping : results) {
      if (mapping.getStaticFactoryMethod() != null) {
        return mapping.getStaticFactoryMethod();
      }
    }
    return null;
  }

  public void acceptParameterMappers(MappingVisitor visitor) {
    for (Mapping mapping : parameters) {
      mapping.accept(this, visitor);
    }
  }

  public void acceptResultMappers(MappingVisitor visitor) {
    for (Mapping mapping : results) {
      mapping.accept(this, visitor);
    }
  }

  private void println(OutputStream out, String s) {
    try {
      out.write(s.getBytes());
      out.write('\n');
    } catch (IOException e) {
    }
  }

  public void printMappingInfo(OutputStream out) {
    println(out, queryType.toString());
    println(out, methodInfo.toString());
    println(out, sql);
    if (parameters != null) {
      for (ParameterMapping mapping : parameters) {
        println(out, mapping.parameterMappingInfo());
      }
    }
    if (results != null) {
      for (ResultMapping mapping : results) {
        println(out, mapping.resultMappingInfo());
      }
    }
    println(out, "");
  }

  public int getMaxParameterSqlIndex() {
    int maxIndex = 0;
    if (parameters != null) {
      for (ParameterMapping mapping : parameters) {
        for (int index : mapping.getSqlIndexes()) {
          if (index > maxIndex) {
            maxIndex = index;
          }
        }
      }
    }
    return maxIndex;
  }

  public boolean usesArray() {
    if (parameters != null) {
      for (ParameterMapping mapping : parameters) {
        if (mapping.usesArray()) {
          return true;
        }
      }
    }
    return false;
  }
}
