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

/**
 * Specification of a parameter definition.
 */
public interface ParameterDefinition extends Definition {
  
  /**
   * Returns an array of SQL parameter names (for callable statements).
   * Can be empty.
   * 
   * @return array of SQL parameter names
   */
  String[] getNames();
  
  /**
   * Returns the names of the fields if the parameter is a Java bean. 
   * 
   * @return name of the fields in a Java bean parameter.
   */
  String[] getFields();
  
  /**
   * Returns an array of SQL parameter indexes.
   * Can be empty.
   * 
   * @return array of SQL parameter indexes
   */
  int[] getIndexes();
  
  /**
   * Returns the index of the parameter in the query method.
   * 
   * @return parameter index in query method 
   */
  int getParameter();
  
  /**
   * Returns the type of the parameter (string, int, etc.).
   * 
   * @return type of the parameter
   */
  String getType();
  
  /**
   * Returns the parameter separator for array parameter types.
   * 
   * @return parameter separator
   */
  String getParameterSeparator();
}
