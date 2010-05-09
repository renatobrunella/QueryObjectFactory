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
package sf.qof.session;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the connection of the query object should be acquired from
 * a session context.
 * 
 * It allows to specify a session context name that is used in the implementation
 * of the <code>getConnection()</code> method in the query object.
 * 
 * <p><blockquote><pre>
 * &#64;UseSessionContext(name = "PERSON_CONTEXT")
 * public interface PersonQuery implements BaseQuery { 
 *   &#64;Query(sql = "select id {%%.id,%%*}, name {%%.name} from person where id = {%1}")
 *   Map&lt;Integer, Person&gt; getPerson(int id);
 * }
 * </pre></blockquote></p>
 * 
 * <p>This specifies that the session context with the name "PERSON_CONTEXT"
 * should be used. The implementation of <code>getConnection()</code> will look
 * similar to this:
 * 
 * <p><blockquote><pre>
 * public Connection getConnection() {
 *   return SessionContextFactory.getContext("PERSON_CONTEXT").getConnection();
 * }
 * </pre></blockquote></p>
 * 
 * @since 1.0.0
 * 
 * @see SessionContext
 * @see SessionContextFactory
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseSessionContext {
  /**
   * This is the session context name
   * 
   * It defaults to the default session context name.
   * 
   * @return session context name
   */
  String name() default sf.qof.session.SessionContext.DEFAULT_CONTEXT_NAME;
}
