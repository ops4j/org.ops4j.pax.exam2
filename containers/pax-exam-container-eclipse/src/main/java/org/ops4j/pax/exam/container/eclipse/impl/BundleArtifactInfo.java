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
package org.ops4j.pax.exam.container.eclipse.impl;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * Artifact Info that holds information if a bundle is a fragment or singleton
 * 
 * @author Christoph Läubrich
 *
 * @param <Context>
 */
public class BundleArtifactInfo<Context> extends ArtifactInfo<Context> implements EclipseBundle {

    private final LazyBoolean fragment;
    private final LazyBoolean singleton;

    public BundleArtifactInfo(Attributes attributes, Context context) {
        super(attributes, context);
        singleton = LazyBoolean.of(isSingleton(attributes));
        fragment = LazyBoolean.of(isFragment(attributes));
    }

    public BundleArtifactInfo(BundleArtifactInfo<?> info, Context context) {
        this(info.getId(), info.getVersion(), new CachedLazyBoolean() {

            @Override
            protected boolean compute() {
                return info.isFragment();
            }
        }, new CachedLazyBoolean() {

            @Override
            protected boolean compute() {
                return info.isSingleton();
            }
        }, context);
    }

    public BundleArtifactInfo(EclipseBundleOption bundle, Context context) {
        this(bundle.getId(), bundle.getVersion(), new CachedLazyBoolean() {

            @Override
            protected boolean compute() {
                return bundle.isFragment();
            }

        }, new CachedLazyBoolean() {

            @Override
            protected boolean compute() {
                return bundle.isSingleton();
            }
        }, context);
    }

    public BundleArtifactInfo(Manifest manifest, Context context) {
        this(manifest.getMainAttributes(), context);
    }

    public BundleArtifactInfo(String symbolicName, Version version, boolean fragment,
        boolean singleton, Context context) {
        this(symbolicName, version, LazyBoolean.of(fragment), LazyBoolean.of(singleton), context);
    }

    public BundleArtifactInfo(String symbolicName, Version version, LazyBoolean fragment,
        LazyBoolean singleton, Context context) {
        super(symbolicName, version, context);
        this.fragment = fragment;
        this.singleton = singleton;
    }

    @Override
    public boolean isFragment() {
        return fragment.call();
    }

    public boolean isSingleton() {
        return singleton.call();
    }

    public static boolean isBundle(File folder) {
        if (new File(folder, ArtifactInfo.MANIFEST_LOCATION).exists()) {
            try {
                return isBundle(readManifest(folder));
            }
            catch (IOException e) {
                // not a valid bundle then...
            }
        }
        return false;
    }

    public static boolean isBundle(Manifest manifest) {
        if (manifest != null) {
            Attributes attributes = manifest.getMainAttributes();
            return attributes.containsKey(new Attributes.Name(Constants.BUNDLE_SYMBOLICNAME))
                && attributes.containsKey(new Attributes.Name(Constants.BUNDLE_VERSION));
        }
        return false;
    }

    public static boolean isFragment(Attributes attributes) {
        return !string(attributes, Constants.FRAGMENT_HOST).isEmpty();
    }

    public static boolean isSingleton(Attributes attributes) {
        String[] split = notNull(attributes, Constants.BUNDLE_SYMBOLICNAME).split(";", 2);
        return split.length == 2 && split[1].replace("\r", "").replace("\n", "").replace("\t", "")
            .replace(" ", "").contains("singleton:=true");
    }

    public static <T> BundleArtifactInfo<T> readExplodedBundle(File folder, T context)
        throws IOException {
        return new BundleArtifactInfo<T>(readManifest(folder), context);
    }

    public static abstract class LazyBoolean implements Callable<Boolean> {

        private static final LazyBoolean TRUE = new LazyBoolean() {

            @Override
            public Boolean call() {
                return Boolean.TRUE;
            }
        };
        private static final LazyBoolean FALSE = new LazyBoolean() {

            @Override
            public Boolean call() {
                return Boolean.FALSE;
            }
        };

        @Override
        public abstract Boolean call();

        public static LazyBoolean of(boolean bool) {
            return bool ? TRUE : FALSE;
        }

        public static LazyBoolean of(Boolean bool) {
            return bool != null && bool.booleanValue() ? TRUE : FALSE;
        }

    }

    private static abstract class CachedLazyBoolean extends LazyBoolean {

        private Boolean value;

        @Override
        public Boolean call() {
            if (value == null) {
                value = compute();
            }
            return value;
        }

        protected abstract boolean compute();

    }

}
