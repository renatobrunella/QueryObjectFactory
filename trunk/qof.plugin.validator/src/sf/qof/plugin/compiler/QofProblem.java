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

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;

public class QofProblem extends CategorizedProblem implements IProblem {

  public static final int EXPECTED = 1000;
  public static final int ERROR = 1;
  public static final int WARNING = 0;
  private static final String NO_ARGS[] = new String[0];
  private int severity;
  private int startingOffset;
  private int endingOffset;
  private int line;
  private char fileName[];
  private final String message;
  private int errorCode;
  private final String arguments[];
//  private static final String MARKER_TYPE_PROBLEM = "org.eclipse.jdt.core.problem";

  public QofProblem(String msg, int severity, char fileName[],
      int startingOffset, int endingOffset, int line, int errorCode,
      String arguments[]) {
    this.message = msg;
    this.severity = severity;
    this.startingOffset = startingOffset;
    this.endingOffset = endingOffset;
    this.line = line;
    this.fileName = fileName;
    this.errorCode = errorCode;
    this.arguments = arguments;
  }

  public int getID() {
    int id = 0;
    if (errorCode == 5)
      id = 1000;
    return id;
  }

  public String[] getArguments() {
    return arguments != null ? (String[]) arguments.clone() : NO_ARGS;
  }

  public String getMessage() {
    return message;
  }

  public char[] getOriginatingFileName() {
    return fileName;
  }

  public int getSourceStart() {
    return startingOffset;
  }

  public int getSourceEnd() {
    return endingOffset;
  }

  public int getSourceLineNumber() {
    return line;
  }

  public void setSourceStart(int sourceStart) {
    startingOffset = sourceStart;
  }

  public void setSourceEnd(int sourceEnd) {
    endingOffset = sourceEnd;
  }

  public void setSourceLineNumber(int lineNumber) {
    line = lineNumber;
  }

  public boolean isError() {
    return severity == 1;
  }

  public boolean isWarning() {
    return severity == 0;
  }

  public String toString() {
    return message != null ? message : "<null message>";
  }

  public int getCategoryID() {
    return 0;
  }

  public String getMarkerType() {
    return "org.eclipse.jdt.core.problem";
  }

}
