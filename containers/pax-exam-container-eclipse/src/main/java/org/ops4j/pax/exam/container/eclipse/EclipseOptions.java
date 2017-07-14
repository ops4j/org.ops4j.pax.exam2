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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.impl.EclipseApplicationImpl;
import org.ops4j.pax.exam.container.eclipse.impl.InstallationEclipseBundleSource;
import org.ops4j.pax.exam.options.ProvisionControl;

/**
 * Static Options to configure the EclipseContainer
 * 
 * @author Christoph Läubrich
 *
 */
public class EclipseOptions {

    private static final class EclipseLauncherImpl implements EclipseLauncher {

        private final boolean forked;

        private EclipseLauncherImpl(boolean forked) {
            this.forked = forked;
        }

        @Override
        public boolean isForked() {
            return forked;
        }

        @Override
        public EclipseApplication ignoreApp() {
            return new EclipseApplicationImpl(this, true,
                CoreOptions.frameworkProperty("eclipse.ignoreApp").value(true));
        }

        @Override
        public EclipseApplication application(String applicationID) {
            return new EclipseApplicationImpl(this, false,
                CoreOptions.frameworkProperty("eclipse.application").value(applicationID));
        }

        @Override
        public EclipseProduct product(InputStream productFile) {
            throw new UnsupportedOperationException("not implmented yet, sorry :-(");
        }

        @Override
        public EclipseProduct product(final String productID) {
            return new EclipseProduct() {

                @Override
                public EclipseApplication application(String applicationID) {
                    return new EclipseApplicationImpl(EclipseLauncherImpl.this, false,
                        CoreOptions.frameworkProperty("eclipse.application").value(applicationID),
                        CoreOptions.frameworkProperty("eclipse.product").value(productID));
                }
            };
        }

    }

    public static EclipseLauncher launcher(final boolean forked) {
        return new EclipseLauncherImpl(forked);
    }

    /**
     * Uses a Installation-Folder to provision bundles from
     * 
     * @param folder
     * @return
     */
    public static EclipseBundleSource fromInstallation(final File baseFolder) {
        return new InstallationEclipseBundleSource(baseFolder);
    }

    /**
     * Use an Eclipse-WOrkspace to provision bundles from
     * 
     * @param workspaceFolder
     * @return
     * @throws IOException
     */
    public static EclipseBundleSource fromWorkspace(final File workspaceFolder) throws IOException {
        // return new WorkspaceEclipseBundleSource(workspaceFolder);
        throw new UnsupportedOperationException("not yet implemented, sorry");
    }

    public static EclipseBundleSource withFallback(final EclipseBundleSource primary,
        final EclipseBundleSource... fallbacks) {
        return new EclipseBundleSource() {

            @Override
            public Option resolve(String bundleName, String bundleVersion, String bundleFile)
                throws IOException, FileNotFoundException {
                FileNotFoundException fnfe = new FileNotFoundException(
                    "bundle not found in any sources");
                try {
                    return primary.resolve(bundleName, bundleVersion, bundleFile);
                }
                catch (FileNotFoundException ep) {
                    fnfe.addSuppressed(ep);
                    for (EclipseBundleSource fallback : fallbacks) {
                        try {
                            return fallback.resolve(bundleName, bundleVersion, bundleFile);
                        }
                        catch (FileNotFoundException ef) {
                            fnfe.addSuppressed(ef);
                        }
                    }
                }
                throw fnfe;
            }

            @Override
            public Option resolve(String bundleName, String bundleVersion)
                throws IOException, FileNotFoundException {
                FileNotFoundException fnfe = new FileNotFoundException(
                    "bundle not found in any sources");
                try {
                    return primary.resolve(bundleName, bundleVersion);
                }
                catch (FileNotFoundException ep) {
                    fnfe.addSuppressed(ep);
                    for (EclipseBundleSource fallback : fallbacks) {
                        try {
                            return fallback.resolve(bundleName, bundleVersion);
                        }
                        catch (FileNotFoundException ef) {
                            fnfe.addSuppressed(ef);
                        }
                    }
                }
                throw fnfe;
            }

        };
    }

    public static EclipseProvision provision(final EclipseBundleSource bundleSource) {
        return new EclipseProvision() {

            @Override
            public Option simpleconfigurator(InputStream bundleFile) throws IOException {
                // We parse the file and add bundle(...) Options, later we might just provision the
                // simple configurator, but then we must find a way to fetch/install it ...
                final List<Option> bundles = new ArrayList<>();
                try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(bundleFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("#") || line.trim().isEmpty()) {
                            continue;
                        }
                        String[] bundleInfo = line.split(",");
                        if (bundleInfo[0].equals("org.eclipse.osgi")
                            || bundleInfo[0].equals("org.eclipse.equinox.simpleconfigurator")) {
                            // we don't want to load the configurator or eclipse itself...
                            continue;
                        }
                        Option bundle = bundleSource.resolve(bundleInfo[0], bundleInfo[1],
                            bundleInfo[2]);
                        if (bundle instanceof ProvisionControl<?>) {
                            ProvisionControl<?> control = (ProvisionControl<?>) bundle;
                            control.startLevel(Integer.parseInt(bundleInfo[3]));
                            control.start(Boolean.valueOf(bundleInfo[4]));
                        }
                        bundles.add(bundle);
                    }
                }
                return CoreOptions.composite(bundles.toArray(new Option[0]));
            }
        };
    }

}
