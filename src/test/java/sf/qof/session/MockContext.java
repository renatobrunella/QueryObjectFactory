package sf.qof.session;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.*;

public class MockContext implements Context {

    private static final MockContext INSTANCE = new MockContext();

    public static MockContext getInstance() {
        return INSTANCE;
    }

    public static Map<String, Object> registry = new HashMap<String, Object>();

    public Object lookup(String name) throws NamingException {
        if (registry.containsKey(name)) {
            return registry.get(name);
        } else {
            throw new NamingException(name + " not found");
        }
    }

    public Object lookup(Name name) throws NamingException {
        if (registry.containsKey(name.toString())) {
            return registry.get(name.toString());
        } else {
            throw new NamingException(name + " not found");
        }
    }

    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {

        return null;
    }

    public void bind(Name name, Object obj) throws NamingException {
        registry.put(name.toString(), obj);
    }

    public void bind(String name, Object obj) throws NamingException {
        registry.put(name, obj);
    }

    public void close() throws NamingException {

    }

    public Name composeName(Name name, Name prefix) throws NamingException {

        return null;
    }

    public String composeName(String name, String prefix)
            throws NamingException {

        return null;
    }

    public Context createSubcontext(Name name) throws NamingException {

        return null;
    }

    public Context createSubcontext(String name) throws NamingException {

        return null;
    }

    public void destroySubcontext(Name name) throws NamingException {

    }

    public void destroySubcontext(String name) throws NamingException {

    }

    public Hashtable<?, ?> getEnvironment() throws NamingException {

        return null;
    }

    public String getNameInNamespace() throws NamingException {

        return null;
    }

    public NameParser getNameParser(Name name) throws NamingException {

        return null;
    }

    public NameParser getNameParser(String name) throws NamingException {

        return null;
    }

    public NamingEnumeration<NameClassPair> list(Name name)
            throws NamingException {

        return null;
    }

    public NamingEnumeration<NameClassPair> list(String name)
            throws NamingException {

        return null;
    }

    public NamingEnumeration<Binding> listBindings(Name name)
            throws NamingException {

        return null;
    }

    public NamingEnumeration<Binding> listBindings(String name)
            throws NamingException {

        return null;
    }

    public Object lookupLink(Name name) throws NamingException {

        return null;
    }

    public Object lookupLink(String name) throws NamingException {

        return null;
    }

    public void rebind(Name name, Object obj) throws NamingException {

    }

    public void rebind(String name, Object obj) throws NamingException {

    }

    public Object removeFromEnvironment(String propName) throws NamingException {

        return null;
    }

    public void rename(Name oldName, Name newName) throws NamingException {

    }

    public void rename(String oldName, String newName) throws NamingException {

    }

    public void unbind(Name name) throws NamingException {
        registry.remove(name.toString());
    }

    public void unbind(String name) throws NamingException {
        registry.remove(name);
    }

}
