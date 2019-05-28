package uk.co.brunella.qof.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CallStackIntrospectorTest {

    private static int findBaseLevel(Class<?>[] callStack) {
        for (int i = callStack.length - 1; i >= 0; i--) {
            if (callStack[i] == CallStackIntrospector.class) {
                return i;
            }
        }
        return -1;
    }

    @Test
    public void testGetCallStack() {
        Class<?>[] stack = CallStackIntrospector.getCallStack();
        int baseLevel = findBaseLevel(stack);
        assertEquals(CallStackIntrospector.class, stack[baseLevel]);
        assertEquals(getClass(), stack[baseLevel + 1]);
    }

    @Test
    public void testGetCaller() {
        Class<?> callerClass = getCaller();
        assertEquals(getClass(), callerClass);
    }

    private Class<?> getCaller() {
        return CallStackIntrospector.getCaller();
    }

    @Test
    public void testGetCallerLevel() {
        Class<?> callerClass = CallStackIntrospector.getCaller(0);
        assertEquals(getClass(), callerClass);

        assertEquals(getClass(), Call.getCaller(1));
        assertEquals(Call.class, Call.getCaller(0));
    }

    public static class Call {
        static Class<?> getCaller(int level) {
            return CallStackIntrospector.getCaller(level);
        }
    }
}
