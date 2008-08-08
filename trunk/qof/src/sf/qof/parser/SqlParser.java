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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sf.qof.exception.SqlParserException;


/**
 * A parser to extract parameter and result definitions. 
 *
 * <p> The <code>SqlParser</code> is used to extract parameter and result defintions embedded
 * in the SQL statement.
 * 
 * <p> Parameter definitions have the following form:
 * <p> <code>{%#}</code> or <code>{type %#}</code>
 * or <code>{%#.field}</code> or <code>{type %#.field}</code>
 * <p> <code>#</code> is the index of the Java parameter (1..n) 
 * <br> <code>field</code> is the name of the field in the mapped Java Bean object 
 * <br> <code>type</code> is the optional name of mapper (<code>int</code>, <code>string</code>, etc)  
 * 
 * <p> Result definitions have the following form:
 * <p> <code>{%%}</code> or <code>{type %%}</code>
 * or <code>{%%.field}</code> or <code>{type %%.field}</code> or <code>{%%*}</code>
 * <p> <code>field</code> is the name of the field in the mapped Java Bean object 
 * <br> <code>type</code> is the optional name of mapper (<code>int</code>, <code>string</code>, etc)
 * <br> <code>{%%*}</code> denotes the map key if the return result type is <code>Map</code>
 *
 * <p> In/out definitions for stored procedure calls have the following form:
 * <p> <code>{result definition,parameter definition}</code> or <code>{parameter definition,result definition}</code>
 * <p> i.e. <code>{%%,%1}</code>, <code>{int %%.id,int %2}</code> 
 *
 * <p>For partial definitions add <code>@#</code> at the end of the definition where <code>#</code> is the part number. 
 * Partial definitions are used for custom mapping adapters when more than one column or parameter maps to the 
 * same Java object. If more than one definition with the same type are in the same SQL statement then a group
 * name in brackets is required at the end of the definition <code>[group]</code>. 
 *
 * <p> Examples:
 * <p> <blockquote><pre>
 * select id {int %%.id,%%*}, name {string %%.name} from person where id = {int %1}
 * 
 * insert into person (id, name) values ({%1}, {%2})
 * 
 * update person set name = {%2} where id = {%1}
 * 
 * delete from person where id = {%1}
 * 
 * { %% = call numberOfPersons({%1}) }
 * 
 * { call inout({%1,%%}) }
 * 
 * insert into person (id, first_name, last_name) values ({%1}, {name%2@1}, {name%2@2})
 * 
 * select width {measurement%%.width@1[width]}, unit_of_measurement_width {measurement%%.width@2[width]}, 
 *   height {measurement%%.height@1[height]}, unit_of_measurement_height {measurement%%.height@2[height]}, 
 *   length {measurement%%.length@1[length]}, unit_of_measurement_length {measurement%%.length@2[length]} 
 * from product 
 * </pre></blockquote>
 */
public class SqlParser {

  private List<ParameterDefinition> parameterDefs;
  private List<ResultDefinition> resultDefs;
  private List<Integer> openCurlyBrackets;
  private List<Integer> closeCurlyBrackets;
  private int sqlLength;
  private String sql;
  private int sqlIndex = 0;

  /**
   * Instantiate and parse a SQL statement.
   * 
   * @param sql                 a SQL statement
   * @param isCallableStatement true if the SQL statement is a store procedure call
   * 
   * @throws sf.qof.exception.SqlParserException
   */
  public SqlParser(String sql, boolean isCallableStatement) {
    parameterDefs = new ArrayList<ParameterDefinition>();
    resultDefs = new ArrayList<ResultDefinition>();
    openCurlyBrackets = new ArrayList<Integer>();
    closeCurlyBrackets = new ArrayList<Integer>();
    sqlLength = sql.length();

    if (sql.indexOf('{') >= 0) {
      this.sql = parse(sql, isCallableStatement);
    } else {
      this.sql = sql;
    }
  }

  /**
   * Returns the extracted parameter definitions.
   * 
   * @return extracted parameter definitions
   */
  public ParameterDefinition[] getParameterDefinitions() {
    return parameterDefs.toArray(new ParameterDefinition[parameterDefs.size()]);
  }

