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

/**
 * Builds a ZIP archive from individual files and directories.
 * 
 * @author Harald Wellmann
 * 
 */
public class ZipBuilder {

    private FileOutputStream os;
    private ZipOutputStream jarOutputStream;

    /**
     * Creates a ZIP archive in the given file. Allocates underlying file system resources. The user
     * must call {@link #close()} to release these resources.
     * 
     * @param zipFile
     * @throws IOException
     */
    public ZipBuilder(File zipFile) throws IOException {
        this.os = new FileOutputStream(zipFile);
        this.jarOutputStream = new ZipOutputStream(os);
    }

    /**
     * Recursively adds a directory tree to the archive. The archive must not be closed.
     * <p>
     * Example:<br/>
     * 
     * <pre>
     * sourceDir = /opt/work/classes
     * targetDir = WEB-INF/classes
     * 
     * /opt/work/classes/com/acme/Foo.class -> WEB-INF/classes/com/acme/Foo.class
     * </pre>
     * 
     * @param sourceDir
     *            Root directory of tree to be added
     * @param targetDir
     *            Relative path within the archive corresponding to root. Regardless of the OS, this
     *            path must use slashes ('/') as separators.
     * 
     * @return this for fluent syntax
     * @throws IOException
     */
    public ZipBuilder addDirectory(File sourceDir, String targetDir) throws IOException {
        addDirectory(sourceDir, sourceDir, jarOutputStream);
        return this;
    }

    /**
     * Adds a file to the archive. The archive must not be closed.
     * <p>
     * Example:<br/>
     * 
     * <pre>
     * sourceFile = C:\opt\work\deps\foo.jar
     * targetDir = WEB-INF/lib/foo.jar
     * 
     * </pre>
     * 
     * @param sourceFile
     *            File to be added
     * @param targetDir
     *            Relative path for the file within the archive. Regardless of the OS, this path
     *            must use slashes ('/') as separators.
     * 
     * @return this for fluent syntax
     * @throws IOException
     */
    public ZipBuilder addFile(File sourceFile, String targetFile) throws IOException {
        FileInputStream fis = new FileInputStream(sourceFile);
        ZipEntry jarEntry = new ZipEntry(targetFile);
        jarOutputStream.putNextEntry(jarEntry);
        StreamUtils.copyStream(fis, jarOutputStream, false);
        fis.close();
        return this;
    }

    /**
     * Closes the archive and releases file system resources. No more files or directories may be
     * added after calling this method.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        if (jarOutputStream != null) {
            jarOutputStream.close();
        }
        else if (os != null) {
            os.close();
        }
    }

    /**
     * Recursively adds the contents of the given directory and all subdirectories to the given ZIP
     * output stream.
     * 
     * @param root
     *            an ancestor of {@code directory}, used to determine the relative path within the
     *            archive
     * @param directory
     *            current directory to be added
     * @param zos
     *            ZIP output stream
     * @throws IOException
     */
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
                addFile(root, child, jarOutputStream);
            }
        }
    }

    /**
     * Adds a given file to the given ZIP output stream.
     * 
     * @param root
     *            an ancestor of {@code file}, used to determine the relative path within the
     *            archive
     * @param file
     *            file to be added
     * @param zos
     *            ZIP output stream
     * @throws IOException
     */
    private void addFile(File root, File file, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry jarEntry = new ZipEntry(normalizePath(root, file));
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
