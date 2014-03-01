/*
 * Copyright 2013 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.spi.war;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import org.ops4j.io.FileUtils;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.options.JarProbeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a JAR according to a {@link JarProbeOption}.
 * 
 * @author Harald Wellmann
 * 
 */
public class JarBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(JarBuilder.class);

    /**
     * Temporary directory for assembling the JAR probe.
     */
    private File tempDir;

    /**
     * Option used to configure the JAR.
     */
    private JarProbeOption option;

    /**
     * Constructs a JAR builder for the given option.
     * 
     * @param option
     *            JAR probe option
     */
    public JarBuilder(File tempDir, JarProbeOption option) {
        this.option = option;
        this.tempDir = tempDir;
    }

    /**
     * Builds a JAR from the given option.
     * 
     * @return file URI referencing the JAR in a temporary directory
     */
    public URI buildJar() {
        if (option.getName() == null) {
            option.name(UUID.randomUUID().toString());
        }
        try {
            File explodedJarDir = getExplodedJarDir();
            File probeJar = new File(tempDir, option.getName() + ".jar");
            ZipBuilder builder = new ZipBuilder(probeJar);
            builder.addDirectory(explodedJarDir, "");
            builder.close();
            URI warUri = probeJar.toURI();
            LOG.info("JAR probe = {}", warUri);
            return warUri;
        }
        catch (IOException exc) {
            throw new TestContainerException(exc);
        }
    }

    private File getExplodedJarDir() throws IOException {
        File jarDir = new File(tempDir, "jar");
        LOG.debug("building webapp in {}", jarDir);
        jarDir.mkdir();

        File metaInfDir = new File(jarDir, "META-INF");
        metaInfDir.mkdir();
        for (String metaInfResource : option.getMetaInfResources()) {
            File source = new File(metaInfResource);
            if (source.isDirectory()) {
                copyDirectory(source, metaInfDir);
            }
            else {
                FileUtils.copyFile(source, new File(metaInfDir, source.getName()), null);
            }
        }

        File resourceDir = jarDir;
        resourceDir.mkdir();
        for (Class<?> klass : option.getClasses()) {
            addClass(klass, resourceDir);
        }

        for (String resource : option.getResources()) {
            addResource(resource, resourceDir);
        }

        return jarDir;
    }

    private void addClass(Class<?> klass, File resourceDir) throws IOException {
        String resource = "/" + klass.getName().replaceAll("\\.", "/") + ".class";
        addResource(resource, resourceDir);
        for (Class<?> innerClass : klass.getClasses()) {
            addClass(innerClass, resourceDir);
        }
    }

    private void addResource(String resource, File resourceDir) throws IOException {
        InputStream is = getClass().getResourceAsStream("/" + resource);
        File target = new File(resourceDir, resource);
        target.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(target);
        StreamUtils.copyStream(is, os, true);
    }

    private void copyDirectory(File fromDir, File toDir) throws IOException {
        for (File file : fromDir.listFiles()) {
            if (file.isDirectory()) {
                File targetDir = new File(toDir, file.getName());
                targetDir.mkdir();
                copyDirectory(file, targetDir);
            }
            else {
                FileUtils.copyFile(file, new File(toDir, file.getName()), null);
            }
        }
    }
}
