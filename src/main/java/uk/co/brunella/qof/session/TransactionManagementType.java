package uk.co.brunella.qof.session;

/**
 * Defines the transaction management type.
 * 
 * @since 1.1.0
 */
public enum TransactionManagementType {

  /**
   * No special transaction management
   */
  NONE,
  
  /**
   * The bean is managing the transaction.
   */
  BEAN,
  
  /**
   * The container is managing the transaction.
   */
  CONTAINER
}
