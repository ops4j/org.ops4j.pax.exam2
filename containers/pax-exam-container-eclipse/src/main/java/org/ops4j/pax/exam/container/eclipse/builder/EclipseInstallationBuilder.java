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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.CopyFilesOption;
import org.ops4j.pax.exam.container.eclipse.EclipseApplicationOption;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseBundleSource;
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

    private static final byte[] ZIP_SIGNATURE = { 0x50, 0x4B, 0x03, 0x04 };

    private static final byte[] RAR_SIGNATURE = { 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00 };

    private static final byte[] TAR_SIGNATURE = { 0x75, 0x73, 0x74, 0x61, 0x72 };

    private static final byte[] GZ_SIGNATURE = { 0x1F, (byte) (0x8B & 0xFF) };

    private static final int READ_LIMIT_AUTO_DETECT = 10;
    private static final String CONFIG_INI = "config.ini";
    private final List<Option> options = new ArrayList<>();

    public EclipseInstallationBuilder(InputStream stream) throws IOException {
        parseStream(stream);
    }

    private void parseStream(InputStream stream) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream, READ_LIMIT_AUTO_DETECT);
        }
        stream.mark(10);
        byte[] buffer = new byte[READ_LIMIT_AUTO_DETECT];
        IOUtils.read(stream, buffer);
        stream.reset();
        if (hasSignature(buffer, ZIP_SIGNATURE)) {
            File tempFolder = File.createTempFile("paxexam", "explode");
            tempFolder.deleteOnExit();
            FileUtils.forceDelete(tempFolder);
            FileUtils.forceMkdir(tempFolder);
            try (ZipInputStream zip = new ZipInputStream(stream)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName();
                    File file = new File(tempFolder, name);
                    if (!file.toPath().normalize().startsWith(tempFolder.toPath().normalize())) {
                        throw new IOException("Bad zip entry");
                    }
                    file.deleteOnExit();
                    if (entry.isDirectory()) {
                        FileUtils.forceMkdir(file);
                        continue;
                    }
                    try (FileOutputStream output = new FileOutputStream(file)) {
                        IOUtils.copy(zip, output);
                    }
                }
            }
            File[] list;
            while (tempFolder.isDirectory() && (list = tempFolder.listFiles()).length == 1) {
                tempFolder = list[0];
            }
            EclipseInstallation installation = EclipseOptions.fromInstallation(tempFolder);
            readFromInstallation(installation);
        }
        else if (hasSignature(buffer, RAR_SIGNATURE)) {
            throw new IOException("RAR-Files are not supported yet");
        }
        else if (hasSignature(buffer, TAR_SIGNATURE)) {
            throw new IOException("TAR-Files are not supported yet");
        }
        else if (hasSignature(buffer, GZ_SIGNATURE)) {
            parseStream(new GZIPInputStream(stream));
        }
    }

    public EclipseInstallationBuilder(EclipseInstallation installation) throws IOException {
        readFromInstallation(installation);
    }

    private void readFromInstallation(EclipseInstallation installation)
        throws IOException, FileNotFoundException, MalformedURLException {
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
                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        parseLauncherIni(fileInputStream, options);
                    }
                    break;
                }
            }
        }
        File configIni = new File(installation.getLayout().getConfigurationFolder(), CONFIG_INI);
        if (configIni.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(configIni)) {
                parseConfigIni(fileInputStream, options, installation,
                    installation.getLayout().getConfigurationFolder().toURI().toURL());
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

    private static void parseConfigIni(InputStream stream, List<Option> options,
        EclipseBundleSource bundleSource, URL baseUrl) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);
        Set<String> names = properties.stringPropertyNames();
        for (String key : names) {
            if ("osgi.framework".equals(key)) {
                // ignore framework setting since we start the framework ourself, and Eclipse can
                // throw exceptions when using relative path here see
                // https://bugs.eclipse.org/bugs/show_bug.cgi?id=527175
                continue;
            }
            String value = properties.getProperty(key);
            if ("org.eclipse.equinox.simpleconfigurator.configUrl".equals(key)) {
                URL url = new URL(baseUrl, value);
                parseSimpleConfigurator(url.openStream(), options, bundleSource);
                value = value + ".disabled";
            }
            options.add(CoreOptions.frameworkProperty(key).value(value));
        }
    }

    private static void parseSimpleConfigurator(InputStream stream, List<Option> options,
        EclipseBundleSource bundleSource) throws IOException {
        SimpleConfigurationParser parser = new SimpleConfigurationParser(stream);
        List<ArtifactInfo<RuntimeOption>> items = parser.getItems();
        for (ArtifactInfo<RuntimeOption> info : items) {
            EclipseBundleOption bundle = bundleSource.bundle(info.getId(), info.getVersion());
            bundle.start(info.getContext().isStart());
            int sl = info.getContext().getStartlevel();
            bundle.startLevel(sl);
            if (sl > -1) {
                options.add(bundle);
            }
        }
    }

    private static void parseLauncherIni(InputStream stream, List<Option> options)
        throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
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

    private static boolean hasSignature(byte[] buffer, byte[] signature) {
        for (int i = 0; i < signature.length; i++) {
            if (signature[i] != buffer[i]) {
                return false;
            }
        }
        return true;
    }

}
