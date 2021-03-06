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
package uk.co.brunella.qof.session;

import java.sql.Connection;

/**
 * Defines methods for session handling.
 * <p>
 * This includes methods to start and stop a session as well as retrieving the
 * current database connection and user transaction.
 *
 * <p>A session context is created by a session context factory.</p>
 * <p>A session context creates a separate session for each calling thread.
 * Therefore session can not be shared between sessions.</p>
 *
 * <p>A <code>SessionContext</code> can be used in the following way:</p>
 *
 * <blockquote><pre>
 * // get the default session context
 * SessionContext ctx = SessionContextFactory.getContext();
 *
 * // start a new session
 * ctx.startSession();
 *
 * // start a new transaction
 * ctx.getUserTransaction().begin();
 *
 * // get the database connection for the current session
 * Connection con = ctx.getConnection();
 *
 * // do something with the connection
 * ...
 *
 * // commit and end the current transaction
 * ctx.getUserTransaction().commit();
 *
 * // stop the session
 * ctx.stopSession();
 * </pre></blockquote>
 *
 * @see SessionContextFactory
 * @see UserTransaction
 * @since 1.0.0
 */
public interface SessionContext {

    /**
     * Name of the default session context.
     *
     * @since 1.0.0
     */
    String DEFAULT_CONTEXT_NAME = "DEFAULT_CONTEXT";

    /**
     * This method starts a session.
     * <p>
     * It creates a new session for the calling thread and assigns a
     * database connection to it and it creates a new transaction.
     * <p>
     * It must be called by the calling thread before any other method
     * of this session context can be called.
     *
     * @throws SystemException       Thrown if an unexpected error condition occurs
     * @throws IllegalStateException Thrown if the session is already started
     * @since 1.0.0
     */
    void startSession() throws SystemException;

    /**
     * This method stops a session.
     * <p>
     * It closes the database connection of the session owned by the calling thread.
     *
     * @throws SystemException       Thrown if an unexpected error condition occurs
     * @throws IllegalStateException Thrown if the session is already stopped
     * @since 1.0.0
     */
    void stopSession() throws SystemException;

    /**
     * Returns the user transaction of the session belonging to the calling
     * thread.
     *
     * @return the current user transaction
     * @throws IllegalStateException Thrown if the session is not started
     * @since 1.0.0
     */
    UserTransaction getUserTransaction();

    /**
     * Returns the database connection of the session belonging to the calling
     * thread.
     *
     * @return the current connection
     * @throws IllegalStateException Thrown if the session is not started
     * @since 1.0.0
     */
    Connection getConnection();
}
