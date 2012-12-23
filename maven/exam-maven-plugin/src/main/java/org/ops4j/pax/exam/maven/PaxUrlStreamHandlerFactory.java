/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.maven;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.List;

/**
 * URL handler lookup by package name uses the application class loader by default, which does not
 * work in a Maven plugin context where the Pax URL dependencies are not on the Maven class path.
 * <p>
 * For this reason, we install this {@code URLStreamHandleFactory} which loads Pax URL protocol
 * handlers from a custom classloader.
 * 
 * @author Harald Wellmann
 * 
 */
public class PaxUrlStreamHandlerFactory implements URLStreamHandlerFactory {

    /**
     * Default protocol supported by the JRE, to be ignored by this factory.
     */
    private static List<String> ignoredProtocols = Arrays.asList("http", "https", "ftp", "jar",
        "file");

    private ClassLoader classLoader;

    public PaxUrlStreamHandlerFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (ignoredProtocols.contains(protocol)) {
            return null;
        }

        String className = String.format("org.ops4j.pax.url.%s.Handler", protocol);
        Class<?> handlerClass = loadHandlerClass(className);
        if (handlerClass == null) {
            return null;
        }

        try {
            return (URLStreamHandler) handlerClass.newInstance();
        }
        catch (InstantiationException exc) {
            throw new IllegalArgumentException("cannot instantiate " + className, exc);
        }
        catch (IllegalAccessException exc) {
            throw new IllegalArgumentException("cannot instantiate " + className, exc);
        }
    }

    private Class<?> loadHandlerClass(String className) {
        try {
            Class<?> handlerClass = Class.forName(className, true, classLoader);
            return handlerClass;
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }
}