  /**
   * Returns the extracted result definitions.
   * 
   * @return extracted result definitions
   */
  public ResultDefinition[] getResultDefinitions() {
    return resultDefs.toArray(new ResultDefinition[resultDefs.size()]);
  }

  /**
   * Returns the parsed SQL statement without parameter and result definitions.
   * 
   * @return parsed SQL statement without parameter and result definitions
   */
  public String getSql() {
    return sql;
  }

//  @SuppressWarnings("unchecked")
  private String parse(String sql, boolean isCallableStatement) {
    findCurlyBrackets(sql);
    int curlyBracketIndex = 0;

    sql = sql.trim();
    boolean callableInCurlyBrackets = false;
    if (isCallableStatement && sql.charAt(0) == '{' && sql.charAt(sql.length() - 1) == '}') {
      callableInCurlyBrackets = true;
      sql = sql.substring(1, sql.length() - 1);
      openCurlyBrackets.remove(0);
      closeCurlyBrackets.remove(closeCurlyBrackets.size() - 1);
    }
    
    List<String> saveStrings = new ArrayList<String>();
    sql = extractStrings(sql, saveStrings);

    String[] tokenList = sql.split("\\s+");

    String preToken = "";
    StringBuffer sbSql = new StringBuffer();
    sqlIndex = 1;
    for (String token : tokenList) {
      if (token.length() == 0) {
        sbSql.append(' ');
        continue;
      }
      // increment sqlIndex for each '?'
      for (int i = 0; i < token.length(); i++) {
        if (token.charAt(i) == '?') {
          sqlIndex++;
        }
      }
      if (token.charAt(0) == '{') {
        int indexComma = token.indexOf(',');
        if (indexComma > 0) {
          String leftToken = token.substring(0, indexComma) + "}";
          String rightToken = "{" + token.substring(indexComma + 1);
          if (isCallableStatement) {
            if (leftToken.contains("%%") && !rightToken.contains("%%") || 
                !leftToken.contains("%%") && rightToken.contains("%%")) {
              parseDefinition(leftToken, preToken, sbSql, isCallableStatement, false, curlyBracketIndex);
              parseDefinition(rightToken, preToken, sbSql, isCallableStatement, true, curlyBracketIndex);
            } else {
              int start = openCurlyBrackets.get(curlyBracketIndex);
              int end = curlyBracketIndex < closeCurlyBrackets.size() ? closeCurlyBrackets.get(curlyBracketIndex) : sqlLength - 1;
              throw new SqlParserException("Only one parameter and result definition are allowed: " + token, start, end - start + 1);
            }
          } else {
            if (leftToken.contains("%%*")) {
              parseDefinition(rightToken, preToken, sbSql, isCallableStatement, true, curlyBracketIndex);
              parseDefinition(leftToken, preToken, sbSql, isCallableStatement, false, curlyBracketIndex);
            } else if (rightToken.contains("%%*")) {
              parseDefinition(leftToken, preToken, sbSql, isCallableStatement, true, curlyBracketIndex);
              parseDefinition(rightToken, preToken, sbSql, isCallableStatement, false, curlyBracketIndex);
            } else {
              int start = openCurlyBrackets.get(curlyBracketIndex);
              int end = curlyBracketIndex < closeCurlyBrackets.size() ? closeCurlyBrackets.get(curlyBracketIndex) : sqlLength - 1;
              throw new SqlParserException("One of the definitions must be a map key definition: " + token, start, end - start + 1);
            }
          }
        } else {
          parseDefinition(token, preToken, sbSql, isCallableStatement, true, curlyBracketIndex);
        }
        curlyBracketIndex++;
      } else {
        sbSql.append(token).append(' ');
      }
      if (token.length() > 0 && !(token.charAt(0) == '{')) {
        preToken = token;
      }
    }
    sql = mergeStrings(sbSql.toString(), saveStrings);

    if (callableInCurlyBrackets) {
      sql = "{ " + sql + " }";
    }
    @SuppressWarnings("unchecked") List<ResultDefinition> combinedResults = 
      (List<ResultDefinition>)PartialDefinitionCombiner.combine(resultDefs);
    resultDefs = combinedResults;
    
    @SuppressWarnings("unchecked") List<ParameterDefinition> combinedParameters = 
      (List<ParameterDefinition>)PartialDefinitionCombiner.combine(parameterDefs);
    parameterDefs = combinedParameters;
    
    if (openCurlyBrackets.size() != closeCurlyBrackets.size()) {
      throw new SqlParserException("Number of opening and closing curly brackets does not match", 0, sqlLength);
    }
    
    return sql;
  }


