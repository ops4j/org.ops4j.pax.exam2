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

import java.net.URL;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.AbstractEclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;

/**
 * 
 * @author Christoph Läubrich
 *
 */
public final class RepositoryEclipseBundleOption extends AbstractEclipseBundleOption<URL> {

    private final EclipseUnitSource location;

    public RepositoryEclipseBundleOption(ArtifactInfo<URL> bundleInfo, EclipseUnitSource location) {
        super(bundleInfo);
        this.location = location;
    }

    @Override
    protected Option toOption(ArtifactInfo<URL> bundleInfo) {
        return CoreOptions.bundle(bundleInfo.getContext().toExternalForm());
    }

    @Override
    public String toString() {
        return super.toString() + ":" + location;
    }
}
