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
package org.ops4j.pax.exam.container.eclipse.impl.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.AbstractParser;
import org.osgi.framework.Version;

/**
 * A source that can be cached to a folder
 * 
 * @author Christoph Läubrich
 *
 */
public interface CacheableSource {

    public static final String KEY_CLASS_NAME = "className";
    public static final String CACHE_METADATA_NAME = "source.metadata";

    /**
     * Writes this source to the given destination
     * 
     * @param metadata
     *            properties to place metadata to that can be used to restore data. Implementations
     *            should prefix keys with the classname to prevent name-clashes
     * @param cacheFolder
     *            the folder where to write data to, the implementation is free to use the folder as
     *            needed
     * @throws IOException
     */
    public void writeToFolder(Properties metadata, File cacheFolder) throws IOException;

    public static final class StoreUtil {
    
	    public static void store(CacheableSource source, File baseFolder) throws IOException {
	        Properties properties = new Properties();
	        File dataFolder = new File(baseFolder, "data");
	        source.writeToFolder(properties, dataFolder);
	        properties.setProperty(KEY_CLASS_NAME, source.getClass().getName());
	        try (FileOutputStream out = new FileOutputStream(
	            new File(baseFolder, CACHE_METADATA_NAME))) {
	            properties.store(out, null);
	        }
	    }
	
	    @SuppressWarnings("unchecked")
	    public static <T extends CacheableSource> T load(File baseFolder) throws IOException {
	        Properties properties = new Properties();
	        File dataFolder = new File(baseFolder, "data");
	        try (FileInputStream in = new FileInputStream(new File(baseFolder, CACHE_METADATA_NAME))) {
	            properties.load(in);
	        }
	        String classProperty = properties.getProperty(KEY_CLASS_NAME);
	        if (classProperty == null) {
	            throw new IllegalStateException("property " + KEY_CLASS_NAME + " is missing");
	        }
	        try {
	            Class<?> loadClass = CacheableSource.class.getClassLoader().loadClass(classProperty);
	            Method method = loadClass.getMethod("restoreFromCache", Properties.class, File.class);
	            return (T) method.invoke(null, properties, dataFolder);
	        }
	        catch (Exception e) {
	            if (e instanceof IOException) {
	                throw (IOException) e;
	            }
	            throw new IOException("can't load source", e);
	        }
	    }
	
	    public static String encode(EclipseVersionedArtifact artifact) {
	        return artifact.getId() + ":" + artifact.getVersion();
	    }
	
	    public static ArtifactInfo<Void> decode(String key) {
	        int index = key.lastIndexOf(':');
	        String symbolicName = key.substring(0, index);
	        Version version = AbstractParser.stringToVersion(key.substring(index + 1));
	        return new ArtifactInfo<Void>(symbolicName, version, null);
	    }
    }
}
