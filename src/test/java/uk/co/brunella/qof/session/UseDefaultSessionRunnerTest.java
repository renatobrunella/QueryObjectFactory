package uk.co.brunella.qof.session;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.co.brunella.qof.BaseQuery;
import uk.co.brunella.qof.Insert;
import uk.co.brunella.qof.Query;
import uk.co.brunella.qof.QueryObjectFactory;
import uk.co.brunella.qof.session.DefaultSessionRunnerTest.DataSourceWrapper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UseDefaultSessionRunnerTest {

    private DataSource dataSource;

    private DataSource createDataSource() {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:aname");
        ds.setUser("sa");
        ds.setPassword("");
        return new DataSourceWrapper(ds);
    }

    @Before
    public void setUp() throws Exception {
        dataSource = createDataSource();
        MockInitialContextFactory.register();
        MockContext.getInstance().bind("datasource", dataSource);
        try (Statement stmt = createDataSource().getConnection().createStatement()) {
            stmt.execute("create table test (id integer, name varchar(40))");
        }
    }

    @After
    public void tearDown() throws Exception {
        MockContext.getInstance().unbind("datasource");
        try (Statement stmt = dataSource.getConnection().createStatement()) {
            stmt.execute("drop table test");
        }
    }

    @Test
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

    @Test
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

    @Test
    public void testDaoInterfaceDefaultContextBeanTM() throws SystemException {
        SessionContextFactory.removeContext();
        SessionContextFactory.setJndiDataSource("datasource", null, TransactionManagementType.BEAN);

        DaoInterfaceDefaultContextBeanTM dao = QueryObjectFactory.createQueryObject(DaoInterfaceDefaultContextBeanTM.class);

        assertEquals(0, dao.numberOfItemsInteger().intValue());
        dao.insertItem(1, "Iten 1");
        assertEquals(1, dao.numberOfItemsInt());

        SessionContextFactory.removeContext();
    }

    @Test
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

    @Test
    public void testDaoInterfaceFails() {
        try {
            QueryObjectFactory.createQueryObject(DaoInterfaceFails.class);
            fail("should throw exception");
        } catch (RuntimeException e) {
            assertEquals("UseDefaultSessionRunner requires UseSessionContext annotation", e.getMessage());
        }
    }

    @Test
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

    @Test
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

    @UseSessionContext()
    public interface DaoInterfaceDefaultContextNoTM extends BaseQuery {

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        Integer numberOfItemsInteger() throws SystemException;

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        int numberOfItemsInt() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        void insertItem(int id, String name) throws SystemException;
    }

    @UseSessionContext(name = "TEST_CONTEXT")
    public interface DaoInterfaceNamedContextNoTM extends BaseQuery {

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        Integer numberOfItemsInteger() throws SystemException;

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        int numberOfItemsInt() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION)
        void insertItem(int id, String name) throws SystemException;
    }

    @UseSessionContext()
    public interface DaoInterfaceDefaultContextBeanTM extends BaseQuery {

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.BEAN)
        Integer numberOfItemsInteger() throws SystemException;

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.BEAN)
        int numberOfItemsInt() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.BEAN)
        void insertItem(int id, String name) throws SystemException;
    }

    @UseSessionContext()
    public interface DaoInterfaceDefaultContextContainerTM extends BaseQuery {

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.CONTAINER)
        Integer numberOfItemsInteger() throws SystemException;

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.CONTAINER)
        int numberOfItemsInt() throws SystemException;

        @Insert(sql = "insert into test (id, name) values ({%1}, {%2})")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.CONTAINER)
        void insertItem(int id, String name) throws SystemException;
    }

    public interface DaoInterfaceFails extends BaseQuery {

        @Query(sql = "select count(*) num {int%%} from test")
        @UseDefaultSessionRunner(sessionPolicy = SessionPolicy.CAN_JOIN_EXISTING_SESSION,
                transactionManagementType = TransactionManagementType.CONTAINER)
        Integer numberOfItemsInteger() throws SystemException;
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
        public String getMessage() {
            return message;
        }

        @UseDefaultSessionRunner()
        public void doInsert() throws SystemException {
            if (numberOfItemsInteger() == 0) {
                insertItem(1, "item");
            }
        }
    }

    public static abstract class BaseClass implements BaseQuery {
        @UseDefaultSessionRunner()
        protected abstract int numberOfItemsInt() throws SystemException;

        protected abstract void insertItem(int id, String name) throws SQLException;

        @UseDefaultSessionRunner
        public void doInsert() throws SQLException {
            insertItem(1, "item");
        }

        @UseDefaultSessionRunner
        public void doInsertDuplicateName() throws SQLException {
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
        public void doInsertDuplicateName() throws SQLException {
            insertItem(2, "item");
            insertItem(3, "item");
            insertItem(4, "item");
        }
    }
}
