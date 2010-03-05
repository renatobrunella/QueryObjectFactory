/*
 * Copyright 2008 - 2010 brunella ltd
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
package sf.qof.plugin.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;

import sf.qof.Call;
import sf.qof.Delete;
import sf.qof.Insert;
import sf.qof.Query;
import sf.qof.Update;
import sf.qof.exception.SqlParserException;
import sf.qof.parser.ParameterDefinition;
import sf.qof.parser.ParameterDefinitionImpl;
import sf.qof.parser.ResultDefinition;
import sf.qof.parser.ResultDefinitionImpl;
import sf.qof.parser.SqlParser;

public class QofASTVisitor extends ASTVisitor {

//  private IProject project;
  private List<IProblem> errorList;
  private char fileName[];
  private CompilationUnit cu;
  private Set<NormalAnnotation> processedAnnotations;

  public QofASTVisitor(IProject project, char[] fileName, CompilationUnit cu) {
    errorList = new ArrayList<IProblem>();
    processedAnnotations = new HashSet<NormalAnnotation>();
//    this.project = project;
    this.fileName = fileName;
    this.cu = cu;
  }

  @Override
  public boolean visit(NormalAnnotation annotation) {
    // only process the annotation once
    if (processedAnnotations.contains(annotation)) {
      return true;
    } else {
      processedAnnotations.add(annotation);
    }
    String name = getQualifiedName(annotation);
    if (isQofAnnotation(name)) {
      validate(annotation);
    }
    return true;
  }

  private boolean isQofAnnotation(String name) {
    return Query.class.getName().equals(name) || Insert.class.getName().equals(name) || 
        Update.class.getName().equals(name) || Delete.class.getName().equals(name) ||
        Call.class.getName().equals(name);
  }

  // these fields are used for the validation process only
  private String annotationQualifiedName;
  private List<Object[]> infoList;
  private String sql;
  private SqlParser parser;
  private MethodDeclaration methodDeclaration;
  private boolean hasCollectionAsParameter;
  
  private void validate(NormalAnnotation annotation) {
    annotationQualifiedName = getQualifiedName(annotation);
    infoList = getAnnotationInfo(annotation, "sql");
    sql = getInfoString(infoList);
    if (annotation.getParent() instanceof MethodDeclaration) {
      methodDeclaration = (MethodDeclaration)annotation.getParent();
    } else {
      methodDeclaration = null;
    }
    parser = null;
    try {
      parser = new SqlParser(sql, isCallStatement());
    } catch (SqlParserException spe) {
      addSqlStatementProblem(infoList, spe.getMessage(), QofProblem.ERROR, spe.getStart(), spe.getLength());
    }
    
    if (methodDeclaration != null) {
      hasCollectionAsParameter = hasCollectionAsParameter(methodDeclaration);
      // common checks
      validateThrowsSQLException();
      validateParameterIndexes();
      
      if (isSelectStatement()) {
        validateSelect();
      } else if (isInsertStatement()) {
        validateInsert();
      } else if (isUpdateStatement()) {
        validateUpdate();
      } else if (isDeleteStatement()) {
        validateDelete();
      } else if (isCallStatement()) {
        validateCall();
      }
    }
  }

  private void validateSelect() {
    validateParameterDefinitions();
    validateResultDefinitions();
  }

  private void validateInsert() {
    validateReturnType();
    validateNoResultDefinition();
    validateParameterDefinitions();
  }

  private void validateUpdate() {
    validateReturnType();
    validateNoResultDefinition();
    validateParameterDefinitions();
  }

  private void validateDelete() {
    validateReturnType();
    validateNoResultDefinition();
    validateParameterDefinitions();
  }

  private void validateCall() {
    validateParameterDefinitions();
    validateResultDefinitions();
  }


  private void validateThrowsSQLException() {
    @SuppressWarnings("unchecked") List<Name> exceptions = 
        (List<Name>) methodDeclaration.thrownExceptions();
    boolean throwsSqlException = false;
    for (Name exception : exceptions) {
      IBinding binding = ((Name)exception).resolveBinding();
      if (binding != null && binding instanceof ITypeBinding) {
        if (java.sql.SQLException.class.getName().equals(((ITypeBinding)binding).getQualifiedName())) {
          throwsSqlException = true;
          break;
        }
      }
    }
    if (!throwsSqlException) {
      int start = methodDeclaration.getStartPosition();
      int end = start + methodDeclaration.getLength() - 1;
      addProblem("Should throw SQLException", QofProblem.WARNING, start, end);
    }
  }

  private void validateParameterIndexes() {
    if (parser != null) {
      int numberOfParameters = methodDeclaration.parameters().size();
      for (ParameterDefinition definition : parser.getParameterDefinitions()) {
        if (definition.getParameter() < 1 || definition.getParameter() > numberOfParameters) {
          ParameterDefinitionImpl definitionImpl = (ParameterDefinitionImpl)definition;
          addSqlStatementProblem(infoList, "Invalid parameter index: " + definitionImpl.getParameter(), 
              QofProblem.ERROR, definitionImpl.getStartPosition(), definitionImpl.getEndPosition() - definitionImpl.getStartPosition() + 1);
        }
      }
    }
  }
  
  private void validateReturnType() {
    Type returnType = methodDeclaration.getReturnType2();
    IBinding binding = returnType.resolveBinding();
    if (binding != null && binding instanceof ITypeBinding) {
      ITypeBinding typeBinding = (ITypeBinding)binding;
      if (hasCollectionAsParameter) {
        if (!typeBinding.getQualifiedName().equals("void") 
            && !typeBinding.getQualifiedName().equals("int[]")) {
          int start = returnType.getStartPosition();
          int end = start + returnType.getLength() - 1;
          addProblem("Only void or int[] is allowed as return type", QofProblem.ERROR, start, end);
        }
      } else {
        if (!typeBinding.getQualifiedName().equals("void") 
            && !typeBinding.getQualifiedName().equals("int")) {
          int start = returnType.getStartPosition();
          int end = start + returnType.getLength() - 1;
          addProblem("Only void or int is allowed as return type", QofProblem.ERROR, start, end);
        }
      }
    }
  }

  private void validateNoResultDefinition() {
    // check if insert, update or delete statements have result definitions
    if (parser != null) {
      for (ResultDefinition definition : parser.getResultDefinitions()) {
        ResultDefinitionImpl resultImpl = (ResultDefinitionImpl)definition;
        addSqlStatementProblem(infoList, "Result definition are not allowed for insert, update and delete statements", 
            QofProblem.ERROR, resultImpl.getStartPosition(), resultImpl.getEndPosition() - resultImpl.getStartPosition() + 1);
      }
    }
  }
  
  private void validateParameterDefinitions() {
    if (parser == null) {
      return;
    }
    // check parameter field mapping
    @SuppressWarnings("unchecked") List<SingleVariableDeclaration> parameters = 
      (List<SingleVariableDeclaration>) methodDeclaration.parameters();
    ITypeBinding[] parameterTypes = new ITypeBinding[parameters.size()];
    boolean isAtomic[] = new boolean[parameterTypes.length];
    for (int i = 0; i < parameters.size(); i++) {
      SingleVariableDeclaration node = parameters.get(i);
      IBinding binding = node.getType().resolveBinding();
      if (binding != null && binding instanceof ITypeBinding) {
        ITypeBinding typeBinding = (ITypeBinding)binding;
        if (isCollection(typeBinding)) {
          parameterTypes[i] = typeBinding.getTypeArguments()[0];
        } else {
          parameterTypes[i] = typeBinding;
        }
        isAtomic[i] = isAtomic(parameterTypes[i].getQualifiedName());
      }
    }
    
    for (ParameterDefinition definition : parser.getParameterDefinitions()) {
      if (definition.getFields() == null) {
        
      } else {
        if (definition.getParameter() - 1 < parameterTypes.length) {
          ITypeBinding type = parameterTypes[definition.getParameter() - 1];
          if (!isAtomic[definition.getParameter() - 1]
               && findMethods(type, definition.getFields()) == null) {
            ParameterDefinitionImpl paramImpl = (ParameterDefinitionImpl)definition;
            addSqlStatementProblem(infoList, "No getter defined for field " + Arrays.toString(definition.getFields()), 
                QofProblem.ERROR, paramImpl.getStartPosition(), paramImpl.getEndPosition() - paramImpl.getStartPosition() + 1);
          }
        }
      }
    }
  }
  
  private void validateResultDefinitions() {
    if (parser == null) {
      return;
    }
    // check result field mapping for query and call statements
    IBinding binding = methodDeclaration.getReturnType2().resolveBinding();
    if (binding != null && binding instanceof ITypeBinding) {
      ITypeBinding typeBinding = (ITypeBinding)binding;
      ITypeBinding returnType = null;
      ITypeBinding mapKeyType = null;

      if (isCollection(typeBinding)) {
        // Collection (Set or List)
        returnType = typeBinding.getTypeArguments()[0];
      } else if (isMap(typeBinding)) {
        // Map
        mapKeyType = typeBinding.getTypeArguments()[0];
        returnType = typeBinding.getTypeArguments()[1];
        
      } else {
        returnType = typeBinding;
      }

      boolean isAtomicResult = false;
      boolean isBeanResult = false;
      if (isAtomic(returnType.getQualifiedName())) {
        isAtomicResult = true;
      } else if (!isVoid(returnType.getQualifiedName())){
        isBeanResult = true;
      }
      
      if (isAtomicResult) {
        // check there is exactly one result definition
        if (parser.getResultDefinitions().length == 0) {
          addSqlStatementProblem(infoList, "No result definition defined", 
              QofProblem.ERROR, 0, sql.length());
        } else if (parser.getResultDefinitions().length > 1) {
          boolean error;
          if (parser.getResultDefinitions().length == 2) {
            error = !(parser.getResultDefinitions()[0].isMapKey() || parser.getResultDefinitions()[1].isMapKey());
          } else {
            error = true;
          }
          if (error) {
            addSqlStatementProblem(infoList, "More than one result definition defined", 
                QofProblem.ERROR, 0, sql.length());
          }
        }
      }
      
      if (isBeanResult) {
        // no result definitions defined
        if (parser.getResultDefinitions().length == 0) {
          addSqlStatementProblem(infoList, "No result definition defined", 
              QofProblem.ERROR, 0, sql.length());
        }
        
        for (ResultDefinition definition : parser.getResultDefinitions()) {
          if (definition.getField() == null && definition.getConstructorParameter() == 0 
              && !definition.isMapKey() && definition.getType().equals("auto")) {
            // no field or constructor parameter defined
            ResultDefinitionImpl resultImpl = (ResultDefinitionImpl)definition;
            addSqlStatementProblem(infoList, "Either a field or constructor parameter must be defined", 
                QofProblem.ERROR, resultImpl.getStartPosition(), resultImpl.getEndPosition() - resultImpl.getStartPosition() + 1);
          } else {
            if (definition.getField() != null) {
              if (findMethod(returnType, setterName(definition.getField())) == null) {
                // bean has no setter method
                ResultDefinitionImpl resultImpl = (ResultDefinitionImpl)definition;
                addSqlStatementProblem(infoList, "No setter defined for field " + definition.getField(), 
                    QofProblem.ERROR, resultImpl.getStartPosition(), resultImpl.getEndPosition() - resultImpl.getStartPosition() + 1);
              }
            }
          }
        }

        // Check constructor parameters
        boolean checkConstructor = true;
        Set<Integer> parameterSet = new HashSet<Integer>();
        int maxConstructorParameter = 0;
        for (ResultDefinition definition : parser.getResultDefinitions()) {
          int parameter = definition.getConstructorParameter();
          if (parameter > 0) {
            if (parameter > maxConstructorParameter) {
              maxConstructorParameter = parameter;
            }
            if (parameterSet.contains(parameter)) {
              // duplicate
              checkConstructor = false;
              ResultDefinitionImpl resultImpl = (ResultDefinitionImpl)definition;
              addSqlStatementProblem(infoList, "Duplicate constructor result definition", 
                  QofProblem.ERROR, resultImpl.getStartPosition(), resultImpl.getEndPosition() - resultImpl.getStartPosition() + 1);
            } else {
              parameterSet.add(parameter);
            }
          }
        }
        
        // check if there are any 'holes' in the parameter indexes
        if (maxConstructorParameter != parameterSet.size()) {
          checkConstructor = false;
          for (ResultDefinition definition : parser.getResultDefinitions()) {
            if (definition.getConstructorParameter() > 0) {
              ResultDefinitionImpl resultImpl = (ResultDefinitionImpl)definition;
              addSqlStatementProblem(infoList, "Inconsistent constructor result definition - index missing", 
                  QofProblem.ERROR, resultImpl.getStartPosition(), resultImpl.getEndPosition() - resultImpl.getStartPosition() + 1);
            }
          }
        }
        
        if (checkConstructor) {
          // check if a constructor exists
          if (parameterSet.size() > 0) {
            if (findConstructor(returnType, parameterSet.size()) == null) {
              for (ResultDefinition definition : parser.getResultDefinitions()) {
                if (definition.getConstructorParameter() > 0) {
                  ResultDefinitionImpl resultImpl = (ResultDefinitionImpl)definition;
                  addSqlStatementProblem(infoList, "No matching constructor could be found", 
                      QofProblem.ERROR, resultImpl.getStartPosition(), resultImpl.getEndPosition() - resultImpl.getStartPosition() + 1);
                }
              }
            }
          }
        }
      }
      
      // check map key
      int numberOfMapKeyDefinitions = 0;
      for (ResultDefinition definition : parser.getResultDefinitions()) {
        if (definition.isMapKey()) {
          numberOfMapKeyDefinitions++;
        }
      }
      
      if (mapKeyType == null && numberOfMapKeyDefinitions > 0) {
        // no map key allowed
        for (ResultDefinition definition : parser.getResultDefinitions()) {
          if (definition.isMapKey()) {
            ResultDefinitionImpl resultImpl = (ResultDefinitionImpl)definition;
            addSqlStatementProblem(infoList, "Map key result definition is only allowed for Map<?,?> return types", 
                QofProblem.ERROR, resultImpl.getStartPosition(), resultImpl.getEndPosition() - resultImpl.getStartPosition() + 1);
          }
        }
      } else if (mapKeyType != null && numberOfMapKeyDefinitions > 1) {
        // only one map key allowed
        for (ResultDefinition definition : parser.getResultDefinitions()) {
          if (definition.isMapKey()) {
            ResultDefinitionImpl resultImpl = (ResultDefinitionImpl)definition;
            addSqlStatementProblem(infoList, "Only one map key result definition is allowed", 
                QofProblem.ERROR, resultImpl.getStartPosition(), resultImpl.getEndPosition() - resultImpl.getStartPosition() + 1);
          }
        }
      } else if (mapKeyType != null && numberOfMapKeyDefinitions == 0) {
        // only one map key allowed
        addSqlStatementProblem(infoList, "A map key result definition is must be defined for Map<?,?> return types", 
            QofProblem.ERROR, 0, sql.length());
      }
      
      // check multiple results mapping to same field
      Set<String> mappedFields = new HashSet<String>();
      for (ResultDefinition definition : parser.getResultDefinitions()) {
        String field = definition.getField();
        if (field != null && mappedFields.contains(field)) {
          ResultDefinitionImpl resultImpl = (ResultDefinitionImpl)definition;
          addSqlStatementProblem(infoList, "Field " + field + " is mapped more than once", 
              QofProblem.WARNING, resultImpl.getStartPosition(), resultImpl.getEndPosition() - resultImpl.getStartPosition() + 1);
        } else {
          mappedFields.add(field);
        }
      }
      
    }
  }

  private boolean isSelectStatement() {
    return Query.class.getName().equals(annotationQualifiedName);
  }

  private boolean isInsertStatement() {
    return Insert.class.getName().equals(annotationQualifiedName);
  }

  private boolean isUpdateStatement() {
    return Update.class.getName().equals(annotationQualifiedName);
  }

  private boolean isDeleteStatement() {
    return Delete.class.getName().equals(annotationQualifiedName);
  }

  private boolean isCallStatement() {
    return Call.class.getName().equals(annotationQualifiedName);
  }

  private String getQualifiedName(NormalAnnotation annotation) {
    IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
    if (annotationBinding == null) {
      return null;
    } else {
      return annotationBinding.getAnnotationType().getQualifiedName();
    }
  }

  private boolean isAtomic(String typeName) {
    return typeName.equals("byte") || typeName.equals("java.lang.Byte")
        || typeName.equals("short") || typeName.equals("java.lang.Short")
        || typeName.equals("int") || typeName.equals("java.lang.Integer")
        || typeName.equals("long") || typeName.equals("java.lang.Long")
        || typeName.equals("float") || typeName.equals("java.lang.Float")
        || typeName.equals("double") || typeName.equals("java.lang.Double")
        || typeName.equals("java.lang.String") || typeName.equals("java.util.Date");      // Atomic
  }
  
  private boolean isVoid(String typeName) {
    return typeName.equals("void") || typeName.equals("java.lang.Void");
  }

  private String getInfoString(List<Object[]> infoList) {
    StringBuilder sb = new StringBuilder();
    for (Object[] info : infoList) {
      sb.append((String)info[1]);
    }
    return sb.toString();
  }
  
  private List<Object[]> getAnnotationInfo(NormalAnnotation annotation, String memberName) {
    @SuppressWarnings("unchecked") List<MemberValuePair> values = 
        (List<MemberValuePair>)annotation.values();
    for (MemberValuePair mvp : values) {
      if (mvp.getName().getFullyQualifiedName().equals(memberName)) {
        List<Object[]> infoList = new ArrayList<Object[]>();
        addExpressionInfo(mvp.getValue(), infoList);
        return infoList;
      }
    }
    // not found
    return null;
  }
  
  @SuppressWarnings("unchecked")
  private void addExpressionInfo(Expression expr, List<Object[]> infoList) {
    if (expr instanceof StringLiteral) {
      addInfo(infoList, expr, ((StringLiteral)expr).getLiteralValue());
    } else if (expr instanceof InfixExpression) {
      InfixExpression infixExpr = (InfixExpression)expr;
      addExpressionInfo(infixExpr.getLeftOperand(), infoList);
      addExpressionInfo(infixExpr.getRightOperand(), infoList);
      for (Expression extendedOps : (List<Expression>)infixExpr.extendedOperands()) {
        addExpressionInfo(extendedOps, infoList);
      }
    } else if (expr instanceof Name) {
      Name name = (Name)expr;
      ITypeBinding type = name.resolveTypeBinding();
      String value = "cannot_resolve";
      if (type.getQualifiedName().equals("java.lang.String")) {
        String constant = (String)name.resolveConstantExpressionValue();
        value = constant == null ? "cannot_resolve_constant" : constant;
      }
      addInfo(infoList, expr, value);
    } else {
      addInfo(infoList, expr, "cannot_resolve");
    }
   }

  private void addInfo(List<Object[]> infoList, Expression expr, String string) {
    Object[] info = new Object[2];
    info[0] = expr;
    info[1] = string;
    infoList.add(info);
  }
  
  private void addSqlStatementProblem(List<Object[]> infoList, String message, int severity, int position, int length) {
    int pos = position;
    int len = length;
    int start = 0;
    int end = 0;
    int i = 0;
    while (start == 0 && i < infoList.size()) {
      Object[] info = infoList.get(i);
      Expression expr = (Expression)info[0];
      if (pos < getInfoLength(info) - 2) {
        if (!(expr instanceof StringLiteral)) {
          start = expr.getStartPosition();
          end = expr.getStartPosition() + expr.getLength() - 1;
        } else {
          start = expr.getStartPosition() + pos + 1;
          if (getInfoLength(info) - 2 - pos >= len) {
            end = start + len - 1;
          } else {
            len = len - (getInfoLength(info) - 2 - pos);
          }
        }
      } else {
        pos = pos - (getInfoLength(info) - 2);
      }
      i++;
    }
    while (end == 0 && i < infoList.size()) {
      Object[] info = infoList.get(i);
      Expression expr = (Expression)info[0];
      if (getInfoLength(info) - 2 >= len) {
        end = expr.getStartPosition() + len;
      } else {
        len = len - (getInfoLength(info) - 2);
      }
      i++;
    }
    addProblem(message, severity, start, end);
  }

  private int getInfoLength(Object[] info) {
    Expression expr = (Expression)info[0];
    String string = (String)info[1];
    if (expr instanceof StringLiteral) {
      return expr.getLength();
    } else {
      return string.length() + 2 /* add two quotes */;
    }
  }

  private void addProblem(String message, int severity, int start, int end) {
    int line = cu.getLineNumber(start);
    errorList.add(new QofProblem(message, severity, fileName, start, end, line, 0, new String[0]));
  }
  
  private IMethodBinding[] findMethods(ITypeBinding type, String[] methodNames) {
    IMethodBinding[] methods = new IMethodBinding[methodNames.length];
    ITypeBinding currentType = type;
    for (int i = 0; i < methodNames.length; i++) {
      IMethodBinding method = findMethod(currentType, getterName(methodNames[i]));
      if (method == null) {
        method = findMethod(currentType, isName(methodNames[i]));
        if (method == null) {
          return null;
        }
      }
      currentType = method.getReturnType();
      methods[i] = method;
    }
    return methods;
  }
    
  private IMethodBinding findMethod(ITypeBinding type, String methodName) {
    ITypeBinding currentType = type;
    while (currentType != null) {
      // search current type
      for (IMethodBinding method : currentType.getDeclaredMethods()) {
        if (method.getName().equals(methodName)) {
          return method;
        }
      }
      // search interfaces
      for (ITypeBinding interfaceType : currentType.getInterfaces()) {
        IMethodBinding method = findMethod(interfaceType, methodName);
        if (method != null) {
          return method;
        }
      }
      currentType = currentType.getSuperclass();
    }
    return null;
  }
  
  private IMethodBinding findConstructor(ITypeBinding type, int numParameters) {
    ITypeBinding currentType = type;
    while (currentType != null) {
      for (IMethodBinding method : currentType.getDeclaredMethods()) {
        if (method.getName().equals(type.getName()) && method.getParameterTypes().length == numParameters) {
          return method;
        }
      }
      currentType = currentType.getSuperclass();
    }
    return null;
  }
  
  private String setterName(String field) {
    return "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
  }
  
  private String getterName(String field) {
    return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
  }
  
  private String isName(String field) {
    return "is" + field.substring(0, 1).toUpperCase() + field.substring(1);
  }
  
  private boolean isCollection(ITypeBinding typeBinding) {
    String name = typeBinding.getQualifiedName();
    if (name.equals(Collection.class.getName()) || name.startsWith(Collection.class.getName() + "<")) {    
      return true;
    }
    for (ITypeBinding intf : typeBinding.getInterfaces()) {
      name = intf.getQualifiedName();
      if (name.equals(Collection.class.getName()) || name.startsWith(Collection.class.getName() + "<")) {    
        return true;
      }
    }
    return false;
  }

  private boolean isMap(ITypeBinding typeBinding) {
    String name = typeBinding.getQualifiedName();
    if (name.equals(Map.class.getName()) || name.startsWith(Map.class.getName() + "<")) {    
      return true;
    }
    for (ITypeBinding intf : typeBinding.getInterfaces()) {
      name = intf.getQualifiedName();
      if (name.equals(Map.class.getName()) || name.startsWith(Map.class.getName() + "<")) {    
        return true;
      }
    }
    return false;
  }
  
  @SuppressWarnings("unchecked")
  private boolean hasCollectionAsParameter(MethodDeclaration methodDeclaration) {
    List<ASTNode> parameters = (List<ASTNode>)methodDeclaration.parameters();
    for (ASTNode node : parameters) {
      IBinding binding = ((SingleVariableDeclaration)node).getType().resolveBinding();
      ITypeBinding typeBinding = (ITypeBinding)binding;
      if (isCollection(typeBinding)) {
        return true;
      }
    }
    return false;
  }

  public List<IProblem> getErrorList() {
    return errorList;
  }

}
