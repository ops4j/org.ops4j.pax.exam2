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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.exam.container.eclipse.EclipseApplicationOption;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseOptions;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProductParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProductParser.PluginConfiguration;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProductParser.ProductEclipseBundle;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProductParser.ProductEclipseFeature;

/**
 * Builder for creating launchers from products
 * 
 * @author Christoph Läubrich
 *
 */
public class EclipseProductBuilder
    extends EclipseLauncherBuilder<EclipseApplicationOption, EclipseProductBuilder> {

    private final ProductParser parser;

    public EclipseProductBuilder(ProductParser productParser) {
        this.parser = productParser;
    }

    @Override
    public EclipseApplicationOption create() throws IOException {
        EclipseProvision provision = super.provision();
        if (parser.useFeatures()) {
            for (ProductEclipseFeature feature : parser.getFeatures()) {
                configure(provision.feature(feature), parser.getConfiguration());
            }
        }
        else {
            for (ProductEclipseBundle bundle : parser.getPlugins()) {
                configure(provision.bundle(bundle), parser.getConfiguration());
            }
        }
        String productID = parser.getProductID();
        String application = parser.getApplication();
        return EclipseOptions.launcher(provision, isForked()).product(productID)
            .application(application);
    }

    private void configure(List<EclipseBundleOption> bundles,
        Map<String, PluginConfiguration> configuration) {
        for (EclipseBundleOption bundle : bundles) {
            PluginConfiguration cfg = configuration.get(bundle.getId());
            if (cfg == null) {
                bundle.start(false);
            }
            else {
                bundle.start(cfg.autoStart);
                if (cfg.startLevel > 0) {
                    bundle.startLevel(cfg.startLevel);
                }
            }
        }
    }

}
