/*
 * Copyright 2007 - 2011 brunella ltd
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a call method. Call methods are used to call SQL stored procedures.
 * 
 * <p> <blockquote><pre>
 * &#64;Call(sql = "{ %% = call numberOfPersons({%1}) }")
 * int numberOfPersons(String name);
 * </pre></blockquote>
 * 
 * <p> <code>{%%}</code> defines a result mapping to the primitive type <code>int</code> 
 * <p> <code>{%1}</code> defines a parameter mapping to the <code>String</code> type parameter <code>name</code>
 * 
 * @see sf.qof.parser.SqlParser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Call {
  /**
   * This is the SQL statement.
   * 
   * @return SQL statement
   * 
   * @see sf.qof.parser.SqlParser
   */
  String sql();
  
  /**
   * This is the class that has the static factory method.
   * 
   * @return the static factory class
   */
  Class<?> factoryClass() default Object.class;
  
  /**
   * This is the name of the static factory method.
   * 
   * @return the static factory method name
   */
  String factoryMethod() default "";
}
