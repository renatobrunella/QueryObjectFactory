package sf.qof;

import sf.qof.BaseQuery;
import sf.qof.QueryObjectFactory;
import junit.framework.TestCase;

public class DefaultFieldTest extends TestCase {

    public interface TestBatchFetchIntf extends BaseQuery {
        int DEFAULT_FETCH_SIZE = 99;
        int DEFAULT_BATCH_SIZE = 88;

        @Query(sql = "select id {%%} from test")
        int select();
    }

    public static abstract class TestBatchFetchClass implements BaseQuery {
        public final static int DEFAULT_FETCH_SIZE = 99;
        public final static int DEFAULT_BATCH_SIZE = 88;

        @Query(sql = "select id {%%} from test")
        protected abstract int select();
    }

    public static abstract class TestBatchFetchClass2 implements BaseQuery {
        protected int batchSize;
        protected int fetchSize;

        public void test() {
            batchSize = 10;
            fetchSize = 20;
        }

        @Query(sql = "select id {%%} from test")
        protected abstract int select();
    }

    public void testBatchFetchIntf() {
        TestBatchFetchIntf test = QueryObjectFactory.createQueryObject(TestBatchFetchIntf.class);
        assertEquals(88, test.getBatchSize());
        assertEquals(99, test.getFetchSize());
    }

    public void testBatchFetchClass() {
        TestBatchFetchClass test = QueryObjectFactory.createQueryObject(TestBatchFetchClass.class);
        assertEquals(88, test.getBatchSize());
        assertEquals(99, test.getFetchSize());
    }

    public void testBatchFetchClass2() {
        TestBatchFetchClass2 test = QueryObjectFactory.createQueryObject(TestBatchFetchClass2.class);
        test.test();
        assertEquals(10, test.getBatchSize());
        assertEquals(20, test.getFetchSize());
    }
}
