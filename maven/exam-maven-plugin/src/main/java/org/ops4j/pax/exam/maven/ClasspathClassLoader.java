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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Specialization of URLClassLoader, interpreting a list of Strings as file URLs.
 * 
 * @author Mauro Talevi, Harald Wellmann
 */
public class ClasspathClassLoader extends URLClassLoader {

	public ClasspathClassLoader() {
		this(new URL[] {});
	}

	public ClasspathClassLoader(List<String> classpathElements) throws MalformedURLException {
		this(toClasspathURLs(classpathElements));
	}

	public ClasspathClassLoader(URL[] urls) {
		this(urls, ClasspathClassLoader.class.getClassLoader());
	}

	public ClasspathClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	protected static URL[] toClasspathURLs(List<String> classpathElements)
		throws MalformedURLException {
		List<URL> urls = new ArrayList<URL>();
		if (classpathElements != null) {
			for (String classpathElement : classpathElements) {
				urls.add(new File(classpathElement).toURI().toURL());
			}
		}
		return urls.toArray(new URL[urls.size()]);
	}
}
