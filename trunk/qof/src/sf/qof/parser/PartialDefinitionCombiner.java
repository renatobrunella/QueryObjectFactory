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
package sf.qof.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sf.qof.exception.ValidationException;

/**
 * Helper class to combine partial definitions.
 */
public class PartialDefinitionCombiner {

  /**
   * Combines partial definitions.
   * 
   * @param definitionList  a list of <code>Definition</code> objects. May contain partial definitions
   *                        that will be combined
   * @return                a list of containing full definitions
   * @throws sq.qof.exception.ValidationException if duplicate partial definitions are found
   */
  public static List<? extends Definition> combine(List<? extends Definition> definitionList) {
    List<Definition> partialDefinitions = new ArrayList<Definition>();
    List<Definition> fullDefinitions = new ArrayList<Definition>();
    
    for (Definition definition : definitionList) {
      if (definition.isPartialDefinition()) {
        partialDefinitions.add(definition);
      } else {
        fullDefinitions.add(definition);
      }
    }
    
    if (partialDefinitions.size() == 0) { 
      return definitionList;
    }
    
    // sort the list
    Collections.sort(partialDefinitions, new DefinitionComparator());
    
    validatePartialDefinitions(partialDefinitions);
    
    int index = 0;
    while (index < partialDefinitions.size()) {
      Definition partialDefinition = partialDefinitions.get(index++);
      if (partialDefinition.getPartialDefinitionPart() == 1) {
        int numberOfParts = numberOfParts(partialDefinitions, index - 1);
        if (partialDefinition instanceof ResultDefinition) {
          ResultDefinition partialResultDefiniton = (ResultDefinition)partialDefinition;
          ResultDefinitionImpl resultDefiniton = new ResultDefinitionImpl();
          resultDefiniton.setType(partialResultDefiniton.getType());
          resultDefiniton.setField(partialResultDefiniton.getField());
          resultDefiniton.setConstructorParameter(partialResultDefiniton.getConstructorParameter());
          resultDefiniton.setIsMapKey(partialResultDefiniton.isMapKey());
          if (partialResultDefiniton.getColumns() != null) {
            String[] columns = new String[numberOfParts];
            resultDefiniton.setColumns(columns);
            columns[0] = partialResultDefiniton.getColumns()[0];
            for (int i = 1; i < numberOfParts; i++) {
              columns[i] = ((ResultDefinition)partialDefinitions.get(index++)).getColumns()[0];
            }
          } else {
            int[] indexes = new int[numberOfParts];
            resultDefiniton.setIndexes(indexes);
            indexes[0] = partialResultDefiniton.getIndexes()[0];
            for (int i = 1; i < numberOfParts; i++) {
              indexes[i] = ((ResultDefinition)partialDefinitions.get(index++)).getIndexes()[0];
            }
          }
          fullDefinitions.add(resultDefiniton);
        } else {
          ParameterDefinition partialParameterDefiniton = (ParameterDefinition)partialDefinition;
          ParameterDefinitionImpl parameterDefiniton = new ParameterDefinitionImpl();
          parameterDefiniton.setType(partialParameterDefiniton.getType());
          parameterDefiniton.setField(partialParameterDefiniton.getField());
          parameterDefiniton.setNames(partialParameterDefiniton.getNames());
          parameterDefiniton.setParameter(partialParameterDefiniton.getParameter());
          int[] indexes = new int[numberOfParts];
          parameterDefiniton.setIndexes(indexes);
          indexes[0] = partialParameterDefiniton.getIndexes()[0];
          for (int i = 1; i < numberOfParts; i++) {
            indexes[i] = ((ParameterDefinition)partialDefinitions.get(index++)).getIndexes()[0];
          }
          fullDefinitions.add(parameterDefiniton);
        }
      }
    }
    
    return fullDefinitions;
  }

  private static int numberOfParts(List<Definition> list, int start) {
    String type = makeEmptyIfNull(list.get(start).getType());
    String group = makeEmptyIfNull(list.get(start).getPartialDefinitionGroup());
    int i = 1;
    while (i + start < list.size()) {
      if (!type.equals(makeEmptyIfNull(list.get(start + i).getType())) || 
          !group.equals(makeEmptyIfNull(list.get(start + i).getPartialDefinitionGroup()))) {
        break;
      }
      i++;
    }
    return i;
  }

  private static String makeEmptyIfNull(String string) {
    return string == null ? "" : string;
  }
  
  private static void validatePartialDefinitions(List<Definition> partialDefinitions) {
    DefinitionComparator comparator = new DefinitionComparator();
    Definition lastDefinition = partialDefinitions.get(0);
    for (int i = 1; i < partialDefinitions.size(); i++) {
      Definition currentDefinition = partialDefinitions.get(i);
      if (comparator.compare(lastDefinition, currentDefinition) == 0 ) {
        throw new ValidationException("Duplicate partial definition");
      }
      lastDefinition = currentDefinition;
    }
  }

  private static class DefinitionComparator implements Comparator<Definition> {
    public int compare(Definition o1, Definition o2) {
      int cmp;
      String type1 = o1.getType();
      String type2 = o2.getType();
      if (type1 != null) {
        cmp = type1.compareTo(type2);
      } else if (type2 != null) {
        cmp = -type2.compareTo(type1);
      } else {
        cmp = 0;
      }
      if (cmp != 0) {
        return cmp;
      }
      String group1 = o1.getPartialDefinitionGroup();
      String group2 = o2.getPartialDefinitionGroup();
      if (group1 != null) {
        cmp = group1.compareTo(group2);
      } else if (group2 != null) {
        cmp = -group2.compareTo(group1);
      } else {
        cmp = 0;
      }
      if (cmp != 0) {
        return cmp;
      }
      return o1.getPartialDefinitionPart() - o2.getPartialDefinitionPart();
    }
  }
}