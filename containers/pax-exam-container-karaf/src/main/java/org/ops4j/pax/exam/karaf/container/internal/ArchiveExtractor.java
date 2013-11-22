/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.karaf.container.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Extract zip or tar.gz archives to a target folder
 */
public class ArchiveExtractor {
    
    private ArchiveExtractor() {
    }

    /**
     * Extract zip or tar.gz archives to a target folder
     * 
     * @param sourceURL url of the archive to extract
     * @param targetFolder where to extract to
     * @throws IOException
     */
    public static void extract(URL sourceURL, File targetFolder)
        throws IOException {
        if (sourceURL.getProtocol().equals("file")) {
            if (sourceURL.getFile().indexOf(".zip") > 0) {
                extractZipDistribution(sourceURL, targetFolder);
            }
            else if (sourceURL.getFile().indexOf(".tar.gz") > 0) {
                extractTarGzDistribution(sourceURL, targetFolder);
            }
            else {
                throw new IllegalStateException(
                    "Unknow packaging of distribution; only zip or tar.gz could be handled.");
            }
            return;
        }
        if (sourceURL.toExternalForm().indexOf("/zip") > 0) {
            extractZipDistribution(sourceURL, targetFolder);
        }
        else if (sourceURL.toExternalForm().indexOf("/tar.gz") > 0) {
            extractTarGzDistribution(sourceURL, targetFolder);
        }
        else {
            throw new IllegalStateException(
                "Unknow packaging; only zip or tar.gz could be handled. URL was " + sourceURL);
        }
    }

    private static void extractTarGzDistribution(URL sourceDistribution, File _targetFolder)
        throws IOException {
        File uncompressedFile = File.createTempFile("uncompressedTarGz-", ".tar");
        extractGzArchive(sourceDistribution.openStream(), uncompressedFile);
        extract(new TarArchiveInputStream(new FileInputStream(uncompressedFile)), _targetFolder);
        FileUtils.forceDelete(uncompressedFile);
    }

    private static void extractZipDistribution(URL sourceDistribution, File _targetFolder)
        throws IOException {
        extract(new ZipArchiveInputStream(sourceDistribution.openStream()), _targetFolder);
    }

    private static void extractGzArchive(InputStream tarGz, File tar) throws IOException {
        BufferedInputStream in = new BufferedInputStream(tarGz);
        FileOutputStream out = new FileOutputStream(tar);
        GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
        final byte[] buffer = new byte[1000];
        int n = 0;
        while (-1 != (n = gzIn.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
        gzIn.close();
    }

    private static void extract(ArchiveInputStream is, File targetDir) throws IOException {
        try {
            if (targetDir.exists()) {
                FileUtils.forceDelete(targetDir);
            }
            targetDir.mkdirs();
            ArchiveEntry entry = is.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                name = name.substring(name.indexOf("/") + 1);
                File file = new File(targetDir, name);
                if (entry.isDirectory()) {
                    file.mkdirs();
                }
                else {
                    file.getParentFile().mkdirs();
                    OutputStream os = new FileOutputStream(file);
                    try {
                        IOUtils.copy(is, os);
                    }
                    finally {
                        IOUtils.closeQuietly(os);
                    }
                }
                entry = is.getNextEntry();
            }
        }
        finally {
            is.close();
        }
    }
}
