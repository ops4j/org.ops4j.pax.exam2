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
package org.ops4j.pax.exam.container.eclipse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.ops4j.pax.exam.Option;

/**
 * represents a Project inside an Eclipse Workspace, this can eihter be used to load resources from
 * it or convert it to an option to add it to a pax exam test
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseProject {

    /**
     * 
     * @return an Option that provisions the given project
     * @throws IOException
     */
    public Option toOption() throws IOException;

    /**
     * 
     * @param name
     * @return the given resource as an {@link InputStream} that can be used for other purpose.
     * @throws FileNotFoundException
     */
    public InputStream getResourceAsStream(String name) throws FileNotFoundException;

    /**
     * 
     * @param name
     * @return the given resource as an {@link URL} that can be used for other purpose.
     * @throws FileNotFoundException
     */
    public URL getResource(String name) throws FileNotFoundException;
}
