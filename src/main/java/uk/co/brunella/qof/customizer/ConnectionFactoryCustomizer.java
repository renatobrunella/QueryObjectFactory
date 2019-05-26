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
package uk.co.brunella.qof.customizer;

import net.sf.cglib.core.ClassEmitter;
import uk.co.brunella.qof.BaseQuery;

/**
 * Defines the methods of a customizer for the implementation of the
 * <code>getConnection()</code> and <code>setConnection()</code>.
 * 
 * The emit methods are called during the code generation to implement
 * connection handling in the generated query object class. It allows query
 * object classes to aquire the database connection from different data sources.
 */
public interface ConnectionFactoryCustomizer {

  /**
   * This method can add fields to the generated query object if needed by the
   * <code>getConnection()</code> and <code>setConnection()</code> implementation.
   * 
   * @param queryDefinitionClass   the annotated query definition class
   * @param superClass             the super class
   * @param ce                     the class emitter
   */
  void emitFields(Class<?> queryDefinitionClass, Class<?> superClass, ClassEmitter ce);

  /**
   * This method must implement the <code>getConnection()</code>
   * method as specified in the <code>BaseQuery</code> interface.
   * 
   * @param queryDefinitionClass   the annotated query definition class
   * @param superClass             the super class
   * @param ce                     the class emitter
   * @see BaseQuery#getConnection()
   */
  void emitGetConnection(Class<?> queryDefinitionClass, Class<?> superClass, ClassEmitter ce);
  
  /**
   * This method must implement the <code>ungetConnection(Connection)</code>
   * method as specified in the <code>BaseQuery</code> interface.
   * 
   * @param queryDefinitionClass   the annotated query definition class
   * @param superClass             the super class
   * @param ce                     the class emitter
   * @see BaseQuery#ungetConnection(java.sql.Connection)
   */
  void emitUngetConnection(Class<?> queryDefinitionClass, Class<?> superClass, ClassEmitter ce);

  /**
   * This method must implement the <code>setConnection()</code>
   * method as specified in the <code>BaseQuery</code> interface.
   * 
   * @param queryDefinitionClass  the annotated query definition class
   * @param superClass             the super class
   * @param ce                    the class emitter
   * @see BaseQuery#setConnection(java.sql.Connection)
   */
  void emitSetConnection(Class<?> queryDefinitionClass, Class<?> superClass, ClassEmitter ce);

}