  private void parseDefinition(String token, String preToken, StringBuffer sbSql, boolean isCallableStatement,
      boolean insertQuestionMark, int curlyBracketIndex) {
    int index = token.indexOf('%');
    if (index + 1 < token.length() && token.charAt(index + 1) == '%') {
      // result definition
      if (!isCallableStatement) {
        // extract the column name from the previous token
        int wordIndex = preToken.length() - 1;
        while (wordIndex >= 0) {
          char c = preToken.charAt(wordIndex);
          if (!Character.isLetterOrDigit(c) && c != '_') {
            break;
          }
          wordIndex--;
        }
        String column = preToken.substring(wordIndex + 1);
        resultDefs.add(parseResultDefinition(token, column, 0, curlyBracketIndex));
      } else {
        resultDefs.add(parseResultDefinition(token, null, sqlIndex, curlyBracketIndex));
      }
      if (insertQuestionMark && isCallableStatement) {
        sqlIndex++;
        sbSql.append("? ");
      }
    } else {
      // parameter definition
      parameterDefs.add(parseParameterDefinition(token, curlyBracketIndex));
      if (insertQuestionMark) {
        sqlIndex++;
        sbSql.append("? ");
      }
    }
  }

//  private static final Pattern RESULT_DEF_PATTERN = Pattern.compile("\\{([\\w-_]+)?%%((\\d+)|\\*|\\.(\\w+))?\\}");
  private static final Pattern RESULT_DEF_PATTERN = 
    Pattern.compile("\\{([\\w\\-]+)?%%((\\d+)|\\*|\\.(\\w+))?(@\\d+)?(\\[[\\w]+\\])?\\}");

  private ResultDefinition parseResultDefinition(String definition, String columnName, int sqlIndex, int curlyBracketIndex) {
    String mappingType = null;
    String field = null;
    int constructorParameter = 0;
    boolean isMapKey = false;
    int partialDefinitionPart = 0;
    String partialDefinitionGroup = null;

    Matcher matcher = RESULT_DEF_PATTERN.matcher(definition);
    if (matcher.find() && matcher.group().equals(definition)) {
      mappingType = matcher.group(1);
      isMapKey = "*".equals(matcher.group(2));
      if (matcher.group(3) != null) {
    	constructorParameter = Integer.valueOf(matcher.group(3));
      }
      field = matcher.group(4);
      if (matcher.group(5) != null) {
        partialDefinitionPart = Integer.valueOf(matcher.group(5).substring(1));
      }
      partialDefinitionGroup = matcher.group(6);

      ResultDefinitionImpl resultDef = new ResultDefinitionImpl();
      resultDef.setType(mappingType);
      if (columnName != null) {
        resultDef.setColumns(new String[] { columnName });
      } else {
        resultDef.setIndexes(new int[] { sqlIndex });
      }
      resultDef.setField(field);
      resultDef.setConstructorParameter(constructorParameter);
      resultDef.setIsMapKey(isMapKey);
      resultDef.setPartialDefinitionPart(partialDefinitionPart);
      resultDef.setPartialDefinitionGroup(partialDefinitionGroup);
      resultDef.setStartPosition(openCurlyBrackets.get(curlyBracketIndex));
      resultDef.setEndPosition(closeCurlyBrackets.get(curlyBracketIndex));
      return resultDef;
    } else {
      int start = openCurlyBrackets.get(curlyBracketIndex);
      int end = curlyBracketIndex < closeCurlyBrackets.size() ? closeCurlyBrackets.get(curlyBracketIndex) : sqlLength - 1;
      throw new SqlParserException("Cannot parse definition: " + definition, start, end - start + 1);
    }
  }

//  private static final Pattern PARAMETER_DEF_PATTERN = Pattern.compile("\\{(\\w+)?%(\\d+)(\\.(\\w+))?\\}");
  private static final Pattern PARAMETER_DEF_PATTERN = 
    Pattern.compile("\\{([\\w\\-]+)?%(\\d+)(\\.(\\w+))?(@\\d+)?(\\[[\\w]+\\])?\\}");

