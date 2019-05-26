package sf.qof;

/**
 * Marker interface for the code generator to implement paging.
 * 
 * <p>Paging is database specific and therefore the SQL dialect needs
 * to be defined before the query object is created.</p> 
 * The SQL select statement that is used will normally have an
 * <code>order by</code> clause.
 * 
 * <p><pre><blockquote>
 * public interface PagedQueries extends BaseQuery, <b>Paging</b> {
 *    &#64;Query(sql = "select id {%%.id}, name {%%.name} from person order by name")
 *    List&lt;Person&gt; listPersons() throws SQLException;
 *    ...
 * }
 * 
 * // use Oracle
 * QueryObjectFactory.setSQLDialect(new OracleDialect());
 * // create a query object
 * PagedQueries queries = QueryObjectFactory.createQueryObject(PagedQueries.class);
 * // set the database connection
 * queries.setConnection(connection);
 * // set the first row number/offset (zero-based)
 * <b>queries.setFirstResult(10);</b>
 * // set the maximum number of results
 * <b>queries.setMaxResults(20);</b>
 * // run the query
 * List&lt;Person&gt; personList = queries.listPersons();
 * // personList will contain the records 11 to 31 from the result set
 * </blockquote></pre></p>
 * 
 * @see sf.qof.QueryObjectFactory#setSQLDialect(sf.qof.dialect.SQLDialect)
 * @see sf.qof.dialect.SQLDialect
 */
public interface Paging {

  /**
   * Defines the first row of the result set to be returned.
   * Setting the maximum of rows and first result to zero disables
   * paging. This is reset to 0 after each call to a query method.
   * 
   * @param firstResult  the zero based index of the first result row
   * @return             this
   */
  Paging setFirstResult(int firstResult);
  
  /**
   * Defines the maximum number of rows that should be returned.
   * Setting the maximum of rows and first result to zero disables
   * paging. This is reset to 0 after each call to a query method. 
   * 
   * @param maxResults  maximum number of rows
   * @return            this
   */
  Paging setMaxResults(int maxResults);
}
