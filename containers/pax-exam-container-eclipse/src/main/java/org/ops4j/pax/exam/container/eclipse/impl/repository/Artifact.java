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
package org.ops4j.pax.exam.container.eclipse.impl.repository;

import java.io.Serializable;

import org.osgi.framework.Version;

/**
 * Container class that holds artifact information of a repro
 * 
 * @author Christoph Läubrich
 *
 */
public final class Artifact extends VersionSerializable
    implements EclipseClassifiedVersionedArtifact, Serializable {

    private static final long serialVersionUID = -7368156518818852071L;
    private final String id;
    private final String classifier;

    public Artifact(String id, Version version, String classifier) {
        super(version);
        this.id = id;
        this.classifier = classifier;
    }

    @Override
    public String toString() {
        return "Artifact:" + id + ":" + getVersion() + ":" + classifier;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

}