  private ParameterDefinition parseParameterDefinition(String definition, int curlyBracketIndex) {
    int parameterIndex = -1;
    String mappingType = null;
    String field = null;
    int partialDefinitionPart = 0;
    String partialDefinitionGroup = null;
    
    Matcher matcher = PARAMETER_DEF_PATTERN.matcher(definition);
    if (matcher.find() && matcher.group().equals(definition)) {
      mappingType = matcher.group(1);
      parameterIndex = Integer.valueOf(matcher.group(2));
      field = matcher.group(4);
      if (matcher.group(5) != null) {
        partialDefinitionPart = Integer.valueOf(matcher.group(5).substring(1));
      }
      partialDefinitionGroup = matcher.group(6);
      
      ParameterDefinitionImpl parameterDef = new ParameterDefinitionImpl();
      parameterDef.setType(mappingType);
      parameterDef.setParameter(parameterIndex);
      parameterDef.setField(field);
      parameterDef.setIndexes(new int[] { sqlIndex });
      parameterDef.setPartialDefinitionPart(partialDefinitionPart);
      parameterDef.setPartialDefinitionGroup(partialDefinitionGroup);
      parameterDef.setStartPosition(openCurlyBrackets.get(curlyBracketIndex));
      parameterDef.setEndPosition(closeCurlyBrackets.get(curlyBracketIndex));
      return parameterDef;
    } else {
      int start = openCurlyBrackets.get(curlyBracketIndex);
      int end = curlyBracketIndex < closeCurlyBrackets.size() ? closeCurlyBrackets.get(curlyBracketIndex) : sqlLength - 1;
      throw new SqlParserException("Cannot parse definition: " + definition, start, end - start + 1);
    }
  }

  private String mergeStrings(String sql, List<String> stringList) {
    StringBuffer sb = new StringBuffer();
    int index = 0;

    for (int i = 0; i < sql.length(); i++) {
      char c = sql.charAt(i);
      if (c == '#') {
        sb.append(stringList.get(index++));
      } else {
        sb.append(c);
      }
    }

    return sb.toString();
  }

  private String extractStrings(String sql, List<String> stringList) {
    char quoteChar = '\0';
    StringBuffer sb = new StringBuffer();
    StringBuffer sbString = null;

    int i = 0;
    while (i < sql.length()) {
      char c = sql.charAt(i++);
      if (quoteChar == '\0') {
        if (c == '"' || c == '\'') {
          // begin of string
          quoteChar = c;
          sbString = new StringBuffer();
          sbString.append(c);
        } else {
          if (c == '{') {
            quoteChar = c;
            sb.append(' ');
          }
          sb.append(c);
        }
      } else {
        if (quoteChar == '{') {
          // remove white space from {...}
          if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
            sb.append(c);
          }
          if (c == '}') {
            quoteChar = '\0';
            sb.append(' ');
          }
        } else if (quoteChar == c) {
          // end of string
          quoteChar = '\0';
          sbString.append(c);
          stringList.add(sbString.toString());
          sb.append('#');
        } else {
          sbString.append(c);
        }
      }
    }

    return sb.toString();
  }

  private void findCurlyBrackets(String sql) {
    char quoteChar = '\0';

    int position = 0;
    while (position < sql.length()) {
      char c = sql.charAt(position);
      if (quoteChar == '\0') {
        if (c == '"' || c == '\'') {
          // begin of string
          quoteChar = c;
        } else {
          if (c == '{') {
            openCurlyBrackets.add(position);
          } else if (c == '}') {
            closeCurlyBrackets.add(position);
          }
        }
      } else {
        if (quoteChar == c) {
          // end of string
          quoteChar = '\0';
        } 
      }
      position++;
    }
  }
}
