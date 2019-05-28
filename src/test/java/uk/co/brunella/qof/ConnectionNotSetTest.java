package uk.co.brunella.qof;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ConnectionNotSetTest {

    @Test
    public void testNoConnection() {
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

    public interface Queries extends BaseQuery {

        @Query(sql = "select abc {%%} from test")
        String select() throws SQLException;
    }
}
