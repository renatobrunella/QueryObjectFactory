package uk.co.brunella.qof;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.List;

public class HierarchyTest extends TestCase {

    public interface QueryObject extends BaseQuery {

        @Query(sql = "select property {%%.property}, property2 {%%.property2} from test")
        List<Sub> find() throws SQLException;
    }

    public void testQuery() {
        QueryObject dao = QueryObjectFactory.createQueryObject(QueryObject.class);
        assertNotNull(dao);
    }

    public static class Super {
        private String property;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }

    public static class Sub extends Super {
        private String property2;

        public String getProperty2() {
            return property2;
        }

        public void setProperty2(String property2) {
            this.property2 = property2;
        }
    }
}
