package uk.co.brunella.qof.session;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class DefaultUserTransactionTest {

    private SessionContextFactory.DefaultUserTransaction trx;

    private DataSource createDataSource() {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:aname");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Before
    public void setUp() throws SQLException {
        trx = new SessionContextFactory.DefaultUserTransaction(new SessionContextFactory.Session(), createDataSource().getConnection());
    }

    @Test
    public void testBegin() {
        trx.begin();
        try {
            trx.begin();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Invalid state: Transaction is IN_TRANSACTION", e.getMessage());
        }
    }

    @Test
    public void testCommitFailed() throws SystemException, RollbackException {
        try {
            trx.commit();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Invalid state: Transaction is NEW", e.getMessage());
        }
        try {
            trx.close();
            trx.commit();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Invalid state: Transaction is CLOSED", e.getMessage());
        }
    }

    @Test
    public void testCommitTwice() throws SystemException, RollbackException {
        trx.begin();
        trx.commit();
        try {
            trx.commit();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Invalid state: Transaction is NEW", e.getMessage());
        }
    }

    @Test
    public void testRollbackFailed() throws SystemException {
        try {
            trx.rollback();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Invalid state: Transaction is NEW", e.getMessage());
        }
    }

    @Test
    public void testRollbackTwice() throws SystemException {
        trx.begin();
        trx.rollback();
        try {
            trx.rollback();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Invalid state: Transaction is NEW", e.getMessage());
        }
    }

    @Test
    public void testCommitForcedRollback() throws SystemException {
        trx.begin();
        trx.setRollbackOnly();
        try {
            trx.commit();
            fail("Should raise exception");
        } catch (RollbackException e) {
            assertEquals("Transaction was rolled back", e.getMessage());
        }
    }

    @Test
    public void testSetRollback() {
        trx.begin();
        assertFalse(trx.isRollbackOnly());
        trx.setRollbackOnly();
        assertTrue(trx.isRollbackOnly());
    }

    @Test
    public void testSetRollbackFailed() {
        try {
            trx.setRollbackOnly();
            fail("Should raise exception");
        } catch (RuntimeException e) {
            assertEquals("Invalid state: Transaction is NEW", e.getMessage());
        }
    }
}
