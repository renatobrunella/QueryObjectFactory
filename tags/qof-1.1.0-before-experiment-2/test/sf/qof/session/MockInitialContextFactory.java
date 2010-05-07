package sf.qof.session;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class MockInitialContextFactory implements InitialContextFactory {

  public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
    return MockContext.getInstance();
  }

  public static void register() {
    System.setProperty("java.naming.factory.initial", MockInitialContextFactory.class.getName());
    try {
      new InitialContext();
    } catch (NamingException e) {
    }
  }
}
