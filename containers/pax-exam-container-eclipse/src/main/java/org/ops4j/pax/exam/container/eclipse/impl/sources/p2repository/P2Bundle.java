/*
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
package org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository;

import java.net.URL;

/**
 * A bundle in the repository
 * 
 * @author Christoph Läubrich
 *
 */
public class P2Bundle {

    private final URL url;
    private final String reproName;

    public P2Bundle(URL url, String reproName) {
        this.url = url;
        this.reproName = reproName;
    }

    public URL getUrl() {
        return url;
    }

    public String getReproName() {
        return reproName;
    }
}
