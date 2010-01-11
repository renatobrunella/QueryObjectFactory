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
package sf.qof;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an insert method. Insert methods normally use SQL insert statements.
 * 
 * <p> <blockquote><pre>
 * &#64;Insert(sql = "insert into person (id, name) values ({%1}, {%2})")
 * void insertPerson(int id, String name);
 * </pre></blockquote>
 * 
 * <p> <code>{%1}</code> defines a parameter mapping to the primitive type parameter <code>id</code> 
 * <p> <code>{%2}</code> defines a parameter mapping to the <code>String</code> type parameter <code>name</code>
 * 
 * <p> If the return parameter of the insert method is <code>int</code> or <code>int[]</code> for collections
 * then the implementation of the method will return the insert count for each SQL insert statement:
 * 
 * <p> <blockquote><pre>
 * &#64;Insert(sql = "insert into person (id, name) values ({%1}, {%2})")
 * int[] insertPersons(List&lt;Integer&gt; idList, List&lt;String&gt; nameList);
 * </pre></blockquote>
 *
 * @see sf.qof.parser.SqlParser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Insert {
  /**
   * This is the SQL statement.
   * 
   * @return SQL statement
   * 
   * @see sf.qof.parser.SqlParser
   */
  String sql();
}
