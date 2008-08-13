/*
 * Copyright 2008 brunella ltd
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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class QofCompilationParticipant extends CompilationParticipant {

  public QofCompilationParticipant() {
  }

  public boolean isActive(IJavaProject javaProject) {
    //TODO check QOF nature
    return true;
    // IProject project = javaProject.getProject();
    // return project.exists() && ProjectHelper.projectHasQofNature(project);
  }

  public void reconcile(ReconcileContext reconcilecontext) {
  }

  public void buildStarting(BuildContext abuildcontext[], boolean flag) {
  }

  public boolean isAnnotationProcessor() {
    return true;
  }

  public void processAnnotations(BuildContext files[]) {
    if (files.length <= 0) {
      return;
    }
    IProject project = files[0].getFile().getProject();
    for (int i = 0; i < files.length; i++) {
      CompilationUnit cu = parseCompilationUnit(files[i], true);
      QofASTVisitor qofASTVisitor = new QofASTVisitor(project, files[i].getFile().getName().toCharArray(), cu);
      cu.accept(qofASTVisitor);
      List<IProblem> errorList = qofASTVisitor.getErrorList();
      if (!errorList.isEmpty()) {
        CategorizedProblem problems[] = (CategorizedProblem[]) errorList.toArray(new CategorizedProblem[errorList.size()]);
        files[i].recordNewProblems(problems);
      }
    }
  }

  protected CompilationUnit parseCompilationUnit(BuildContext buildContext, boolean resolveBindings) {
    try {
      ICompilationUnit cu = JavaCore.createCompilationUnitFrom(buildContext.getFile());
      ASTParser parser = ASTParser.newParser(AST.JLS3);
      parser.setSource(cu);
      parser.setResolveBindings(resolveBindings);
      ASTNode result = parser.createAST(null);
      return (CompilationUnit) result;
    } catch (IllegalStateException _ex) {
      throw new IllegalArgumentException();
    }
  }

}
