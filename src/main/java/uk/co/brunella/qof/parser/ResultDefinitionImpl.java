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
 * Default implementation of a result definition.
 */
public class ResultDefinitionImpl implements ResultDefinition {

    private String[] columns;
    private String field;
    private int[] indexes;
    private String type;
    private boolean isMapKey;
    private int constructorParameter;
    private String partialDefinitionGroup;
    private int partialDefinitionPart;
    private int startPosition;
    private int endPosition;

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        if (columns == null || columns.length == 0) {
            this.columns = null;
        } else {
            this.columns = columns;
        }
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        if (field == null || "".equals(field)) {
            this.field = null;
        } else {
            this.field = field;
        }
    }

    public int getConstructorParameter() {
        return constructorParameter;
    }

    public void setConstructorParameter(int parameter) {
        this.constructorParameter = parameter;
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

    public void setIsMapKey(boolean isMapKey) {
        this.isMapKey = isMapKey;
    }

    private String getColumnsString() {
        return columns == null ? "" : Arrays.stream(columns).map(n -> "\"" + n + "\"").collect(Collectors.joining(","));
    }

    private String getIndexesString() {
        return indexes == null ? "" : Arrays.stream(indexes).mapToObj(Integer::toString).collect(Collectors.joining(","));
    }

    public String toString() {
        return "Result: " + type + " (" + getColumnsString() + " " + getIndexesString() + ") " + field;

    }

    public boolean isMapKey() {
        return isMapKey;
    }

    public String getPartialDefinitionGroup() {
        return partialDefinitionGroup;
    }

    public void setPartialDefinitionGroup(String partialDefinitionGroup) {
        this.partialDefinitionGroup = partialDefinitionGroup;
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
