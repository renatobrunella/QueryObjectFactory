package sf.qof.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

public class MockBundle implements Bundle {

  @SuppressWarnings("unchecked")
  public Enumeration findEntries(String path, String filePattern, boolean recurse) {
    return null;
  }

  public BundleContext getBundleContext() {
    return null;
  }

  public long getBundleId() {
    return 1;
  }

  public URL getEntry(String path) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public Enumeration getEntryPaths(String path) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public Dictionary getHeaders() {
    return null;
  }

  @SuppressWarnings("unchecked")
  public Dictionary getHeaders(String locale) {
    return null;
  }

  public long getLastModified() {
    return 0;
  }

  public String getLocation() {
    return null;
  }

  public ServiceReference[] getRegisteredServices() {
    return null;
  }

  public URL getResource(String name) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public Enumeration getResources(String name) throws IOException {
    return null;
  }

  public ServiceReference[] getServicesInUse() {
    return null;
  }

  public int getState() {
    return 0;
  }

  public String getSymbolicName() {
    return "test.bundle";
  }

  public boolean hasPermission(Object permission) {
    return false;
  }

  @SuppressWarnings("unchecked")
  public Class loadClass(String name) throws ClassNotFoundException {
    return null;
  }

  public void start() throws BundleException {
  }

  public void start(int options) throws BundleException {
  }

  public void stop() throws BundleException {
  }

  public void stop(int options) throws BundleException {
  }

  public void uninstall() throws BundleException {
  }

  public void update() throws BundleException {
  }

  public void update(InputStream in) throws BundleException {
  }

}
