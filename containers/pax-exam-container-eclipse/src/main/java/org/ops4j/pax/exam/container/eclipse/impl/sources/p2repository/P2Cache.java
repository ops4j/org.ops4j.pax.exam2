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
package org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supports caching of several data read by P2
 * 
 * @author Christoph Läubrich
 *
 */
public class P2Cache {

    private static final Logger LOG = LoggerFactory.getLogger(P2Cache.class);

    private static final File FILE_CACHE_LOACATION = new File(System.getProperty("exam.p2.cache",
        System.getProperty("user.home") + "/.eclipse/exam.p2/cache"));

    private static final boolean useCacheOnServerError = !Boolean
        .getBoolean("exam.p2.dontcacheonfailed");

    private static final Map<URL, MetaDataProperties> metaDataProperties = new HashMap<>();

    public static synchronized MetaDataProperties getMetaDataProperties(URL url)
        throws IOException {
        MetaDataProperties p = metaDataProperties.get(url);
        if (p == null) {
            File cacheFile = getCacheFile(url);
            File metaFile = new File(cacheFile.getParentFile(), cacheFile.getName() + ".meta");
            p = new MetaDataProperties(metaFile);
            metaDataProperties.put(url, p);
        }
        return p;
    }

    public static P2CacheStream tryOpen(URL url) throws IOException {
        LOG.debug("try open {}...", url);
        URLConnection connection;
        try {
            connection = url.openConnection();
            connection.setUseCaches(true);
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection http = (HttpURLConnection) connection;
                http.setInstanceFollowRedirects(true);
                File cachefile = getCacheFile(url);
                if (cachefile.exists()) {
                    connection.setIfModifiedSince(cachefile.lastModified());
                }
                int code = http.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK
                    || code == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    long length = http.getContentLengthLong();
                    LOG.debug("...success. Size: {}",
                        length > 0 ? (length / 1024.0) + "kb" : "unkown");
                    return getCachedStream(url, http, cachefile);
                }
                else {
                    if (useCacheOnServerError) {
                        File cacheFile = getCacheFile(url);
                        if (cacheFile.exists()) {
                            return new P2CacheStream(cacheFile);
                        }
                    }
                    LOG.debug("...failed with http-code {}!", code);
                    http.getErrorStream().close();
                    return null;
                }
            }
        }
        catch (IOException e) {
            if (useCacheOnServerError) {
                File cacheFile = getCacheFile(url);
                if (cacheFile.exists()) {
                    return new P2CacheStream(cacheFile);
                }
            }
            throw e;
        }
        try {
            InputStream stream = connection.getInputStream();
            LOG.debug("...success!");
            return new P2CacheStream(stream, -1);
        }
        catch (FileNotFoundException e) {
            LOG.debug("...failed with error {}!", e.getMessage());
            return null;
        }
    }

    public static File getCacheFile(URL url) {
        String key = url.toExternalForm().replaceAll("[^a-zA-Z0-9_\\.-]", "_");
        File file = new File(FILE_CACHE_LOACATION, key);
        return file;
    }

    public static P2CacheStream getCachedStream(URL url, HttpURLConnection connection,
        File cachefile) throws IOException {
        if (FILE_CACHE_LOACATION.exists() || FILE_CACHE_LOACATION.mkdirs()) {
            OutputStream outStream = null;
            long lastModified = connection.getLastModified() / 1000;
            try {
                if (cachefile.exists()) {
                    boolean notmodified;
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        notmodified = true;
                    }
                    else {
                        notmodified = lastModified > 0
                            && lastModified <= (cachefile.lastModified() / 1000);
                    }
                    if (notmodified) {
                        LOG.debug("return cached file {}...", cachefile);
                        return new P2CacheStream(cachefile);
                    }
                }
                outStream = new FileOutputStream(cachefile);
            }
            catch (IOException e) {
                LOG.debug("loading cache file failed!", e);
            }
            try {
                InputStream stream = connection.getInputStream();
                if (outStream == null) {
                    // out stream creation failed so we can only return the raw stream...
                    return new P2CacheStream(stream, lastModified);
                }
                // all fine for copy the data...
                try {
                    LOG.info("Download {}...", url);
                    IOUtils.copy(stream, outStream);
                    outStream.close();
                    cachefile.setLastModified(lastModified * 1000);
                    return new P2CacheStream(cachefile);
                }
                finally {
                    IOUtils.closeQuietly(stream);
                }
            }
            finally {
                IOUtils.closeQuietly(outStream);
            }
        }
        return new P2CacheStream(connection.getInputStream(), -1);
    }

    public static class MetaDataProperties extends Properties {

        private static final long serialVersionUID = 8907898492960803462L;
        private final File metaFile;
        private final File metaObjectFile;
        private final Map<String, Object> objectBuffer;

        @SuppressWarnings("unchecked")
        private MetaDataProperties(File metaFile) throws IOException {
            this.metaFile = metaFile;
            this.metaObjectFile = new File(metaFile.getParentFile(),
                metaFile.getName() + ".obj.gz");
            if (metaFile.exists()) {
                try (FileInputStream stream = new FileInputStream(metaFile)) {
                    super.load(stream);
                }
            }
            if (metaObjectFile.exists()) {
                try (ObjectInputStream stream = new ObjectInputStream(
                    new GZIPInputStream(new FileInputStream(metaObjectFile)))) {
                    objectBuffer = (Map<String, Object>) stream.readObject();
                }
                catch (ClassNotFoundException e) {
                    throw new IOException("reading cached objects failed!", e);
                }
            }
            else {
                objectBuffer = new HashMap<>();
            }
        }

        public synchronized void store() {
            try {
                try (FileOutputStream stream = new FileOutputStream(metaFile)) {
                    super.store(stream, null);
                }
                try (ObjectOutputStream stream = new ObjectOutputStream(
                    new GZIPOutputStream(new FileOutputStream(metaObjectFile)))) {
                    stream.writeObject(objectBuffer);
                    stream.flush();
                }
            }
            catch (Exception e) {
                LOG.debug("Storing metadata failed: {}", e.toString());
            }
        }

        @Override
        public synchronized void load(InputStream inStream) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void load(Reader reader) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public synchronized void loadFromXML(InputStream in)
            throws IOException, InvalidPropertiesFormatException {
            throw new UnsupportedOperationException();
        }

        public void setObjectProperty(String key, Object value) {
            objectBuffer.put(key, value);
        }

        public <T> T getObjectProperty(String key, Class<T> type) {
            return type.cast(objectBuffer.get(key));
        }

        @Override
        public synchronized void clear() {
            objectBuffer.clear();
            super.clear();
        }

        public Set<String> objectNames() {
            return Collections.unmodifiableSet(objectBuffer.keySet());
        }

        public void clearObjects(String cacheKey) {
            for (Iterator<java.util.Map.Entry<String, Object>> iterator = objectBuffer.entrySet()
                .iterator(); iterator.hasNext();) {
                String key = iterator.next().getKey();
                if (key.startsWith(cacheKey)) {
                    iterator.remove();
                }
            }
        }

        public void clear(String cacheKey) {
            Set<String> names = stringPropertyNames();
            for (String key : names) {
                if (key.startsWith(cacheKey)) {
                    remove(key);
                }
            }
        }

        public boolean isModified(String cacheKey, long lastModified) {
            return lastModified < 0 || Long.parseLong(getProperty(cacheKey, "-1")) != lastModified;
        }

        public void setProperty(String key, Object value) {
            if (value == null) {
                setProperty(key, (String) null);
            }
            setProperty(key, value.toString());
        }
    }

    public static final class P2CacheStream extends FilterInputStream {

        private File cachefile;
        private long lastModified;

        protected P2CacheStream(InputStream in, long lastModified) {
            super(in);
            this.lastModified = lastModified;
        }

        public P2CacheStream(File cachefile) throws FileNotFoundException {
            super(new FileInputStream(cachefile));
            this.cachefile = cachefile;
        }

        public long getLastModified() {
            if (cachefile != null) {
                return cachefile.lastModified();
            }
            return lastModified;
        }

    }
}
