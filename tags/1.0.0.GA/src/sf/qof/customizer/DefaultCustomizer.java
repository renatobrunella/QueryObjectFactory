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
package sf.qof.customizer;

import static sf.qof.codegen.Constants.TYPE_ArrayList;
import static sf.qof.codegen.Constants.TYPE_HashMap;
import static sf.qof.codegen.Constants.TYPE_HashSet;

import org.objectweb.asm.Type;

import sf.qof.session.UseSessionContext;

/**
 * Provides the default implementation to customize the generation process.
 * 
 * @see Customizer
 */
public class DefaultCustomizer implements Customizer {
  /**
   * Customizes the type of <code>List</code> implementations.
   * 
   * @return <code>ArrayList</code> type
   * 
   * @see java.util.List
   */
  public Type getListType() {
    return TYPE_ArrayList;
  }

  /**
   * Customizes the type of <code>Map</code> implementations.
   * 
   * @return <code>HashMap</code> type
   * 
   * @see java.util.Map
   */
  public Type getMapType() {
    return TYPE_HashMap;
  }

  /**
   * Customizes the type of <code>Set</code> implementations.
   * 
   * @return <code>HashSet</code> type
   * 
   * @see java.util.Set
   */
  public Type getSetType() {
    return TYPE_HashSet;
  }

  /**
   * Customizes the class name of the generated query object. 
   * 
   * @param queryDefinitionClass query definition class or interface
   * @return class name of the query definition class with "$Impl" appended
   * @see Customizer#getClassName(Class)
   */
  public String getClassName(Class<?> queryDefinitionClass) {
    return queryDefinitionClass.getName() + "$Impl";
  }

  /**
   * Customizes the implementation of getConnection() and setConnection()
   * methods.
   * 
   * @param queryDefinitionClass query definition class or interface
   * @return a ConnectionFactoryCustomizer
   * @see Customizer#getConnectionFactoryCustomizer(Class)
   * @see ConnectionFactoryCustomizer
   */
  public ConnectionFactoryCustomizer getConnectionFactoryCustomizer(Class<?> queryDefinitionClass) {
    if (queryDefinitionClass.isAnnotationPresent(UseSessionContext.class)) {
      return new SessionContextConnectionFactoryCustomizer();
    } else {
      return new DefaultConnectionFactoryCustomizer();
    }
  }

}
