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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * A Map for finding {@link ArtifactInfo} of a given symbolic name and version
 * 
 * @author Christoph Läubrich
 *
 * @param <BundleInfoContext>
 */
public class ArtifactInfoMap<BundleInfoContext> {

    private final Map<String, List<ArtifactInfo<BundleInfoContext>>> artifacts = new HashMap<>();

    /**
     * Add a {@link ArtifactInfo} to the Map
     * 
     * @param bundleInfo
     */
    public void add(ArtifactInfo<BundleInfoContext> bundleInfo) {
        List<ArtifactInfo<BundleInfoContext>> list = artifacts.get(bundleInfo.getId());
        if (list == null) {
            list = new ArrayList<>();
            artifacts.put(bundleInfo.getId(), list);
        }
        list.add(bundleInfo);
        Collections.sort(list, Collections.reverseOrder());
    }

    public ArtifactInfo<BundleInfoContext> get(EclipseVersionedArtifact bundle) {
        return get(bundle.getId(), bundle.getVersion());
    }

    /**
     * 
     * @param symbolicName
     * @param version
     * @return the best matching {@link ArtifactInfo} or <code>null</code> if no such is found
     */
    public ArtifactInfo<BundleInfoContext> get(String symbolicName, Version version) {
        List<ArtifactInfo<BundleInfoContext>> list = artifacts.get(symbolicName);
        if (list != null && !list.isEmpty()) {
            for (ArtifactInfo<BundleInfoContext> bundleInfo : list) {
                if (version.equals(bundleInfo.getVersion())) {
                    // perfect match...
                    return bundleInfo;
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param symbolicName
     * @param versionRange
     * @return the bundleinfo with the highest version that matches the given range
     */
    public ArtifactInfo<BundleInfoContext> get(String symbolicName, VersionRange versionRange) {
        List<ArtifactInfo<BundleInfoContext>> list = artifacts.get(symbolicName);
        if (list != null && !list.isEmpty()) {
            for (ArtifactInfo<BundleInfoContext> bundleInfo : list) {
                if (versionRange.includes(bundleInfo.getVersion())) {
                    return bundleInfo;
                }
            }
        }
        return null;
    }

    public List<ArtifactInfo<BundleInfoContext>> getArtifacts() {
        List<ArtifactInfo<BundleInfoContext>> list = new ArrayList<>();
        for (List<ArtifactInfo<BundleInfoContext>> eclipseBundleOption : artifacts.values()) {
            for (ArtifactInfo<BundleInfoContext> bundleInfo : eclipseBundleOption) {
                list.add(bundleInfo);
            }
        }
        Collections.sort(list);
        return list;
    }

    public int size() {
        return artifacts.size();
    }

}
