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
import java.util.Properties;

public class KarafCfgFile extends KarafConfigurationFile {

    private final Properties properties = new Properties();

    public KarafCfgFile(final File karafHome, final String location) {
        super(karafHome, location);
    }

    @Override
    public void store() throws IOException {
        final FileOutputStream fos = new FileOutputStream(file);
        properties.store(fos, "Modified by paxexam");
        fos.close();
    }

    @Override
    public void load() throws IOException {
        if (!file.exists()) {
            return;
        }
        final FileInputStream fis = new FileInputStream(file);
        properties.clear();
        properties.load(fis);
        fis.close();
    }

    @Override
    public void put(final String key, final Object value) {
        properties.put(key, value);
    }

    @Override
    public void extend(final String key, final Object value) {
        if (properties.get(key) == null) {
            properties.put(key, value);
            return;
        }
        properties.put(key, JoinUtil.join((String) properties.get(key), (String) value));
    }

    @Override
    public Object get(final String key) {
        return properties.getProperty(key);
    }

}
