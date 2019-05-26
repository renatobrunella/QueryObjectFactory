package sf.qof.session;

import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;

import junit.framework.TestCase;
import sf.qof.BaseQuery;
import sf.qof.Insert;
import sf.qof.Query;
import sf.qof.QueryObjectFactory;
import sf.qof.session.DefaultSessionRunnerTest.DataSourceWrapper;

public class UseDefaultSessionRunnerTest extends TestCase {

    private DataSource dataSource;

    private DataSource createDataSource() {
        JDBCDataSource ds = new JDBCDataSource();
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
        public Integer numberOfItemsInteger() throws SystemException;

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        public int numberOfItemsInt() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        public void insertItem(int id, String name) throws SystemException;
    }

    public void testDaoInterfaceDefaultContextNoTM() throws SystemException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(dataSource);
        SessionContextFactory.setAutoCommitPolicy(true);

        DaoInterfaceDefaultContextNoTM dao = QueryObjectFactory.createQueryObject(DaoInterfaceDefaultContextNoTM.class);

        assertEquals(0, dao.numberOfItemsInteger().intValue());
        dao.insertItem(1, "Iten 1");
        assertEquals(1, dao.numberOfItemsInt());

        SessionContextFactory.removeContext();
    }

    @UseSessionContext(name = "TEST_CONTEXT")
    public interface DaoInterfaceNamedContextNoTM extends BaseQuery {

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        public Integer numberOfItemsInteger() throws SystemException;

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        public int numberOfItemsInt() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        public void insertItem(int id, String name) throws SystemException;
    }

    public void testDaoInterfaceNameContextNoTM() throws SystemException {
        SessionContextFactory.removeContext("TEST_CONTEXT");
        SessionContextFactory.setDataSource("TEST_CONTEXT", dataSource);
        SessionContextFactory.setAutoCommitPolicy("TEST_CONTEXT", true);

        DaoInterfaceNamedContextNoTM dao = QueryObjectFactory.createQueryObject(DaoInterfaceNamedContextNoTM.class);

        assertEquals(0, dao.numberOfItemsInteger().intValue());
        dao.insertItem(1, "Iten 1");
        assertEquals(1, dao.numberOfItemsInt());

        SessionContextFactory.removeContext("TEST_CONTEXT");
    }

    @UseSessionContext()
    public interface DaoInterfaceDefaultContextBeanTM extends BaseQuery {

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.BEAN)
        public Integer numberOfItemsInteger() throws SystemException;

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.BEAN)
        public int numberOfItemsInt() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.BEAN)
        public void insertItem(int id, String name) throws SystemException;
    }

    public void testDaoInterfaceDefaultContextBeanTM() throws SystemException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);

        DaoInterfaceDefaultContextBeanTM dao = QueryObjectFactory.createQueryObject(DaoInterfaceDefaultContextBeanTM.class);

        assertEquals(0, dao.numberOfItemsInteger().intValue());
        dao.insertItem(1, "Iten 1");
        assertEquals(1, dao.numberOfItemsInt());

        SessionContextFactory.removeContext();
    }

    @UseSessionContext()
    public interface DaoInterfaceDefaultContextContainerTM extends BaseQuery {

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.CONTAINER)
        public Integer numberOfItemsInteger() throws SystemException;

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.CONTAINER)
        public int numberOfItemsInt() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.CONTAINER)
        public void insertItem(int id, String name) throws SystemException;
    }

    public void testDaoInterfaceDefaultContextContainerTM() throws SystemException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.CONTAINER);

        DaoInterfaceDefaultContextContainerTM dao = QueryObjectFactory.createQueryObject(DaoInterfaceDefaultContextContainerTM.class);

        assertEquals(0, dao.numberOfItemsInteger().intValue());
        dao.insertItem(1, "Iten 1");
        // no commit!
        assertEquals(0, dao.numberOfItemsInt());

        SessionContextFactory.removeContext();
    }

    public interface DaoInterfaceFails extends BaseQuery {

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.CONTAINER)
        public Integer numberOfItemsInteger() throws SystemException;
    }

    public void testDaoInterfaceFails() throws SystemException {
        try {
            QueryObjectFactory.createQueryObject(DaoInterfaceFails.class);
            fail("should throw exception");
        } catch (RuntimeException e) {
            assertEquals("UseDefaultSessionRunner requires UseSessionContext annotation", e.getMessage());
        }
    }

    @UseSessionContext()
    public static abstract class DaoClassDefaultContextNoTM implements BaseQuery {

        private String message;

        public DaoClassDefaultContextNoTM(String message) {
            this.message = message;
        }

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner()
        protected abstract int numberOfItemsInt() throws SystemException;

        @Query(sql = "select count(*) num {int%%} from test")
        protected abstract Integer numberOfItemsInteger() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        protected abstract void insertItem(int id, String name) throws SystemException;

        @UseDefaultSessionRunner()
        public String getMessage() throws SystemException {
            return message;
        }

        @UseDefaultSessionRunner()
        public void doInsert() throws SystemException {
            if (numberOfItemsInteger() == 0) {
                insertItem(1, "item");
            }
        }
    }

    public void testDaoClassDefaultContextNoTM() throws SystemException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(dataSource);
        SessionContextFactory.setAutoCommitPolicy(true);

        DaoClassDefaultContextNoTM dao = QueryObjectFactory.createQueryObject(DaoClassDefaultContextNoTM.class, "Hi there");
        assertEquals("Hi there", dao.getMessage());

        assertEquals(0, dao.numberOfItemsInt());
        dao.doInsert();
        assertEquals(1, dao.numberOfItemsInt());

        SessionContextFactory.removeContext();
    }

    public static abstract class BaseClass implements BaseQuery {
        @UseDefaultSessionRunner()
        protected abstract int numberOfItemsInt() throws SystemException;

        protected abstract void insertItem(int id, String name) throws SQLException;

        @UseDefaultSessionRunner
        public void doInsert() throws SystemException, SQLException {
            insertItem(1, "item");
        }

        @UseDefaultSessionRunner
        public void doInsertDuplicateName() throws SystemException, SQLException {
            // this is never called!!!
            insertItem(2, "item");
            insertItem(3, "item");
        }
    }

    @UseSessionContext
    public static abstract class SubClass extends BaseClass {
        @Query(sql = "select count(*) num {int%%} from test")
        protected abstract int numberOfItemsInt() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        @Override
        protected abstract void insertItem(int id, String name) throws SQLException;

        @UseDefaultSessionRunner
        public void doInsertDuplicateName() throws SystemException, SQLException {
            insertItem(2, "item");
            insertItem(3, "item");
            insertItem(4, "item");
        }
    }

    public void testAnnotatedBaseClass() throws SystemException, SQLException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setDataSource(dataSource);
        SessionContextFactory.setAutoCommitPolicy(true);

        SubClass dao = QueryObjectFactory.createQueryObject(SubClass.class);

        assertEquals(0, dao.numberOfItemsInt());
        dao.doInsert();
        assertEquals(1, dao.numberOfItemsInt());
        dao.doInsertDuplicateName();
        assertEquals(4, dao.numberOfItemsInt());

        SessionContextFactory.removeContext();
    }
}
