package org.ops4j.pax.exam;

/**
 *
 */
public class ExceptionHelper {

    
    /** Hidden utility class constructor */
    private ExceptionHelper() {
    }

    public static Throwable unwind(Throwable e) {
        Throwable t = e.getCause();
        if (t != null) {
            return unwind(t);
        }
        else {
            return e;
        }
    }

    public static boolean hasThrowable(Throwable stack, Class<? extends Throwable> clazz) {
        if (stack == null) {
            return false;
        }
        else if (stack.getClass().getName().equalsIgnoreCase(clazz.getName())) {
            return true;
        }
        else {
            return hasThrowable(stack.getCause(), clazz);
        }
    }
}
