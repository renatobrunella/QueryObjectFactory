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
package sf.qof.parser;

/**
 * Specification of a result definition.
 */
public interface ResultDefinition extends Definition {

  /**
   * Returns an array of SQL column names.
   * Can be empty.
   * 
   * @return array of SQL column names
   */
  String[] getColumns();
  
  /**
   * Returns the name a field if the result is a Java bean. 
   * 
   * @return name of a field in a Java bean result.
   */
  String getField();
  
  /**
   * Returns the index of a parameter in a constructor.
   *  
   * @return parameter index in the constructor 
   */
  int getConstructorParameter();
  
  /**
   * Returns an array of SQL column indexes.
   * Can be empty.
   * 
   * @return array of SQL column indexes
   */
  int[] getIndexes();
  
  /**
   * Returns true if the result definition is for the key if the 
   * result is a map.
   * 
   * @return map key
   */
  boolean isMapKey();
}
