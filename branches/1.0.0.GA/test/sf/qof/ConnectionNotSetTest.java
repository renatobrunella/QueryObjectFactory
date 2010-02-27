package sf.qof;

import java.sql.SQLException;

import junit.framework.TestCase;

public class ConnectionNotSetTest extends TestCase {

  public interface Queries extends BaseQuery {
    
    @Query(sql = "select abc {%%} from test")
    String select() throws SQLException;
  }
  
  public void testNoConnection() throws SQLException {
    Queries dao = QueryObjectFactory.createQueryObject(Queries.class);
    try {
      dao.select();
      fail("Should throw exception");
    } catch (SQLException e) {
      assertEquals("Connection was not set", e.getMessage());
    } catch (Exception e) {
      fail("Invalid exception " + e);
    }
  }
}
