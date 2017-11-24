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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.impl.BundleArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.StreamReference;
import org.ops4j.pax.exam.container.eclipse.impl.options.AbstractEclipseBundleOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;

/**
 * 
 * @author Christoph Läubrich
 *
 */
public final class P2EclipseBundleOption extends AbstractEclipseBundleOption<P2Bundle>
    implements StreamReference {

    private final String reproName;

    public P2EclipseBundleOption(BundleArtifactInfo<P2Bundle> bundleInfo) {
        super(bundleInfo);
        this.reproName = bundleInfo.getContext().getReproName();
    }

    @Override
    protected Option toOption() {
        URL url = getBundleInfo().getContext().getUrl();
        File cacheFile = P2Cache.getCacheFile(url);
        UrlProvisionOption bundle;
        if (cacheFile.exists()) {
            bundle = CoreOptions.bundle(cacheFile.toURI().toASCIIString());
        }
        else {
            bundle = CoreOptions.bundle(url.toExternalForm());
        }
        bundle.startLevel(getStartLevel());
        bundle.start(shouldStart());
        bundle.update(shouldUpdate());
        return bundle;
    }

    @Override
    public String toString() {
        if (reproName != null) {
            return super.toString() + ":" + reproName;
        }
        return super.toString();
    }

    @Override
    public InputStream createStream() throws IOException {
        return getBundleInfo().getContext().getUrl().openStream();
    }
}
