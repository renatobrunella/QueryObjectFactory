/*
 * Copyright 2008 - 2010 brunella ltd
 *
 * Licensed under the GPL Version 3 (the "License");
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

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Defines methods to get a connection from a data source and perform
 * additional configuration of the connection and to close connections.
 * <p>
 * A <code>SessionConnectionHandler</code> is used during starting of a
 * session to get a connection from a data source.
 *
 * <p>The <code>DefaultSessionConnectionHandler</code> gets a connection
 * from the data source and sets auto commit to false.</p>
 *
 * @see DefaultSessionConnectionHandler
 * @see SessionContextFactory#setSessionConnectionHandler(SessionConnectionHandler)
 * @see SessionContextFactory#setSessionConnectionHandler(String, SessionConnectionHandler)
 * @since 1.0.0
 */
public interface SessionConnectionHandler {

    /**
     * Returns a new connection from the data source.
     *
     * @param dataSource the data source
     * @return a new connection
     * @throws SystemException if data source does throw SQLException
     * @since 1.0.0
     */
    Connection getConnection(DataSource dataSource) throws SystemException;

    /**
     * Closes a connection.
     *
     * @param connection the connection
     * @throws SystemException if data source does throw SQLException
     * @since 1.0.0
     */
    void closeConnection(Connection connection) throws SystemException;
}
