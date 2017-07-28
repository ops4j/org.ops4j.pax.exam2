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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseOptions.CombinedEclipseArtifactSource;
import org.ops4j.pax.exam.container.eclipse.EclipseProject;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * Combines basic {@link EclipseArtifactSource}s base don the givne types
 * 
 * @author Christoph Läubrich
 *
 */
public final class CombinedSource implements CombinedEclipseArtifactSource {

    // TODO make this more useful and compact, e.g. using reflection, so we can choose what
    // interfaces are implemented, and only throw the exception with suppressed exception if we have
    // more than one source

    private final EclipseArtifactSource[] sources;

    public CombinedSource(Collection<EclipseArtifactSource> sources) {
        this.sources = sources.toArray(new EclipseArtifactSource[0]);
    }

    @Override
    public EclipseBundleOption bundle(String bundleName) throws IOException, FileNotFoundException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "bundle " + bundleName + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseBundleSource) {
                    return ((EclipseBundleSource) source).bundle(bundleName);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public EclipseBundleOption bundle(String bundleSymbolicName, Version bundleVersion)
        throws IOException, ArtifactNotFoundException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "bundle " + bundleSymbolicName + ":" + bundleVersion + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseBundleSource) {
                    return ((EclipseBundleSource) source).bundle(bundleSymbolicName, bundleVersion);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, VersionRange bundleVersionRange)
        throws IOException, ArtifactNotFoundException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "bundle " + bundleName + ":" + bundleVersionRange + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseBundleSource) {
                    return ((EclipseBundleSource) source).bundle(bundleName, bundleVersionRange);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public EclipseFeatureOption feature(String featureName) throws IOException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "feature " + featureName + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseFeatureSource) {
                    return ((EclipseFeatureSource) source).feature(featureName);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public EclipseFeatureOption feature(String featureName, VersionRange featureVersionRange)
        throws IOException, ArtifactNotFoundException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "feature " + featureName + ":" + featureVersionRange + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseFeatureSource) {
                    return ((EclipseFeatureSource) source).feature(featureName,
                        featureVersionRange);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public EclipseFeatureOption feature(String featureName, Version featureVersion)
        throws IOException, ArtifactNotFoundException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "feature " + featureName + ":" + featureVersion + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseFeatureSource) {
                    return ((EclipseFeatureSource) source).feature(featureName, featureVersion);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public EclipseProject project(String projectName) throws ArtifactNotFoundException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "project " + projectName + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseProjectSource) {
                    return ((EclipseProjectSource) source).project(projectName);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public EclipseInstallableUnit unit(String id) throws IOException, ArtifactNotFoundException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "unit " + id + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseUnitSource) {
                    return ((EclipseUnitSource) source).unit(id);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public EclipseInstallableUnit unit(String id, VersionRange versionRange)
        throws IOException, ArtifactNotFoundException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "unit " + id + ":" + versionRange + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseUnitSource) {
                    return ((EclipseUnitSource) source).unit(id, versionRange);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public EclipseInstallableUnit unit(String id, Version version)
        throws IOException, ArtifactNotFoundException {
        ArtifactNotFoundException fnfe = new ArtifactNotFoundException(
            "unit " + id + ":" + version + " not found in any sources");
        for (EclipseArtifactSource source : sources) {
            try {
                if (source instanceof EclipseUnitSource) {
                    return ((EclipseUnitSource) source).unit(id, version);
                }
            }
            catch (ArtifactNotFoundException ef) {
                fnfe.addSuppressed(ef);
            }
        }
        throw fnfe;
    }

    @Override
    public Collection<EclipseInstallableUnit> getAllUnits() throws IOException {
        ArrayList<EclipseInstallableUnit> result = new ArrayList<>();
        for (EclipseArtifactSource source : sources) {
            if (source instanceof EclipseUnitSource) {
                result.addAll(((EclipseUnitSource) source).getAllUnits());
            }
        }
        return result;
    }
}
