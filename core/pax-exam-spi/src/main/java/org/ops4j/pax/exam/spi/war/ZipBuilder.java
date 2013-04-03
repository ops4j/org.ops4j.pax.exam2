/*
 * Copyright 2013 Harald Wellmann
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

package org.ops4j.pax.exam.spi.war;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.ops4j.io.StreamUtils;

public class ZipBuilder {

    private FileOutputStream os;
    private ZipOutputStream jarOutputStream;

    public ZipBuilder(File zipFile) throws IOException {
        this.os = new FileOutputStream(zipFile);
        this.jarOutputStream = new ZipOutputStream(os);
    }

    public ZipBuilder addDirectory(File sourceDir, String targetDir) throws IOException {
        addDirectory(sourceDir, sourceDir, jarOutputStream);
        return this;
    }

    public ZipBuilder addFile(File sourceFile, String targetFile) throws IOException {
        FileInputStream fis = new FileInputStream(sourceFile);
        ZipEntry jarEntry = new ZipEntry(targetFile);
        jarOutputStream.putNextEntry(jarEntry);
        StreamUtils.copyStream(fis, jarOutputStream, false);
        fis.close();
        return this;
    }

    public void close() throws IOException {
        if (jarOutputStream != null) {
            jarOutputStream.close();
        }
        else if (os != null) {
            os.close();
        }
    }

    private void addDirectory(File root, File directory, ZipOutputStream zos) throws IOException {
        // directory entries are required, or else bundle classpath may be
        // broken
        if (!directory.equals(root)) {
            String path = normalizePath(root, directory);
            ZipEntry jarEntry = new ZipEntry(path + "/");
            jarOutputStream.putNextEntry(jarEntry);
        }
        File[] children = directory.listFiles();
        // loop through dirList, and zip the files
        for (File child : children) {
            if (child.isDirectory()) {
                addDirectory(root, child, jarOutputStream);
            }
            else {
                archiveFile(root, child, jarOutputStream);
            }
        }
    }

    private void archiveFile(File root, File child, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(child);
        ZipEntry jarEntry = new ZipEntry(normalizePath(root, child));
        zos.putNextEntry(jarEntry);
        StreamUtils.copyStream(fis, zos, false);
        fis.close();
    }

    /**
     * Returns the relative path of the given file with respect to the root directory, with all file
     * separators replaced by slashes.
     * 
     * Example: For root {@code C:\work} and file {@code C:\work\com\example\Foo.class}, the result
     * is {@code com/example/Foo.class}
     * 
     * @param file
     * @return
     */
    private String normalizePath(File root, File file) {
        String relativePath = file.getPath().substring(root.getPath().length() + 1);
        String path = relativePath.replaceAll("\\" + File.separator, "/");
        return path;
    }
}
