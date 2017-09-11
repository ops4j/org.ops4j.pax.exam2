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
package org.ops4j.pax.exam.container.eclipse.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseLauncher;
import org.ops4j.pax.exam.container.eclipse.EclipseOptions;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision;
import org.ops4j.pax.exam.container.eclipse.impl.BundleArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.AbstractParser;
import org.osgi.framework.Version;

/**
 * 
 * 
 * Provisions bundles from a bundle file as defined by the
 * https://wiki.eclipse.org/Configurator#SimpleConfigurator
 * 
 * @author Christoph Läubrich
 *
 */
public class SimpleConfiguratorBuilder
    extends EclipseLauncherBuilder<EclipseLauncher, SimpleConfiguratorBuilder> {

    private final List<String> lines = new ArrayList<>();

    public SimpleConfiguratorBuilder(InputStream bundleFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(bundleFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                lines.add(line);
            }
        }
    }

    @Override
    public EclipseLauncher create() throws IOException {
        EclipseProvision provision = provision();
        for (String line : lines) {
            String[] bundleInfo = line.split(",");
            String bsn = bundleInfo[0];
            Version version = AbstractParser.stringToVersion(bundleInfo[1]);
            List<EclipseBundleOption> bundles = provision
                .bundle(new BundleArtifactInfo<Void>(bsn, version, false, false, null));
            for (EclipseBundleOption bundle : bundles) {
                int sl = Integer.parseInt(bundleInfo[3]);
                Boolean start = Boolean.valueOf(bundleInfo[4]);
                bundle.start(start);
                bundle.startLevel(sl);
            }
        }
        return EclipseOptions.launcher(provision, isForked());
    }

}
