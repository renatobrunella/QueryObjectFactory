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
package uk.co.brunella.qof.dialect;

import uk.co.brunella.qof.Paging;

/**
 * Specifies database specific SQL dialects.
 * <p>
 * This interface can be implemented to use database specific
 * SQL syntax for example to implement paging.
 *
 * @see Paging
 */
public interface SQLDialect {

    /**
     * Returns a modified SQL select statement that allows limiting
     * the number of rows returned. This is used to implement paging.
     *
     * <p>If <code>hasOffset</code> is true the SQL select statement
     * that is returned must allow to set an offset as well as the
     * number of rows.</p>
     *
     * @param sql       the select SQL statement
     * @param hasOffset true is an offset should be set
     * @return the modified SQL statement
     * @see Paging
     * @see #limitParametersBeforeQueryParameters()
     * @see #limitAddOffset()
     * @see #limitOffsetFirst()
     */
    String getLimitString(String sql, boolean hasOffset);

    /**
     * Returns true is the parameters to set the maximum number of rows
     * and optionally the offset are the first parameters in the
     * prepared select statement. This is used to implement paging.
     *
     * @return true if limitation parameters are before the query parameters
     */
    boolean limitParametersBeforeQueryParameters();

    /**
     * Returns true if the offset needs to be added to maximum number of rows.
     * This is used to implement paging.
     *
     * @return true if offset and maximum number of rows need to added
     */
    boolean limitAddOffset();

    /**
     * Returns true if the offset parameter is in front of the maximum
     * number of rows parameter.
     * This is used to implement paging.
     *
     * @return true if offset is set first
     */
    boolean limitOffsetFirst();
}
