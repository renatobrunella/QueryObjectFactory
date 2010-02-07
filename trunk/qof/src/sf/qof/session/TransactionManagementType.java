package sf.qof.session;

/**
 * Defines the transaction management type.
 * 
 * @since 1.1.0
 */
public enum TransactionManagementType {

  /**
   * The bean is managing the transaction.
   */
  BEAN,
  
  /**
   * The container is managing the transaction.
   */
  CONTAINER
}
