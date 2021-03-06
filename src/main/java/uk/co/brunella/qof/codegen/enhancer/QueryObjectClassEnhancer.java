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
package uk.co.brunella.qof.codegen.enhancer;

/**
 * Defines methods to enhance generated query object classes.
 * <p>
 * This can be used to process additional annotations.
 *
 * @see SessionRunnerEnhancer
 */
public interface QueryObjectClassEnhancer {

    /**
     * Called during the creation of query object classes to enable enhancement.
     * <p>
     * Returns either a class that inherits from the <code>superClass</code> or the
     * <code>superClass</code> if no enhancements were made.
     *
     * @param queryDefinitionClass the query definition class or interface
     * @param superClass           the super class
     * @param <T>                  Query definition class type
     * @return the enhanced class or super class
     */
    <T> Class<T> enhance(Class<T> queryDefinitionClass, Class<T> superClass);

}