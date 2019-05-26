package uk.co.brunella.qof.util;

import junit.framework.TestCase;

public class ClassGenerationCacheTest extends TestCase {

    private static class C1 {
    }

    private static class C2 {
    }

    private static class C3 {
    }

    public void testCacheClass1() {
        ClassGenerationCache cache = new ClassGenerationCache();
        assertNotNull(cache);
        assertNull(ClassGenerationCache.getCachedClass(C1.class));
        ClassGenerationCache.putCachedClass(C1.class, C1.class);
        assertEquals(C1.class, ClassGenerationCache.getCachedClass(C1.class));
    }

    public void testCacheClass2() {
        assertNull(ClassGenerationCache.getCachedClass(C2.class));
        final Class<?>[] clazz = new Class<?>[1];
        Thread t = new Thread() {
            public void run() {
                clazz[0] = ClassGenerationCache.getCachedClass(C2.class);
            }
        };
        t.start();
        ClassGenerationCache.putCachedClass(C2.class, C2.class);
        try {
            t.join();
        } catch (InterruptedException e) {
        }
        assertEquals(C2.class, clazz[0]);
        assertFalse(t.isAlive());
    }

    public void testCacheClass3() throws InterruptedException {
        assertNull(ClassGenerationCache.getCachedClass(C3.class));
        final Class<?>[] clazz = new Class<?>[1];
        Thread t = new Thread() {
            public void run() {
                clazz[0] = ClassGenerationCache.getCachedClass(C3.class);
            }
        };
        t.start();
        assertNull(clazz[0]);
        t.interrupt();
        Thread.sleep(10);
        ClassGenerationCache.putCachedClass(C3.class, C3.class);
        t.join();
        assertEquals(C3.class, clazz[0]);
        assertFalse(t.isAlive());
    }
}
