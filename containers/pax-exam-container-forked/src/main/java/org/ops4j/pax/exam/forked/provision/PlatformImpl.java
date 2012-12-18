/*
 * Copyright 2007 Alin Dreghiciu, Stuart McCulloch.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.forked.provision;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.ops4j.pax.exam.TestContainerException;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the workflow of creating the platform. Concrete platforms should implement only the
 * PlatformBuilder interface. TODO Add unit tests
 * 
 * @author Alin Dreghiciu, Stuart McCulloch, Harald Wellmann
 * @since August 19, 2007
 */
public class PlatformImpl {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformImpl.class);

    /**
     * Concrete platform builder as equinox, felix, kf.
     */
    /**
     * Downloads files from urls.
     * 
     * @param workDir
     *            the directory where to download bundles
     * @param url
     *            of the file to be downloaded
     * @param displayName
     *            to be shown during download
     * @param overwrite
     *            if the bundles should be overwritten
     * @param checkAttributes
     *            whether or not to check attributes in the manifest
     * @param failOnValidation
     *            if validation fails should or not fail with an exception (or just return null)
     * @param downloadFeeback
     *            whether or not downloading process should display fine grained progres info
     * 
     * @return the File corresponding to the downloaded file, or null if the bundle is invalid (not
     *         an osgi bundle)
     * 
     * @throws TestContainerException
     *             if the url could not be downloaded
     */
    public File download(final File workDir, final URL url, final String displayName,
        final Boolean overwrite, final boolean checkAttributes, final boolean failOnValidation,
        final boolean downloadFeeback) {
        LOGGER.debug("Downloading [" + url + "]");
        File downloadedBundlesFile = new File(workDir, "bundles/downloaded_bundles.properties");
        Properties fileNamesForUrls = loadProperties(downloadedBundlesFile);

        String downloadedFileName = fileNamesForUrls.getProperty(url.toExternalForm());
        String hashFileName = "" + url.toExternalForm().hashCode();
        if (downloadedFileName == null) {
            // destination will be made based on the hashcode of the url to be downloaded
            downloadedFileName = hashFileName + ".jar";

        }
        File destination = new File(workDir, "bundles/" + downloadedFileName);

        // download the bundle only if is a forced overwrite or the file does not exist or the file
        // is there but is
        // invalid
        boolean forceOverwrite = overwrite || !destination.exists();
        if (!forceOverwrite) {
            try {
                String cachingName = determineCachingName(destination, hashFileName);
                if (!destination.getName().equals(cachingName)) {
                    throw new TestContainerException("File " + destination + " should have name "
                        + cachingName);
                }
            }
            catch (TestContainerException ignore) {
                forceOverwrite = true;
            }
        }
        if (forceOverwrite) {
            try {
                LOGGER.debug("Creating new file at destination: " + destination.getAbsolutePath());
                destination.getParentFile().mkdirs();
                destination.createNewFile();
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(destination);
                    FileChannel fileChannel = os.getChannel();
                    StreamUtils.ProgressBar progressBar = null;
                    if (LOGGER.isInfoEnabled()) {
                        if (downloadFeeback) {
                            progressBar = new StreamUtils.FineGrainedProgressBar(displayName);
                        }
                        else {
                            progressBar = new StreamUtils.CoarseGrainedProgressBar(displayName);
                        }
                    }
                    StreamUtils.streamCopy(url, fileChannel, progressBar);
                    fileChannel.close();
                    LOGGER.debug("Successfully downloaded to [" + destination + "]");
                }
                finally {
                    if (os != null) {
                        os.close();
                    }
                }
            }
            catch (IOException e) {
                throw new TestContainerException("[" + url + "] could not be downloaded", e);
            }
        }
        if (checkAttributes) {
            try {
                validateBundle(url, destination);
            }
            catch (TestContainerException e) {
                if (failOnValidation) {
                    throw e;
                }
                return null;
            }
        }
        String cachingName = determineCachingName(destination, hashFileName);
        File newDestination = new File(destination.getParentFile(), cachingName);
        if (!cachingName.equals(destination.getName())) {
            if (newDestination.exists()) {
                if (!newDestination.delete()) {
                    throw new TestContainerException("Cannot delete " + newDestination);
                }
            }
            if (!destination.renameTo(newDestination)) {
                throw new TestContainerException("Cannot rename " + destination + " to "
                    + newDestination);
            }
            fileNamesForUrls.setProperty(url.toExternalForm(), cachingName);
            saveProperties(fileNamesForUrls, downloadedBundlesFile);
        }

        return newDestination;
    }

    private Properties loadProperties(File file) {
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            properties.load(in);
            return properties;
        }
        catch (IOException e) {
            return properties;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void saveProperties(Properties properties, File file) throws TestContainerException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            properties.store(os, "");
        }
        catch (IOException e) {
            throw new TestContainerException("Cannot store properties " + file, e);
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }

        }
    }

    /**
     * Validate that the file is an valid bundle. A valid bundle will be a loadable jar file that
     * has manifest and the manifest contains at least an entry for Bundle-SymboliName or
     * Bundle-Name (R3).
     * 
     * @param url
     *            original url from where the bundle was created.
     * @param file
     *            file to be validated
     * 
     * @throws TestContainerException
     *             if the jar is not a valid bundle
     */
    void validateBundle(final URL url, final File file) throws TestContainerException {
        String bundleSymbolicName = null;
        String bundleName = null;
        JarFile jar = null;
        try {
            // verify that is a valid jar. Do not verify that is signed (the false param).
            jar = new JarFile(file, false);
            final Manifest manifest = jar.getManifest();
            if (manifest == null) {
                throw new TestContainerException("[" + url + "] is not a valid bundle");
            }
            bundleSymbolicName = manifest.getMainAttributes().getValue(
                Constants.BUNDLE_SYMBOLICNAME);
            bundleName = manifest.getMainAttributes().getValue(Constants.BUNDLE_NAME);
        }
        catch (IOException e) {
            throw new TestContainerException("[" + url + "] is not a valid bundle", e);
        }
        finally {
            if (jar != null) {
                try {
                    jar.close();
                }
                catch (IOException ignore) {
                    // just ignore as this is less probably to happen.
                }
            }
        }
        if (bundleSymbolicName == null && bundleName == null) {
            throw new TestContainerException("[" + url + "] is not a valid bundle");
        }
    }

    /**
     * Determine name to be used for caching on local file system.
     * 
     * @param file
     *            file to be validated
     * @param defaultBundleSymbolicName
     *            default bundle symbolic name to be used if manifest does not have a bundle
     *            symbolic name
     * 
     * @return file name based on bundle symbolic name and version
     */
    String determineCachingName(final File file, final String defaultBundleSymbolicName) {
        String bundleSymbolicName = null;
        String bundleVersion = null;
        JarFile jar = null;
        try {
            // verify that is a valid jar. Do not verify that is signed (the false param).
            jar = new JarFile(file, false);
            final Manifest manifest = jar.getManifest();
            if (manifest != null) {
                bundleSymbolicName = manifest.getMainAttributes().getValue(
                    Constants.BUNDLE_SYMBOLICNAME);
                bundleVersion = manifest.getMainAttributes().getValue(Constants.BUNDLE_VERSION);
            }
        }
        catch (IOException ignore) {
            // just ignore
        }
        finally {
            if (jar != null) {
                try {
                    jar.close();
                }
                catch (IOException ignore) {
                    // just ignore as this is less probably to happen.
                }
            }
        }
        if (bundleSymbolicName == null) {
            bundleSymbolicName = defaultBundleSymbolicName;
        }
        else {
            // remove directives like "; singleton:=true"
            int semicolonPos = bundleSymbolicName.indexOf(";");
            if (semicolonPos > 0) {
                bundleSymbolicName = bundleSymbolicName.substring(0, semicolonPos);
            }
        }
        if (bundleVersion == null) {
            bundleVersion = "0.0.0";
        }
        return bundleSymbolicName + "_" + bundleVersion + ".jar";
    }
}
