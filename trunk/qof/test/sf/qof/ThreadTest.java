package sf.qof;

import java.sql.SQLException;

import junit.framework.TestCase;

public class ThreadTest extends TestCase {

  public ThreadTest(String arg0) {
    super(arg0);
  }

  public interface Test1 extends BaseQuery {
    @Query(sql = "select count(*) as count {broken%%} from person")
    int numberOfPersons() throws SQLException;
  }

  public interface Test2 extends BaseQuery {
    @Query(sql = "select count(*) as count {int%%} from person")
    int numberOfPersons() throws SQLException;
  }

  public interface Test3 extends BaseQuery {
    @Query(sql = "select count(*) as count {int%%} from person")
    int numberOfPersons() throws SQLException;
  }

  private class T extends Thread {

    private int id;
    protected boolean finishedSuccessfully;

    public T(int id) {
      this.id = id;
    }

    public void run() {
      try {
        if (id % 2 == 1) {
          try {
            QueryObjectFactory.createQueryObject(Test1.class);
            finishedSuccessfully = true;
          } catch (Exception e) {
            finishedSuccessfully = false;
          }
        } else {
          try {
            QueryObjectFactory.createQueryObject(Test2.class);
            finishedSuccessfully = true;
          } catch (Exception e) {
            finishedSuccessfully = false;
          }
        }
      } catch (Throwable t) {
        fail(t.getMessage());
      }
    }
  }

  public void testTwoThread() {
    QueryObjectFactory.createQueryObject(Test3.class);
    T t1 = new T(1);
    T t2 = new T(2);
    T t3 = new T(3);
    t1.start();
    t2.start();
    t3.start();
    try {
      t1.join();
      t2.join();
      t3.join();
    } catch (InterruptedException e) {
    }
    assertFalse(t1.finishedSuccessfully);
    assertTrue(t2.finishedSuccessfully);
    assertFalse(t3.finishedSuccessfully);
    assertNotNull(QueryObjectFactory.createQueryObject(Test2.class));
    try {
      QueryObjectFactory.createQueryObject(Test1.class);
    } catch (RuntimeException e) {
      assertEquals("No mapping found for type broken or int", e.getMessage());
    }
  }

}