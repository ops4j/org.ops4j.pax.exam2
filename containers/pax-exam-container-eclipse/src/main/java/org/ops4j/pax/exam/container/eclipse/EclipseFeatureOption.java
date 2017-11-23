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
package org.ops4j.pax.exam.container.eclipse;

import java.util.List;

/**
 * Option that adds a feature to pax exam, take care that in general a feature can't be installed
 * directly inside an Eclipse Framework, use
 * {@link EclipseProvision#feature(String, org.osgi.framework.Version)} if you want to add the
 * bundles(!) of a feature to your run
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseFeatureOption extends EclipseFeature, EclipseVersionedArtifactOption {

    public List<EclipseFeatureBundle> getBundles();

    public List<EclipseFeature> getIncluded();

    public static interface EclipseFeatureBundle extends EclipseBundle {

        boolean isFragment();

        boolean isUnpack();

        boolean matches(EclipseEnvironment environment);
    }
}
