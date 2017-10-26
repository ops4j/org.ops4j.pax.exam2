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
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.CopyFilesOption;
import org.ops4j.pax.exam.container.eclipse.EclipseApplicationOption;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseDirectoryLayout;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallation;
import org.ops4j.pax.exam.container.eclipse.EclipseOptions;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision;
import org.ops4j.pax.exam.container.eclipse.LauncherOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.SimpleConfigurationParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.SimpleConfigurationParser.RuntimeOption;

/**
 * 
 * 
 * Provisions from an installation folder
 * 
 * @author Christoph Läubrich
 *
 */
public class EclipseInstallationBuilder
    extends EclipseLauncherBuilder<EclipseApplicationOption, EclipseInstallationBuilder> {

    private static final String CONFIG_INI = "config.ini";
    private final List<Option> options;

    public EclipseInstallationBuilder(EclipseInstallation installation) throws IOException {
        options = new ArrayList<>();
        File baseFolder = installation.getLayout().getBaseFolder();
        options.add(new CopyFilesOption() {

            @Override
            public void copyTo(EclipseDirectoryLayout folder) throws IOException {
                FileUtils.copyDirectory(baseFolder, folder.getBaseFolder(), new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        // since we parse the config ini and pass over these setting to pas exam
                        // we can ignore it here to prevent conflicts
                        return !pathname.getName().equalsIgnoreCase(CONFIG_INI);
                    }
                });
            }
        });
        // try to find the launcher ini...
        File[] files = baseFolder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if ("exe".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
                File iniFile = new File(file.getParentFile(),
                    FilenameUtils.getBaseName(file.getName()) + ".ini");
                if (iniFile.exists()) {
                    parseLauncherIni(iniFile, options);
                    break;
                }
            }
        }
        File configIni = new File(installation.getLayout().getConfigurationFolder(), CONFIG_INI);
        if (configIni.exists()) {
            String sc = parseConfigIni(configIni, options);
            if (sc != null) {
                File configurationFolder = installation.getLayout().getConfigurationFolder();
                URL url = new URL(configurationFolder.toURI().toURL(), sc);
                SimpleConfigurationParser parser = new SimpleConfigurationParser(url.openStream());
                List<ArtifactInfo<RuntimeOption>> items = parser.getItems();
                for (ArtifactInfo<RuntimeOption> info : items) {
                    EclipseBundleOption bundle = installation.bundle(info.getId(),
                        info.getVersion());
                    bundle.start(info.getContext().isStart());
                    int sl = info.getContext().getStartlevel();
                    bundle.startLevel(sl);
                    if (sl > -1) {
                        options.add(bundle);
                    }
                }
            }
        }
    }

    @Override
    public EclipseApplicationOption create() throws IOException {
        EclipseProvision provision = provision();
        for (Option option : options) {
            provision.option(option);
        }
        return EclipseOptions.launcher(provision, isForked()).product(null).application(null);
    }

    private static String parseConfigIni(File file, List<Option> options) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream fin = new FileInputStream(file)) {
            properties.load(fin);
        }
        Set<String> names = properties.stringPropertyNames();
        String simpleConfigurator = null;
        for (String key : names) {
            String value = properties.getProperty(key);
            if ("org.eclipse.equinox.simpleconfigurator.configUrl".equals(key)) {
                simpleConfigurator = value;
                value = value + ".off";
            }
            options.add(CoreOptions.frameworkProperty(key).value(value));
        }
        return simpleConfigurator;
    }

    private static void parseLauncherIni(File file, List<Option> options) throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file)))) {
            String line;
            boolean vmfound = false;
            String key = null;
            while ((line = reader.readLine()) != null) {
                if (vmfound) {
                    if (line.startsWith("-D")) {
                        // transform Systemproperty VM Options into regular options so they can be
                        // used in non forked mode...
                        int indexOf = line.indexOf('=');
                        if (indexOf > -1) {
                            options.add(CoreOptions.systemProperty(line.substring(2, indexOf))
                                .value(line.substring(indexOf + 1)));
                        }
                        else {
                            options.add(CoreOptions.systemProperty(line));
                        }
                    }
                    else {
                        options.add(CoreOptions.vmOption(line));
                    }
                }
                else if ("-vmargs".equals(line)) {
                    vmfound = true;
                }
                else if (key == null) {
                    key = line;
                }
                else {
                    options.add(new LauncherOption(key, line));
                    key = null;
                }
            }
        }
    }

}
