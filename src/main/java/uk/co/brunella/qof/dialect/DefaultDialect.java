package uk.co.brunella.qof.dialect;

/**
 * Implements default SQL syntax.
 */
public class DefaultDialect implements SQLDialect {

  public String getLimitString(String sql, boolean hasOffset) {
    throw new RuntimeException("Not supported");
  }

  public boolean limitParametersBeforeQueryParameters() {
    throw new RuntimeException("Not supported");
  }

  public boolean limitAddOffset() {
    throw new RuntimeException("Not supported");
  }

  public boolean limitOffsetFirst() {
    throw new RuntimeException("Not supported");
  }

}
