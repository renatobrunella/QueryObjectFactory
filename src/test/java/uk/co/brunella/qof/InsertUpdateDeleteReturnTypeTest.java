package uk.co.brunella.qof;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.List;

public class InsertUpdateDeleteReturnTypeTest extends TestCase {

    public interface InsertQueries extends BaseQuery {
        @Insert(sql = "insert into test values ({%1})")
        int[] insert(int i) throws SQLException;
    }

    public interface InsertQueries2 extends BaseQuery {
        @Insert(sql = "insert into test values ({%1})")
        int insert(List<Integer> i) throws SQLException;
    }

    public interface UpdateQueries extends BaseQuery {
        @Update(sql = "update test set x = 1 where id = {%1}")
        int[] insert(int i) throws SQLException;
    }

    public interface UpdateQueries2 extends BaseQuery {
        @Update(sql = "update test set x = 1 where id = {%1}")
        int insert(List<Integer> i) throws SQLException;
    }

    public interface DeleteQueries extends BaseQuery {
        @Delete(sql = "delete from test where id = {%1}")
        int[] insert(int i) throws SQLException;
    }

    public interface DeleteQueries2 extends BaseQuery {
        @Delete(sql = "delete from test where id = {%1}")
        int insert(List<Integer> i) throws SQLException;
    }

    public interface CallQueries extends BaseQuery {
        @Call(sql = "{ call test({%1})}")
        int call(List<Integer> i) throws SQLException;
    }

    public void testInsert() {
        try {
            QueryObjectFactory.createQueryObject(InsertQueries.class);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Only int or void is allowed as return type", e.getCause().getMessage());
        }
    }

    public void testInsert2() {
        try {
            QueryObjectFactory.createQueryObject(InsertQueries2.class);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Only int[] or void is allowed as return type", e.getCause().getMessage());
        }
    }

    public void testUpdate() {
        try {
            QueryObjectFactory.createQueryObject(UpdateQueries.class);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Only int or void is allowed as return type", e.getCause().getMessage());
        }
    }

    public void testUpdate2() {
        try {
            QueryObjectFactory.createQueryObject(UpdateQueries2.class);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Only int[] or void is allowed as return type", e.getCause().getMessage());
        }
    }

    public void testDelete() {
        try {
            QueryObjectFactory.createQueryObject(DeleteQueries.class);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Only int or void is allowed as return type", e.getCause().getMessage());
        }
    }

    public void testDelete2() {
        try {
            QueryObjectFactory.createQueryObject(DeleteQueries2.class);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Only int[] or void is allowed as return type", e.getCause().getMessage());
        }
    }

    public void testCall() {
        try {
            QueryObjectFactory.createQueryObject(CallQueries.class);
            fail("Should throw exception");
        } catch (RuntimeException e) {
            assertEquals("Only void is allowed as return type", e.getCause().getMessage());
        }
    }
}
