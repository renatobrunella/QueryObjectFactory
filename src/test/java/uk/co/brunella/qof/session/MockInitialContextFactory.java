package uk.co.brunella.qof.session;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

public class MockInitialContextFactory implements InitialContextFactory {

    public static void register() {
        System.setProperty("java.naming.factory.initial", MockInitialContextFactory.class.getName());
        try {
            new InitialContext();
        } catch (NamingException e) {
        }
    }

    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return MockContext.getInstance();
    }
}
