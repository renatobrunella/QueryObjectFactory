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
package uk.co.brunella.qof;

import uk.co.brunella.qof.parser.SqlParser;

import java.lang.annotation.*;

/**
 * Defines a query method. Query methods normally use SQL select statements.
 *
 * <p> <blockquote><pre>
 * &#64;Query(sql = "select id {int%%1}, name {%%.name} from person where id = {%1}")
 * Person getPerson(int id);
 *
 * &#64;Query(sql = "select id {%%.id,%%*}, name {%%.name} from person where id = {%1}")
 * Map&lt;Integer, Person&gt; getPerson(int id);
 * </pre></blockquote>
 *
 * <p> <code>{%%.name}</code> defines a result mapping to the setter <code>setName</code> on <code>Person</code>
 * <br> <code>{%%*}</code> defines a the map ket for the colletion type <code>Map</code> using <code>id</code>
 * <br> <code>{int%%1}</code> defines a result mapping to the first parameter of the
 * constructor of <code>Person</code>
 * <br> <code>{%1}</code> defines a parameter mapping to the primitive type parameter <code>id</code>
 *
 * <p> Allowed return types of the query method are:
 * <ul type="disc">
 * <li> An atomic value object type such as <code>int</code>, <code>Double</code>, <code>String</code>, etc. </li>
 * <li> Java object type supporting JavaBean style getters and setters.</li>
 * <li> A value object mapped with a custom mapping adapter
 * <li> A <code>List</code> collection with a generic type definition of one of the above types</li>
 * <li> A <code>Set</code> collection with a generic type definition of one of the above types</li>
 * <li> A <code>Map</code> collection with a generic type definition of one of the above types
 * and an atomic value object type or a value object mapped with custom mapping adapter as map key</li>
 * </ul>
 *
 * @see SqlParser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Query {
    /**
     * This is the SQL statement.
     *
     * @return SQL statement
     * @see SqlParser
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

    /**
     * This is the actual collection type to be used for the result.
     *
     * @return the collection type returned
     */
    Class<?> collectionClass() default Object.class;

    /**
     * This is the initial capacity of collection.
     * <p>
     * Set to 0 for default capacity.
     *
     * @return initial capacity
     */
    int collectionInitialCapacity() default 0;
}
