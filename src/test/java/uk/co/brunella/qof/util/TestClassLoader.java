package uk.co.brunella.qof.util;

import sun.net.www.ParseUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class TestClassLoader extends URLClassLoader {

    private TestClassLoader(URL[] aurl, ClassLoader classloader) {
        super(aurl, classloader);
    }

    public static ClassLoader createClassLoader(ClassLoader parent) throws IOException {
        String s = System.getProperty("java.class.path");
        File[] afile = s != null ? getClassPath(s) : new File[0];
        URL[] aurl = s != null ? pathToURLs(afile) : new URL[0];

        return new TestClassLoader(aurl, parent);
    }

    private static URL[] pathToURLs(File[] afile) {
        URL[] aurl = new URL[afile.length];
        for (int i = 0; i < afile.length; i++)
            aurl[i] = getFileURL(afile[i]);

        return aurl;
    }

    static URL getFileURL(File file) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException ioexception) {
        }
        try {
            return ParseUtil.fileToEncodedURL(file);
        } catch (MalformedURLException malformedurlexception) {
            throw new InternalError();
        }
    }

    private static File[] getClassPath(String s) {
        File[] afile;
        if (s != null) {
            int i = 0;
            int j = 1;
            int k;
            for (int i1 = 0; (k = s.indexOf(File.pathSeparator, i1)) != -1; i1 = k + 1)
                j++;

            afile = new File[j];
            int l;
            int j1;
            for (j1 = l = 0; (l = s.indexOf(File.pathSeparator, j1)) != -1; j1 = l + 1)
                if (l - j1 > 0)
                    afile[i++] = new File(s.substring(j1, l));
                else
                    afile[i++] = new File(".");

            if (j1 < s.length())
                afile[i++] = new File(s.substring(j1));
            else
                afile[i++] = new File(".");
            if (i != j) {
                File[] afile1 = new File[i];
                System.arraycopy(afile, 0, afile1, 0, i);
                afile = afile1;
            }
        } else {
            afile = new File[0];
        }
        return afile;
    }

}