package uk.co.brunella.qof;

import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class HierarchyTest {

    @Test
    public void testQuery() {
        QueryObject dao = QueryObjectFactory.createQueryObject(QueryObject.class);
        assertNotNull(dao);
    }

    public interface QueryObject extends BaseQuery {

        @Query(sql = "select property {%%.property}, property2 {%%.property2} from test")
        List<Sub> find() throws SQLException;
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
