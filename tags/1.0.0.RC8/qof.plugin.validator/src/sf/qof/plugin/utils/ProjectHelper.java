/*
 * Copyright 2008 brunella ltd
 *
 * Licensed under the LGPL Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package sf.qof.plugin.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

import sf.qof.plugin.Activator;

public class ProjectHelper {

  private static String qofLibraryName = null;
  private static String qofLibrarySourceName = null;
  
  public static void addQOFLibraryToProject(IProject project, String folder) {
    try {
      IFolder jarFolder = project.getFolder(folder);
      if (!jarFolder.exists()) {
        jarFolder.create(false, true, null);
      }
      String qofLibrary = getQOFLibraryName();
      if (qofLibrary != null) {
        // copy the QOF library to the project
        IFile libraryTarget = jarFolder.getFile(qofLibrary);
        if (!libraryTarget.exists()) {
          Bundle bundle = Activator.getDefault().getBundle();
          InputStream source = bundle.getResource("/lib/" + qofLibrary).openStream();
          try {
            libraryTarget.create(source, false, null);
          } finally {
            source.close();
          }
        }
        String qofLibrarySource = getQOFLibrarySourceName();
        IFile librarySourceTarget = jarFolder.getFile(qofLibrarySource);
        if (!librarySourceTarget.exists()) {
          Bundle bundle = Activator.getDefault().getBundle();
          InputStream source = bundle.getResource("/lib/" + qofLibrarySource).openStream();
          try {
            librarySourceTarget.create(source, false, null);
          } finally {
            source.close();
          }
        }
        
        // add QOF library to classpath
        IJavaProject javaProject = JavaCore.create(project);
        if (!javaProject.isOnClasspath(libraryTarget)) {
          IClasspathEntry[] oldClasspath = javaProject.getRawClasspath();
          IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length + 1];
          System.arraycopy(oldClasspath, 0, newClasspath, 0, oldClasspath.length);
          IPath qofLibraryPath = new Path(project.getFullPath().toString() + "/" + libraryTarget.getProjectRelativePath().toString());
          IPath qofLibrarySourcePath = new Path(project.getFullPath().toString() + "/" + librarySourceTarget.getProjectRelativePath().toString());
          newClasspath[newClasspath.length - 1] = 
            JavaCore.newLibraryEntry(qofLibraryPath, qofLibrarySourcePath, null);
          javaProject.setRawClasspath(newClasspath, null);
        }
      }
    } catch (CoreException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static String getQOFLibraryName() {
    if (qofLibraryName == null) {
      getLibraryNames();
    }
    return qofLibraryName;
  }
  
  public static String getQOFLibrarySourceName() {
    if (qofLibrarySourceName == null) {
      getLibraryNames();
    }
    return qofLibrarySourceName;
  }

  private static void getLibraryNames() {
    Bundle bundle = Activator.getDefault().getBundle();
    @SuppressWarnings("unchecked") Enumeration<String> entries = bundle.getEntryPaths("/lib"); 
    while ((qofLibraryName == null || qofLibrarySourceName == null) && entries.hasMoreElements()) {
      String entry = (String) entries.nextElement();
      entry = entry.substring(entry.indexOf('/') + 1);
      if (entry.startsWith("sf.qof-source-")) {
        qofLibrarySourceName = entry;
      } else if (entry.startsWith("sf.qof-")) {
        qofLibraryName = entry;
      }
    }
  }
}
