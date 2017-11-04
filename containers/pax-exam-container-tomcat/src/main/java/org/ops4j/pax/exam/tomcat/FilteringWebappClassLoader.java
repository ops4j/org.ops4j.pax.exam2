/*
 * Copyright 2017 OPS4J Contributors
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
package org.ops4j.pax.exam.tomcat;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.catalina.loader.WebappClassLoader;

/**
 * @author Harald Wellmann
 *
 */
public class FilteringWebappClassLoader extends WebappClassLoader {


    public FilteringWebappClassLoader() {
    }

    public FilteringWebappClassLoader(ClassLoader cl) {
        super();
    }



    @Override
    public URL[] getURLs() {
        URL[] urls = super.getURLs();
        List<URL> filteredUrls = new ArrayList<>();
        for (URL url : urls) {
            if (!url.toString().contains("/target/")) {
                filteredUrls.add(url);
            }
        }
        return filteredUrls.toArray(new URL[filteredUrls.size()]);
    }

    @Override
    public URL getResource(String name) {
        URL url = super.getResource(name);
        if (name.contains("/beans.xml")) {
            if (url != null && !url.toString().contains("Pax-Exam-Probe")) {
                return null;
            }
        }
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> e = super.getResources(name);
        if (e == null || !name.contains("/beans.xml")) {
            return e;
        }

        List<URL> urls = new ArrayList<>();
        for (URL url : Collections.list(e)) {
            if (url.toString().contains("Pax-Exam-Probe")) {
                urls.add(url);
            }
        }
        return Collections.enumeration(urls);
    }
}
