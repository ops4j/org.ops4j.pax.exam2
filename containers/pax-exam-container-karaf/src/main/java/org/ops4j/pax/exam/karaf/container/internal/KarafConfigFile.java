/*
 * Copyright 2017 Oliver Lietz.
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
package org.ops4j.pax.exam.karaf.container.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.cm.file.ConfigurationHandler;

/**
 * @link https://sling.apache.org/documentation/bundles/configuration-installer-factory.html#configuration-files-config
 */
public class KarafConfigFile extends KarafConfigurationFile {

    private Dictionary configuration = new Hashtable();

    public KarafConfigFile(File karafHome, String location) {
        super(karafHome, location);
    }

    @Override
    public void store() throws IOException {
        final FileOutputStream fos = new FileOutputStream(file);
        ConfigurationHandler.write(fos, configuration);
        fos.close();
    }

    @Override
    public void load() throws IOException {
        if (!file.exists()) {
            return;
        }
        final FileInputStream fis = new FileInputStream(file);
        configuration = ConfigurationHandler.read(fis);
        fis.close();
    }

    @Override
    public void put(final String key, final Object value) {
        configuration.put(key, value);
    }

    // TODO support primitives
    @Override
    public void extend(final String key, final Object value) {
        final Object v = configuration.get(key);
        if (v == null) {
            final Object array = Array.newInstance(value.getClass(), 1);
            Array.set(array, 0, value);
            configuration.put(key, array);
        } else {
            final Class clazz = v.getClass();
            if (clazz.isArray()) {
                final int length = Array.getLength(v);
                final Object array = Array.newInstance(clazz.getComponentType(), length + 1);
                System.arraycopy(v, 0, array, 0, length);
                Array.set(array, length, value);
                configuration.put(key, array);
            } else if (v instanceof Collection) {
                ((Collection) v).add(value);
            } else {
                final String message = String.format("Cannot extend %s by %s.", key, value);
                throw new IllegalArgumentException(message);
            }
        }
    }

    @Override
    public Object get(final String key) {
        return configuration.get(key);
    }

}
