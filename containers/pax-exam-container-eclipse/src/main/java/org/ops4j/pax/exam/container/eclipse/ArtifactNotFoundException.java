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

/**
 * thrown when a artifact can't be resolved
 * 
 * @author Christoph Läubrich
 */
public final class ArtifactNotFoundException extends FileNotFoundException {

    private static final long serialVersionUID = -2162456041243828303L;

    public ArtifactNotFoundException(EclipseVersionedArtifact artifact) {
        this(artifact.getClass().getSimpleName(), artifact.getId(), artifact.getVersion());
    }

    public ArtifactNotFoundException(String id, Object versionOrRangeOrContext) {
        this("artifact", id, versionOrRangeOrContext);
    }

    public ArtifactNotFoundException(String type, String id, Object versionOrRangeOrContext) {
        this(type + " " + id + ":" + versionOrRangeOrContext + " not found");
    }

    public ArtifactNotFoundException(String s) {
        super(s);
    }

}
