package sf.qof.session;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;
import sf.qof.BaseQuery;
import sf.qof.Query;
import sf.qof.QueryObjectFactory;

public class UseDefaultSessionRunnerTest extends TestCase {

  @UseSessionContext(name = "MY_CONTEXT")
  public interface Dao extends BaseQuery {
    
    @Query(sql = "select sum(*) {%%} from person")
    @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
        transactionManagementType = TransactionManagementType.NONE)
    public Integer numberOfPerson() throws SQLException;
  }
  
  
  public void test() {
    Dao2 dao = QueryObjectFactory.createQueryObject(Dao2.class, 10, "string");
    assertNotNull(dao);
  }

  @UseSessionContext(name = "MY_CONTEXT")
  public static abstract class Dao2 implements BaseQuery {
    
    public Dao2(String s) {
      
    }
      
    public Dao2(int id, String s) {
      
    }
    
    @Query(sql = "select sum(*) {%%} from person")
    @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
        transactionManagementType = TransactionManagementType.NONE)
    public abstract Integer numberOfPerson(int id) throws SQLException, SystemException;
    
    protected Integer numberOfPerson2(int id) throws SQLException {
      return -1;
    }
  }
  
  public static abstract class Dao3 extends Dao2 {

    public Dao3(int id, String s) {
      super(id, s);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Integer numberOfPerson2(final int id) throws SQLException {
      try {
        return (Integer) DefaultSessionRunner.executeContainerManaged(
          new TransactionRunnable() {
            public Object run(Connection connection, Object... arguments) throws SQLException {
              return Dao3.super.numberOfPerson2(id);
            }}, SessionPolicy.CAN_JOIN_EXISTING_SESSION);
      } catch (SystemException e) {
        if (e.getCause() instanceof SQLException) {
          throw (SQLException) e.getCause();
        } else {
          throw new SQLException(e.getMessage());
        }
      }
    }
  }
}
