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
package uk.co.brunella.qof.parser;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Default implementation of a parameter definition.
 */
public class ParameterDefinitionImpl implements ParameterDefinition {

    private String[] names;
    private String[] fields;
    private int[] indexes;
    private String type;
    private int parameter;
    private String parameterName;
    private String partialDefinitionGroup;
    private String parameterSeparator;
    private int partialDefinitionPart;
    private int startPosition;
    private int endPosition;

    public String[] getNames() {
        return names;
    }

    void setNames(String[] names) {
        if (names == null || names.length == 0) {
            this.names = null;
        } else {
            this.names = names;
        }
    }

    public String[] getFields() {
        return fields;
    }

    void setFields(String[] fields) {
        this.fields = fields;
    }

    public int[] getIndexes() {
        return indexes;
    }

    public void setIndexes(int[] indexes) {
        if (indexes == null || indexes.length == 0) {
            this.indexes = null;
        } else {
            this.indexes = indexes;
        }
    }

    public int getParameter() {
        return parameter;
    }

    public void setParameter(int parameter) {
        this.parameter = parameter;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || "".equals(type)) {
            this.type = "auto";
        } else {
            this.type = type;
        }
    }

    private String getNamesString() {
        return names == null ? "" : Arrays.stream(names).map(n -> "\"" + n + "\"").collect(Collectors.joining(","));
    }

    private String getIndexesString() {
        return indexes == null ? "" : Arrays.stream(indexes).mapToObj(Integer::toString).collect(Collectors.joining(","));
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
