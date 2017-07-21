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

import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * A Map for finding {@link BundleInfo} of a given symbolic name and version
 * 
 * @author Christoph Läubrich
 *
 * @param <BundleInfoContext>
 */
public class BundleInfoMap<BundleInfoContext> {

    private final Map<String, List<BundleInfo<BundleInfoContext>>> bundles = new HashMap<>();

    /**
     * Add a {@link BundleInfo} to the Map
     * 
     * @param bundleInfo
     */
    public void add(BundleInfo<BundleInfoContext> bundleInfo) {
        List<BundleInfo<BundleInfoContext>> list = bundles.get(bundleInfo.getSymbolicName());
        if (list == null) {
            list = new ArrayList<>();
            bundles.put(bundleInfo.getSymbolicName(), list);
        }
        list.add(bundleInfo);
        Collections.sort(list, Collections.reverseOrder());
    }

    public BundleInfo<BundleInfoContext> get(EclipseBundle bundle, boolean strict) {
        return get(bundle.getSymbolicName(), bundle.getVersion(), strict);
    }

    /**
     * 
     * @param symbolicName
     * @param version
     * @return the best matching {@link BundleInfo} or <code>null</code> if no such is found
     */
    public BundleInfo<BundleInfoContext> get(String symbolicName, Version version, boolean strict) {
        List<BundleInfo<BundleInfoContext>> list = bundles.get(symbolicName);
        if (list != null && !list.isEmpty()) {
            if (version == null || Version.emptyVersion.equals(version)) {
                return Collections.max(list);
            }
            for (BundleInfo<BundleInfoContext> bundleInfo : list) {
                if (version.equals(bundleInfo.getVersion())) {
                    // perfect match...
                    return bundleInfo;
                }
            }
            if (!strict) {
                // now try to find one without qualifier then...
                String base = version.getMajor() + "." + version.getMinor() + ".";
                String lower = base + version.getMicro();
                String upper = base + version.getMicro() + 1;
                return get(symbolicName, VersionRange.valueOf("[" + lower + "," + upper + ")"));
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
    public BundleInfo<BundleInfoContext> get(String symbolicName, VersionRange versionRange) {
        List<BundleInfo<BundleInfoContext>> list = bundles.get(symbolicName);
        if (list != null && !list.isEmpty()) {
            for (BundleInfo<BundleInfoContext> bundleInfo : list) {
                if (versionRange.includes(bundleInfo.getVersion())) {
                    return bundleInfo;
                }
            }
        }
        return null;
    }

    /**
     * @return a map view of this BundleInfo
     */
    public Map<String, List<BundleInfo<BundleInfoContext>>> asMap() {
        return Collections.unmodifiableMap(bundles);
    }

}
