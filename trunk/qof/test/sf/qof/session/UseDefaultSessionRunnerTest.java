package sf.qof.session;

import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.hsqldb.jdbc.jdbcDataSource;

import junit.framework.TestCase;
import sf.qof.BaseQuery;
import sf.qof.Insert;
import sf.qof.Query;
import sf.qof.QueryObjectFactory;
import sf.qof.session.DefaultSessionRunnerTest.DataSourceWrapper;

public class UseDefaultSessionRunnerTest extends TestCase {

  private DataSource dataSource;
  
  private DataSource createDataSource() {
    jdbcDataSource ds = new jdbcDataSource();
    ds.setDatabase("jdbc:hsqldb:mem:aname");
    ds.setUser("sa");
    ds.setPassword("");
    return new DataSourceWrapper(ds);
  }

  public void setUp() throws Exception {
    dataSource = createDataSource();
    MockInitialContextFactory.register();
    MockContext.getInstance().bind("datasource", dataSource);
    Statement stmt = createDataSource().getConnection().createStatement();
    try {
      stmt.execute("create table test (id integer, name varchar(40))");
    } finally {
      stmt.close();
    }
  }

  public void tearDown() throws Exception {
    MockContext.getInstance().unbind("datasource");
    Statement stmt = dataSource.getConnection().createStatement();
    try {
      stmt.execute("drop table test");
    } finally {
      stmt.close();
    }
  }

  @UseSessionContext()
  public interface DaoInterfaceDefaultContextNoTM extends BaseQuery {
    
    @Query(sql = "select count(*) num {int%%} from test")
    @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
    public Integer numberOfItemsInteger() throws SQLException;
    
    @Query(sql = "select count(*) num {int%%} from test")
    @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
    public int numberOfItemsInt() throws SQLException;
    
    @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
    @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
    public void insertItem(int id, String name) throws SQLException;
  }
  
  public void testDaoInterfaceDefaultContextNoTM() throws SQLException {
    SessionContextFactory.removeContext();
    SessionContextFactory.setDataSource(dataSource);
    
    DaoInterfaceDefaultContextNoTM dao = QueryObjectFactory.createQueryObject(DaoInterfaceDefaultContextNoTM.class);
    
    assertEquals(0, dao.numberOfItemsInteger().intValue());
    dao.insertItem(1, "Iten 1");
    assertEquals(1, dao.numberOfItemsInt());
    
    SessionContextFactory.removeContext();
  }
  
}
