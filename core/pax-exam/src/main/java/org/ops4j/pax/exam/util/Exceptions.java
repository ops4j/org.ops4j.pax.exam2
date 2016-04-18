/*
 * Copyright 2015 OPS4J Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.util;

/**
 * Helper class for rethrowing a checked exception as runtime exception.
 * <p>
 * Usage:
 *
 * <pre>
 * try {
 *     // ...
 * }
 * catch (IOException exc) {
 *     throw Exceptions.unchecked(exc);
 * }
 * </pre>
 *
 * @author Harald Wellmann
 *
 */
public class Exceptions {

    /**
     * Hidden constructor.
     */
    private Exceptions() {
    }

    /**
     * Rethrows a checked exception as unchecked exception. This method tricks the compiler into
     * thinking the exception is unchecked, rather than wrapping the given exception in a new
     * {@code RuntimeException}.
     * <p>
     * This method never returns. Nevertheless, it specifies a return type so it can be invoked as
     * {@code throw unchecked(exc)} in contexts where an exception type is syntactically required
     * (e.g. when the enclosing method is non-void).
     *
     * @param exc
     *            checked exception
     * @return syntactically, a runtime exception (but never actually returns)
     */
    public static RuntimeException unchecked(Throwable exc) {
        Exceptions.<RuntimeException> adapt(exc);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Exception> void adapt(Throwable exc) throws T {
        throw (T) exc;
    }
}
