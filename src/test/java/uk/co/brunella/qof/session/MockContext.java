package uk.co.brunella.qof.session;

import javax.naming.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class MockContext implements Context {

    private static final MockContext INSTANCE = new MockContext();
    private static Map<String, Object> registry = new HashMap<>();

    static MockContext getInstance() {
        return INSTANCE;
    }

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

    public Object addToEnvironment(String propName, Object propVal) {
        return null;
    }

    public void bind(Name name, Object obj) {
        registry.put(name.toString(), obj);
    }

    public void bind(String name, Object obj) {
        registry.put(name, obj);
    }

    public void close() {
    }

    public Name composeName(Name name, Name prefix) {
        return null;
    }

    public String composeName(String name, String prefix) {
        return null;
    }

    public Context createSubcontext(Name name) {
        return null;
    }

    public Context createSubcontext(String name) {
        return null;
    }

    public void destroySubcontext(Name name) {
    }

    public void destroySubcontext(String name) {
    }

    public Hashtable<?, ?> getEnvironment() {
        return null;
    }

    public String getNameInNamespace() {
        return null;
    }

    public NameParser getNameParser(Name name) {
        return null;
    }

    public NameParser getNameParser(String name) {
        return null;
    }

    public NamingEnumeration<NameClassPair> list(Name name) {
        return null;
    }

    public NamingEnumeration<NameClassPair> list(String name) {
        return null;
    }

    public NamingEnumeration<Binding> listBindings(Name name) {
        return null;
    }

    public NamingEnumeration<Binding> listBindings(String name) {
        return null;
    }

    public Object lookupLink(Name name) {
        return null;
    }

    public Object lookupLink(String name) {
        return null;
    }

    public void rebind(Name name, Object obj) {
    }

    public void rebind(String name, Object obj) {
    }

    public Object removeFromEnvironment(String propName) {
        return null;
    }

    public void rename(Name oldName, Name newName) {
    }

    public void rename(String oldName, String newName) {
    }

    public void unbind(Name name) {
        registry.remove(name.toString());
    }

    public void unbind(String name) {
        registry.remove(name);
    }

}
