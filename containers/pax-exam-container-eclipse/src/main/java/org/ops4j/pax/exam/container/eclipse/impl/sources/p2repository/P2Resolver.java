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

import org.ops4j.pax.exam.container.eclipse.EclipseRepository;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureAndUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.unit.UnitResolver;

/**
 * Reads the information of a single repository and provides this as a source, see
 * {@link UnitResolver} for a way of using this to resolve dependent information
 * 
 * @author Christoph Läubrich
 *
 */
public class P2Resolver extends BundleAndFeatureAndUnitSource implements EclipseRepository {

    private final P2ArtifactRepository artifactRepository;
    private final P2MetadataRepository metadataRepository;
    private final String name;
    private final URL url;

    public P2Resolver(String name, URL url) throws IOException {
        this.name = name;
        this.url = url;
        P2Index p2Index = new P2Index(url);
        artifactRepository = new P2ArtifactRepository(name, p2Index.getArtifactRepository());
        metadataRepository = new P2MetadataRepository(name, p2Index.getMetadataRepository(),
            artifactRepository);
    }

    public URL getUrl() {
        return url;
    }

    @Override
    protected EclipseUnitSource getUnitSource() {
        return metadataRepository;
    }

    @Override
    protected EclipseBundleSource getBundleSource() {
        return artifactRepository;
    }

    @Override
    protected EclipseFeatureSource getFeatureSource() {
        return artifactRepository;
    }

    @Override
    public String getName() {
        return name;
    }

}
