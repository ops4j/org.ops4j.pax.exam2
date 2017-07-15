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

import org.osgi.framework.Version;

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
    }

    /**
     * 
     * @param symbolicName
     * @param version
     * @return the best matching {@link BundleInfo} or <code>null</code> if no such is found
     */
    public BundleInfo<BundleInfoContext> get(String symbolicName, Version version) {
        List<BundleInfo<BundleInfoContext>> list = bundles.get(symbolicName);
        if (list != null) {
            if (version == null || Version.emptyVersion.equals(version)) {
                return Collections.max(list);
            }
            BundleInfo<BundleInfoContext> mostlyMatch = null;
            for (BundleInfo<BundleInfoContext> bundleInfo : list) {
                if (version.equals(bundleInfo.getVersion())) {
                    // perfect match...
                    return bundleInfo;
                }
                else if (matchWithoutQualifier(version, bundleInfo)) {
                    if (mostlyMatch != null
                        && mostlyMatch.getVersion().compareTo(bundleInfo.getVersion()) > 0) {
                        continue;
                    }
                    mostlyMatch = bundleInfo;
                }
            }
            return mostlyMatch;
        }
        else {
            return null;
        }
    }

    private boolean matchWithoutQualifier(Version version, BundleInfo<BundleInfoContext> bundleInfo) {
        if (version.getMajor() == bundleInfo.getVersion().getMajor()) {
            if (version.getMinor() == bundleInfo.getVersion().getMinor()) {
                if (version.getMicro() == bundleInfo.getVersion().getMicro()) {
                    return true;
                }
            }
        }
        return false;
    }

    public BundleInfoContext getContext(String symbolicName, Version version) {
        BundleInfo<BundleInfoContext> bundleInfo = get(symbolicName, version);
        if (bundleInfo == null) {
            return null;
        }
        return bundleInfo.getContext();
    }

}
