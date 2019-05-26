/*
 * Copyright 2010 brunella ltd
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
package uk.co.brunella.qof.session;

import java.lang.annotation.*;

/**
 * Methods annotated with <code>UseDefaultSessionRunner</code> are
 * are executed by the <code>DefaultSessionRunner</code>.
 *
 * <p><blockquote><pre>
 * &#64;UseSessionContext(name = "PERSON_CONTEXT")
 * public interface PersonQuery implements BaseQuery {
 *   &#64;Query(sql = "select id {%%.id}, name {%%.name} from person where id = {%1}")
 *   <b>&#64;UseDefaultSessionRunner</b>
 *   Person getPerson(int id) throws SQLException;
 * }
 * </pre></blockquote></p>
 * Method <code>getPerson()</code> is annotated with <code>&#64;UseDefaultSessionRunner</code>.
 * The code
 * <p><blockquote><pre>
 * ...
 * PersonQuery dao = QueryObjectFactory.createQueryObject(PersonQuery.class);
 * Person person = dao.getPerson(1);
 * ...
 * </pre></blockquote></p>
 * <p>
 * is equivalent to code without annotation like this:
 *
 * <p><blockquote><pre>
 * ...
 * final int id = 1;
 * Person person = DefaultSessionRunner.execute(new TransactionRunnable&lt;Person&gt;() {
 *     public Person run(Connection connection, Object... arguments) throws SQLException {
 *       PersonQuery dao = QueryObjectFactory.createQueryObject(PersonQuery.class);
 *       return dao.getPerson(id);
 *     }
 *   }, "PERSON_CONTEXT", SessionPolicy.CAN_JOIN_EXISTING_SESSION);
 * ...
 * </pre></blockquote></p>
 * <p>
 * Query object classes that have methods annotated with <code>UseDefaultSessionRunner</code>
 * must be annotated with <code>UseSessionContext</code>.
 * <p>
 * Session policy and transaction management type can be specified in the annotation.
 *
 * @see UseSessionContext
 * @see DefaultSessionRunner
 * @since 1.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface UseDefaultSessionRunner {

    /**
     * The session policy to be used by the <code>DefaultSessionRunner</code>.
     * <p>
     * The default is <code>CAN_JOIN_EXISTING_SESSION</code>.
     *
     * @return session policy
     * @see SessionPolicy
     */
    SessionPolicy sessionPolicy() default SessionPolicy.CAN_JOIN_EXISTING_SESSION;

    /**
     * The transaction management type to be used by the <code>DefaultSessionRunner</code>.
     * <p>
     * The default is <code>NONE</code>.
     *
     * @return transaction management type
     * @see TransactionManagementType
     */
    TransactionManagementType transactionManagementType() default TransactionManagementType.NONE;
}
