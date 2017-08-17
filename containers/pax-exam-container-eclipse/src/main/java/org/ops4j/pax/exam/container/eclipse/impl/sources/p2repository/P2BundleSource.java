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
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.BundleArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.BundleArtifactInfo.LazyBoolean;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseBundleSource;
import org.osgi.framework.Constants;
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
    protected EclipseBundleOption getArtifact(BundleArtifactInfo<P2Bundle> info)
        throws IOException {
        return new P2EclipseBundleOption(info);
    }

    public void addBundles(String reproName, Iterable<ArtifactInfo<URL>> artifacts) {
        for (ArtifactInfo<URL> info : artifacts) {
            LazyBundleInfoLoader loader = new LazyBundleInfoLoader(info.getContext());
            if (add(new BundleArtifactInfo<P2Bundle>(info.getId(), info.getVersion(),
                loader.getFragment(), loader.getSingleton(),
                new P2Bundle(info.getContext(), reproName)))) {
                LOG.debug("Add bundle {}:{}:{}",
                    new Object[] { info.getId(), info.getVersion(), reproName });
            }
        }
    }

    private static final class LazyBundleInfoLoader {

        private final URL url;

        private boolean fragment;
        private boolean singleton;
        private boolean loaded;

        public LazyBundleInfoLoader(URL url) {
            this.url = url;
        }

        public LazyBoolean getFragment() {
            return new LazyBoolean() {

                @Override
                public Boolean call() {
                    synchronized (LazyBundleInfoLoader.this) {
                        if (!loaded) {
                            loadBundle();
                        }
                    }
                    return fragment;
                }
            };
        }

        public LazyBoolean getSingleton() {
            return new LazyBoolean() {

                @Override
                public Boolean call() {
                    synchronized (LazyBundleInfoLoader.this) {
                        if (!loaded) {
                            loadBundle();
                        }
                    }
                    return singleton;
                }
            };
        }

        private synchronized void loadBundle() {
            if (!loaded) {
                LOG.debug("Load bundle {}...", url);
                try {
                    try (JarInputStream jar = new JarInputStream(P2Cache.open(url))) {
                        Manifest mf = jar.getManifest();
                        if (mf != null) {
                            Attributes manifest = mf.getMainAttributes();
                            fragment = BundleArtifactInfo.isFragment(manifest);
                            singleton = BundleArtifactInfo.isSingleton(manifest);
                            LOG.debug("... bundle {} is fragemnt = {}, is singleton = {}",
                                new Object[] { manifest.getValue(Constants.BUNDLE_SYMBOLICNAME),
                                    fragment, singleton });
                        }
                        else {
                            LOG.debug("No manifest info found!");
                        }
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException("loading bundle info from url " + url + " failed",
                        e);
                }
                loaded = true;
            }
        }
    }

}
