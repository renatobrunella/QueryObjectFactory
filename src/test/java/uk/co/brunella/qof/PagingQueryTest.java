package uk.co.brunella.qof;

import junit.framework.TestCase;
import uk.co.brunella.qof.dialect.SQLDialect;
import uk.co.brunella.qof.testtools.MockConnectionData;
import uk.co.brunella.qof.testtools.MockConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PagingQueryTest extends TestCase {

    private Connection connection;
    private List<String> log;

    public void setUp() {
        connection = MockConnectionFactory.getConnection();
        log = ((MockConnectionData) connection).getLog();
    }

    public void testSqlStatements1() throws SQLException {
        QueryObjectFactory.setSQLDialect(new SQLDialect() {
            public String getLimitString(String sql, boolean hasOffset) {
                return hasOffset ? sql + " limit ? offset ?" : sql + " limit ?";
            }

            public boolean limitAddOffset() {
                return false;
            }

            public boolean limitOffsetFirst() {
                return false;
            }

            public boolean limitParametersBeforeQueryParameters() {
                return false;
            }
        });
        SelectQueries1 queries = QueryObjectFactory.createQueryObject(SelectQueries1.class);
        queries.setConnection(connection);
        queries.setFetchSize(99);
        queries.setFirstResult(0).setMaxResults(0);
        log.clear();
        queries.select(88, new java.util.Date(0), "string");
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(0).setMaxResults(5);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ?)", log.get(i++));
        assertEquals("setInt(4,5)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(2);
        queries.setMaxResults(7);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ? offset ?)", log.get(i++));
        assertEquals("setInt(4,7)", log.get(i++));
        assertEquals("setInt(5,2)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSqlStatements2() throws SQLException {
        QueryObjectFactory.setSQLDialect(new SQLDialect() {
            public String getLimitString(String sql, boolean hasOffset) {
                return hasOffset ? "offset ? limit ? " + sql : "limit ? " + sql;
            }

            public boolean limitAddOffset() {
                return true;
            }

            public boolean limitOffsetFirst() {
                return true;
            }

            public boolean limitParametersBeforeQueryParameters() {
                return true;
            }
        });
        SelectQueries2 queries = QueryObjectFactory.createQueryObject(SelectQueries2.class);
        queries.setConnection(connection);
        queries.setFetchSize(99);
        queries.setFirstResult(0);
        queries.setMaxResults(0);
        log.clear();
        queries.select(88, new java.util.Date(0), "string");
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(0);
        queries.setMaxResults(5);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(limit ? select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setInt(1,5)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(2,88)", log.get(i++));
        assertEquals("setDate(3,1970-01-01)", log.get(i++));
        assertEquals("setString(4,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(2);
        queries.setMaxResults(7);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(11, log.size());
        assertEquals("prepareStatement(offset ? limit ? select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setInt(1,2)", log.get(i++));
        assertEquals("setInt(2,9)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(3,88)", log.get(i++));
        assertEquals("setDate(4,1970-01-01)", log.get(i++));
        assertEquals("setString(5,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSqlStatements3() throws SQLException {
        QueryObjectFactory.setSQLDialect(new SQLDialect() {
            public String getLimitString(String sql, boolean hasOffset) {
                return hasOffset ? sql + " limit ? offset ?" : sql + " limit ?";
            }

            public boolean limitAddOffset() {
                return true;
            }

            public boolean limitOffsetFirst() {
                return true;
            }

            public boolean limitParametersBeforeQueryParameters() {
                return false;
            }
        });
        SelectQueries3 queries = QueryObjectFactory.createQueryObject(SelectQueries3.class);
        queries.setConnection(connection);
        queries.setFetchSize(99);
        queries.setFirstResult(0);
        queries.setMaxResults(0);
        log.clear();
        queries.select(88, new java.util.Date(0), "string");
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(0);
        queries.setMaxResults(5);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ?)", log.get(i++));
        assertEquals("setInt(4,5)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(2);
        queries.setMaxResults(7);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ? offset ?)", log.get(i++));
        assertEquals("setInt(4,2)", log.get(i++));
        assertEquals("setInt(5,9)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSqlStatements4() throws SQLException {
        QueryObjectFactory.setSQLDialect(new SQLDialect() {
            public String getLimitString(String sql, boolean hasOffset) {
                return hasOffset ? sql + " limit ? offset ?" : sql + " limit ?";
            }

            public boolean limitAddOffset() {
                return true;
            }

            public boolean limitOffsetFirst() {
                return false;
            }

            public boolean limitParametersBeforeQueryParameters() {
                return true;
            }
        });
        SelectQueries4 queries = QueryObjectFactory.createQueryObject(SelectQueries4.class);
        queries.setConnection(connection);
        queries.setFetchSize(99);
        queries.setFirstResult(0);
        queries.setMaxResults(0);
        log.clear();
        queries.select(88, new java.util.Date(0), "string");
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(0);
        queries.setMaxResults(5);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ?)", log.get(i++));
        assertEquals("setInt(1,5)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(2,88)", log.get(i++));
        assertEquals("setDate(3,1970-01-01)", log.get(i++));
        assertEquals("setString(4,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(2);
        queries.setMaxResults(7);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ? offset ?)", log.get(i++));
        assertEquals("setInt(1,9)", log.get(i++));
        assertEquals("setInt(2,2)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(3,88)", log.get(i++));
        assertEquals("setDate(4,1970-01-01)", log.get(i++));
        assertEquals("setString(5,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSqlStatements5() throws SQLException {
        QueryObjectFactory.setSQLDialect(new SQLDialect() {
            public String getLimitString(String sql, boolean hasOffset) {
                return hasOffset ? sql + " limit ? offset ?" : sql + " limit ?";
            }

            public boolean limitAddOffset() {
                return false;
            }

            public boolean limitOffsetFirst() {
                return false;
            }

            public boolean limitParametersBeforeQueryParameters() {
                return true;
            }
        });
        SelectQueries5 queries = QueryObjectFactory.createQueryObject(SelectQueries5.class);
        queries.setConnection(connection);
        queries.setFetchSize(99);
        queries.setFirstResult(0);
        queries.setMaxResults(0);
        log.clear();
        queries.select(88, new java.util.Date(0), "string");
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(0);
        queries.setMaxResults(5);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ?)", log.get(i++));
        assertEquals("setInt(1,5)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(2,88)", log.get(i++));
        assertEquals("setDate(3,1970-01-01)", log.get(i++));
        assertEquals("setString(4,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(2);
        queries.setMaxResults(7);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ? offset ?)", log.get(i++));
        assertEquals("setInt(1,7)", log.get(i++));
        assertEquals("setInt(2,2)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(3,88)", log.get(i++));
        assertEquals("setDate(4,1970-01-01)", log.get(i++));
        assertEquals("setString(5,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSqlStatements6() throws SQLException {
        QueryObjectFactory.setSQLDialect(new SQLDialect() {
            public String getLimitString(String sql, boolean hasOffset) {
                return hasOffset ? sql + " limit ? offset ?" : sql + " limit ?";
            }

            public boolean limitAddOffset() {
                return false;
            }

            public boolean limitOffsetFirst() {
                return true;
            }

            public boolean limitParametersBeforeQueryParameters() {
                return true;
            }
        });
        SelectQueries6 queries = QueryObjectFactory.createQueryObject(SelectQueries6.class);
        queries.setConnection(connection);
        queries.setFetchSize(99);
        queries.setFirstResult(0);
        queries.setMaxResults(0);
        log.clear();
        queries.select(88, new java.util.Date(0), "string");
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(0);
        queries.setMaxResults(5);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ?)", log.get(i++));
        assertEquals("setInt(1,5)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(2,88)", log.get(i++));
        assertEquals("setDate(3,1970-01-01)", log.get(i++));
        assertEquals("setString(4,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(2);
        queries.setMaxResults(7);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ? offset ?)", log.get(i++));
        assertEquals("setInt(1,2)", log.get(i++));
        assertEquals("setInt(2,7)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(3,88)", log.get(i++));
        assertEquals("setDate(4,1970-01-01)", log.get(i++));
        assertEquals("setString(5,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSqlStatements7() throws SQLException {
        QueryObjectFactory.setSQLDialect(new SQLDialect() {
            public String getLimitString(String sql, boolean hasOffset) {
                return hasOffset ? sql + " limit ? offset ?" : sql + " limit ?";
            }

            public boolean limitAddOffset() {
                return false;
            }

            public boolean limitOffsetFirst() {
                return true;
            }

            public boolean limitParametersBeforeQueryParameters() {
                return false;
            }
        });
        SelectQueries7 queries = QueryObjectFactory.createQueryObject(SelectQueries7.class);
        queries.setConnection(connection);
        queries.setFetchSize(99);
        queries.setFirstResult(0);
        queries.setMaxResults(0);
        log.clear();
        queries.select(88, new java.util.Date(0), "string");
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(0);
        queries.setMaxResults(5);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ?)", log.get(i++));
        assertEquals("setInt(4,5)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(2);
        queries.setMaxResults(7);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ? offset ?)", log.get(i++));
        assertEquals("setInt(4,2)", log.get(i++));
        assertEquals("setInt(5,7)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public void testSqlStatements8() throws SQLException {
        QueryObjectFactory.setSQLDialect(new SQLDialect() {
            public String getLimitString(String sql, boolean hasOffset) {
                return hasOffset ? sql + " limit ? offset ?" : sql + " limit ?";
            }

            public boolean limitAddOffset() {
                return true;
            }

            public boolean limitOffsetFirst() {
                return false;
            }

            public boolean limitParametersBeforeQueryParameters() {
                return false;
            }
        });
        SelectQueries8 queries = QueryObjectFactory.createQueryObject(SelectQueries8.class);
        queries.setConnection(connection);
        queries.setFetchSize(99);
        queries.setFirstResult(0);
        queries.setMaxResults(0);
        log.clear();
        queries.select(88, new java.util.Date(0), "string");
        int i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(0);
        queries.setMaxResults(5);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(10, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ?)", log.get(i++));
        assertEquals("setInt(4,5)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.setFirstResult(2);
        queries.setMaxResults(7);
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(11, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ?  limit ? offset ?)", log.get(i++));
        assertEquals("setInt(4,9)", log.get(i++));
        assertEquals("setInt(5,2)", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
        log.clear();
        queries.select(88, new java.util.Date(0), "string");
        i = 0;
        assertEquals(9, log.size());
        assertEquals("prepareStatement(select value from test where id = ? and d= ? and s= ? )", log.get(i++));
        assertEquals("setFetchSize(99)", log.get(i++));
        assertEquals("setInt(1,88)", log.get(i++));
        assertEquals("setDate(2,1970-01-01)", log.get(i++));
        assertEquals("setString(3,string)", log.get(i++));
        assertEquals("executeQuery()", log.get(i++));
        assertEquals("next()", log.get(i++));
        assertEquals("close()", log.get(i++));
        assertEquals("close()", log.get(i++));
    }

    public interface SelectQueries extends BaseQuery, Paging {
        @Query(sql = "select value {%%} from test where id = {%1} and d={%2} and s={%3}")
        List<String> select(int id, java.util.Date d, String s) throws SQLException;
    }

    public interface SelectQueries1 extends SelectQueries {
    }

    public interface SelectQueries2 extends SelectQueries {
    }

    public interface SelectQueries3 extends SelectQueries {
    }

    public interface SelectQueries4 extends SelectQueries {
    }

    public interface SelectQueries5 extends SelectQueries {
    }

    public interface SelectQueries6 extends SelectQueries {
    }

    public interface SelectQueries7 extends SelectQueries {
    }

    public interface SelectQueries8 extends SelectQueries {
    }
}
