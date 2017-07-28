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

import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseBundleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bundles of a P2 repro
 * 
 * @author Christoph Läubrich
 *
 */
public class P2BundleSource extends AbstractEclipseBundleSource<P2Bundle> {

    private static final Logger LOG = LoggerFactory.getLogger(P2BundleSource.class);

    @Override
    protected EclipseBundleOption getArtifact(ArtifactInfo<P2Bundle> info) throws IOException {
        return new P2EclipseBundleOption(info);
    }

    public void addBundles(String reproName, ArtifactInfoMap<URL> artifacts) {
        for (ArtifactInfo<URL> info : artifacts.getArtifacts()) {
            if (add(new ArtifactInfo<P2Bundle>(info, new P2Bundle(info.getContext(), reproName)))) {
                LOG.info("Add bundle {}:{}:{}",
                    new Object[] { info.getId(), info.getVersion(), reproName });
            }
        }
    }

}
