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

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Interface that represents an entry in a P2 index
 * 
 * @author Christoph Läubrich
 *
 */
public interface P2RepositoryFile {

    P2Index getIndex();

    boolean isComposite();

    boolean isRepository();

    boolean isArtifactRepository();

    boolean isMetadataRepository();

    String getType();

    List<P2RepositoryFile> getChilds() throws IOException;

    Element getRespository() throws IOException;

    URL getURL();

    long getLastModified();

}
