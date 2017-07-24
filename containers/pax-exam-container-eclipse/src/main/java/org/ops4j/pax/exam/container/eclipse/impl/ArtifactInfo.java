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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * Represents the information of a resolvable bundle, it also carry some context that can be used to
 * specify the bundle further
 * 
 * @author Christoph Läubrich
 *
 * @param <Context>
 */
public class ArtifactInfo<Context>
    implements Comparable<ArtifactInfo<Context>>, EclipseVersionedArtifact {

    public static final String MANIFEST_LOCATION = "META-INF/MANIFEST.MF";

    public static final String FEATURE_XML_LOCATION = "feature.xml";

    private final String symbolicName;
    private final Context context;
    private final Version version;

    public ArtifactInfo(Manifest manifest, Context context) {
        this(manifest.getMainAttributes(), context);
    }

    public ArtifactInfo(Attributes attributes, Context context) {
        this(notNull(attributes, Constants.BUNDLE_SYMBOLICNAME).split(";")[0],
            Version.parseVersion(notNull(attributes, Constants.BUNDLE_VERSION).split(";")[0]),
            context);
    }

    public ArtifactInfo(EclipseVersionedArtifact bundle, Context context) {
        this(bundle.getId(), bundle.getVersion(), context);
    }

    public ArtifactInfo(String symbolicName, Version version, Context context) {
        this.symbolicName = symbolicName;
        this.version = version;
        this.context = context;
    }

    @Override
    public final String getId() {
        return symbolicName;
    }

    @Override
    public final Version getVersion() {
        return version;
    }

    public final Context getContext() {
        return context;
    }

    @Override
    public int compareTo(ArtifactInfo<Context> o) {
        int cmp = symbolicName.compareTo(o.symbolicName);
        if (cmp == 0) {
            return version.compareTo(o.version);
        }
        return cmp;
    }

    private static String notNull(Attributes attributes, String attrName) {
        String value = attributes.getValue(attrName);
        if (value == null) {
            throw new IllegalArgumentException(
                "Header-Name " + attrName + " not found in Manifest!");
        }
        return value;
    }

    @Override
    public String toString() {
        if (context == null) {
            return symbolicName + ":" + version;
        }
        else {
            return symbolicName + ":" + version + ":" + context;
        }
    }

    public static boolean isBundle(Manifest manifest) {
        if (manifest != null) {
            Attributes attributes = manifest.getMainAttributes();
            return attributes.containsKey(new Attributes.Name(Constants.BUNDLE_SYMBOLICNAME))
                && attributes.containsKey(new Attributes.Name(Constants.BUNDLE_VERSION));
        }
        return false;
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

    public static Manifest readManifest(File folder) throws IOException {
        File metaInf = new File(folder, "META-INF");
        if (metaInf.exists()) {
            try (FileInputStream is = new FileInputStream(new File(metaInf, "MANIFEST.MF"))) {
                Manifest manifest = new Manifest(is);
                return manifest;
            }
        }
        else {
            throw new FileNotFoundException(
                "can't find folder META-INF in folder " + folder.getAbsolutePath());
        }
    }

    public static <T> ArtifactInfo<T> readExplodedBundle(File folder, T context)
        throws IOException {
        return new ArtifactInfo<T>(readManifest(folder), context);
    }

}
