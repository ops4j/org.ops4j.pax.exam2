/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Wrap not serializable exception while extracting as much information as possible
 */
public class WrappedTestContainerException extends TestContainerException {
    private static final long serialVersionUID = 2153567650526556189L;
    private String wrappedClassName;
    private String wrappedMessage;

    public WrappedTestContainerException(Throwable exception) {
        this(exception.getMessage(), exception);
    }

    public WrappedTestContainerException(String message, Throwable exception) {
        super(message);
        wrappedClassName = exception.getClass().getName();
        wrappedMessage = exception.getMessage();
        setStackTrace(exception.getStackTrace());
    }

    public String getWrappedClassName() {
        return wrappedClassName;
    }

    public String getWrappedMessage() {
        return wrappedMessage;
    }

    public static Throwable wrapIfNeeded(Throwable exc) {
        return isSerializable(exc) ? exc : new WrappedTestContainerException(exc.getMessage(), exc);
    }

    /**
     * Check if given exception is serializable by doing a serialization and checking the exception
     *
     * @param ex
     *            exception to check
     * @return if the given exception is serializable
     */
    private static boolean isSerializable(Throwable exc) {
        try (ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream())) {
            os.writeObject(exc);
            return true;
        }
        catch (IOException ioe) {
            return false;
        }
    }
}
