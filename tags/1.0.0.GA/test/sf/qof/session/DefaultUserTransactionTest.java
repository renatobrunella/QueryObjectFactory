package sf.qof.session;

import java.sql.SQLException;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.hsqldb.jdbc.jdbcDataSource;

public class DefaultUserTransactionTest extends TestCase {

	private DataSource createDataSource() {
		jdbcDataSource ds = new jdbcDataSource();
		ds.setDatabase("jdbc:hsqldb:mem:aname");
		ds.setUser("sa");
		ds.setPassword("");
		return ds;
	}

	private SessionContextFactory.DefaultUserTransaction trx;
	
	public void setUp() throws SQLException {
		trx = new SessionContextFactory.DefaultUserTransaction(createDataSource().getConnection());
	}
	
	public void testBegin() throws SystemException {
		trx.begin();
		try {
			trx.begin();
			fail("Should raise exception");
		} catch (RuntimeException e) {
			assertEquals("Invalid state: Transaction is IN_TRANSACTION", e.getMessage());
		}
	}
	
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
	
	public void testRollbackFailed() throws SystemException, RollbackException {
		try {
			trx.rollback();
			fail("Should raise exception");
		} catch (RuntimeException e) {
			assertEquals("Invalid state: Transaction is NEW", e.getMessage());
		}
	}
	
	public void testRollbackTwice() throws SystemException, RollbackException {
		trx.begin();
		trx.rollback();
		try {
			trx.rollback();
			fail("Should raise exception");
		} catch (RuntimeException e) {
			assertEquals("Invalid state: Transaction is NEW", e.getMessage());
		}
	}
	
	public void testCommitForcedRollback() throws SystemException, RollbackException {
		trx.begin();
		trx.setRollbackOnly();
		try {
			trx.commit();
			fail("Should raise exception");
		} catch (RollbackException e) {
			assertEquals("Transaction was rolled back", e.getMessage());
		}
	}
	
	public void testSetRollback() throws SystemException, RollbackException {
		trx.begin();
		assertFalse(trx.isRollbackOnly());
		trx.setRollbackOnly();
		assertTrue(trx.isRollbackOnly());
	}
	
	public void testSetRollbackFailed() throws SystemException, RollbackException {
		try {
			trx.setRollbackOnly();
			fail("Should raise exception");
		} catch (RuntimeException e) {
			assertEquals("Invalid state: Transaction is NEW", e.getMessage());
		}
	}
}
