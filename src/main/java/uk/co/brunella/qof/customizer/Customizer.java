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

import org.objectweb.asm.Type;

/**
 * Defines methods to customize the generation process.
 *
 * @see DefaultCustomizer
 */
public interface Customizer {
    /**
     * Customizes the type of <code>List</code> implementations.
     *
     * @return list type
     * @see java.util.List
     */
    Type getListType();

    /**
     * Customizes the type of <code>Set</code> implementations.
     *
     * @return set type
     * @see java.util.Set
     */
    Type getSetType();

    /**
     * Customizes the type of <code>Map</code> implementations.
     *
     * @return map type
     * @see java.util.Map
     */
    Type getMapType();

    /**
     * Customizes the class name of the generated query object.
     *
     * @param queryDefinitionClass query definition class or interface
     * @return class name of the query object
     */
    String getClassName(Class<?> queryDefinitionClass);

    /**
     * Customizes the way the query class gets its database connection.
     *
     * @param queryDefinitionClass query definition class or interface
     * @return connection factory customizer instance
     */
    ConnectionFactoryCustomizer getConnectionFactoryCustomizer(Class<?> queryDefinitionClass);
}
