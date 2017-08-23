/*
 * Copyright 2012 Harald Wellmann
 *
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
package org.ops4j.pax.exam.wildfly80;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.ops4j.io.StreamUtils;
import org.ops4j.io.ZipExploder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a ZIP archive from a given URL and unpacks it into a given directory.
 * 
 * @author Harald Wellmann
 *
 */
public class ZipInstaller {

    private static final Logger LOG = LoggerFactory.getLogger(ZipInstaller.class);

    private URL zipUrl;
    private File installDir;

    /**
     * Constructs installer with archive URL and install path.
     * 
     * @param zipUrl archive URL
     * @param installRoot install root directory
     */
    public ZipInstaller(URL zipUrl, String installRoot) {
        this.zipUrl = zipUrl;
        this.installDir = new File(installRoot);
    }

    /**
     * Download and unpacks the archive.
     * 
     * @throws IOException on I/O errors
     */
    public void downloadAndInstall() throws IOException {
        installDir.mkdirs();

        File tempFile = File.createTempFile("pax-exam", ".zip");
        FileOutputStream os = null;
        LOG.info("downloading {} to {}", zipUrl, tempFile);
        try {
            os = new FileOutputStream(tempFile);
            StreamUtils.copyStream(zipUrl.openStream(), os, true);

            LOG.info("unzipping into {}", installDir);
            ZipExploder exploder = new ZipExploder();
            exploder.processFile(tempFile, installDir);
        }
        finally {
            tempFile.delete();
        }
    }
}
