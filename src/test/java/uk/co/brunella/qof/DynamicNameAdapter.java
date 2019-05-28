package uk.co.brunella.qof;

import uk.co.brunella.qof.adapter.DynamicMappingAdapter;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;


public class DynamicNameAdapter implements DynamicMappingAdapter {

    private final static int[] types = new int[]{java.sql.Types.VARCHAR, java.sql.Types.VARCHAR};

    public Set<Class<?>> getTypes() {
        Set<Class<?>> types = new HashSet<>();
        types.add(Name.class);
        return types;
    }

    public Object get(ResultSet rs, int[] indexes) throws SQLException {
        return new Name(rs.getString(indexes[0]), rs.getString(indexes[1]));
    }

    public Object get(ResultSet rs, String[] columns) throws SQLException {
        return new Name(rs.getString(columns[0]), rs.getString(columns[1]));
    }

    public Object get(CallableStatement cs, int[] indexes) throws SQLException {
        return new Name(cs.getString(indexes[0]), cs.getString(indexes[1]));
    }

    public void set(PreparedStatement ps, Object value, int[] indexes) throws SQLException {
        Name name = (Name) value;
        if (name != null) {
            ps.setString(indexes[0], name.getFirstName());
            ps.setString(indexes[1], name.getLastName());
        } else {
            ps.setNull(indexes[0], java.sql.Types.VARCHAR);
            ps.setNull(indexes[1], java.sql.Types.VARCHAR);
        }
    }

    public void registerOutputParameter(CallableStatement cs, int[] indexes) throws SQLException {
        cs.registerOutParameter(indexes[0], java.sql.Types.VARCHAR);
        cs.registerOutParameter(indexes[1], java.sql.Types.VARCHAR);
    }

    public int getNumberOfColumns() {
        return 2;
    }

    public int[] preferredSqlTypes() {
        return types;
    }
}
