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
package sf.qof.parser;

import java.util.Arrays;

/**
 * Default implementation of a parameter definition.
 */
public class ParameterDefinitionImpl implements ParameterDefinition {

  private String[] names;
  private String[] fields;
  private int[] indexes;
  private String type;
  private int parameter;
  private String partialDefinitionGroup;
  private String parameterSeparator;
  private int partialDefinitionPart;
  private int startPosition;
  private int endPosition;

  public String[] getNames() {
    return names;
  }

  public String[] getFields() {
    return fields;
  }

  public int[] getIndexes() {
    return indexes;
  }

  public int getParameter() {
    return parameter;
  }

  public String getType() {
    return type;
  }

  public void setNames(String[] names) {
    if (names == null || names.length == 0) {
      this.names = null; //NOPMD
    } else {
      this.names = names;
    }
  }

  public void setFields(String[] fields) {
    this.fields = fields;
  }

  public void setIndexes(int[] indexes) {
    if (indexes == null || indexes.length == 0) {
      this.indexes = null; //NOPMD
    } else {
      this.indexes = indexes;
    }
  }

  public void setParameter(int parameter) {
    this.parameter = parameter;
  }

  public void setType(String type) {
    if (type == null || "".equals(type)) {
      this.type = "auto";
    } else {
      this.type = type;
    }
  }

  private String getNamesString() {
    if (names == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < names.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append('"').append(names[i]).append('"');
    }
    return sb.toString();
  }

  private String getIndexesString() {
    if (indexes == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indexes.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(indexes[i]);
    }
    return sb.toString();
  }

  public String toString() {
    return "Parameter: " + type + " " + parameter + " (" + getNamesString() + " " + getIndexesString() + ") " + Arrays.toString(fields);
  }

  public String getPartialDefinitionGroup() {
    return partialDefinitionGroup;
  }
  
  public void setPartialDefinitionGroup(String partialDefinitionGroup) {
    this.partialDefinitionGroup = partialDefinitionGroup;
  }
  
  public String getParameterSeparator() {
    return parameterSeparator;
  }

  public void setParameterSeparator(String parameterSeparator) {
    this.parameterSeparator = parameterSeparator;
  }
  
  public int getPartialDefinitionPart() {
    return partialDefinitionPart;
  }
  
  public void setPartialDefinitionPart(int partialDefinitionPart) {
    this.partialDefinitionPart = partialDefinitionPart;
  }

  public boolean isPartialDefinition() {
    return partialDefinitionPart > 0;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public void setStartPosition(int startPosition) {
    this.startPosition = startPosition;
  }

  public int getEndPosition() {
    return endPosition;
  }

  public void setEndPosition(int endPosition) {
    this.endPosition = endPosition;
  }
}
