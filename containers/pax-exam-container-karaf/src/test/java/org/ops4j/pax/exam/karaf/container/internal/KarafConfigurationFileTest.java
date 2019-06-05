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
import java.io.FileOutputStream;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.cm.file.ConfigurationHandler;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public abstract class KarafConfigurationFileTest {

    final static protected File KARAF_ETC = new File("src/test/resources");

    @Test
    public void store() throws Exception {
        final String[] strings = {"1", "2", "3", "4"};
        final Dictionary<String, Object> configuration = new Hashtable<>();
        configuration.put("strings", strings);
        final FileOutputStream fos = new FileOutputStream("target/store.config");
        ConfigurationHandler.write(fos, configuration);
        fos.close();
    }

    @Test
    public void load() throws Exception {
        final String[] expected = {"1", "2", "3", "4"};
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(KARAF_ETC, "etc/strings_4");
        karafConfigFile.load();
        final String[] strings = toStrings(karafConfigFile.get("strings"));
        assertArrayEquals(expected, strings);
    }

    @Test
    public void put() {
        final Double d = 2D;
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(KARAF_ETC, "etc/na");
        karafConfigFile.put("Double", d);
    }

    @Test
    public void extendEmpty() throws Exception {
        final String[] expected = {"1"};
        final String i = "1";
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(KARAF_ETC, "etc/na");
        karafConfigFile.load();
        karafConfigFile.extend("strings", i);
        final String[] strings = toStrings(karafConfigFile.get("strings"));
        assertArrayEquals(expected, strings);
    }

    @Test
    public void extendExisting() throws Exception {
        final String[] expected = {"1", "2", "3", "4", "5"};
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(KARAF_ETC, "etc/strings_4");
        karafConfigFile.load();
        karafConfigFile.extend("strings", "5");
        final String[] strings = toStrings(karafConfigFile.get("strings"));
        assertArrayEquals(expected, strings);
    }

    @Test
    public void get() {
        final String in = "value";
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(KARAF_ETC, "etc/na");
        karafConfigFile.put("key", in);
        final Object out = karafConfigFile.get("key");
        assertEquals(in, out);
    }

    protected abstract KarafConfigurationFile newKarafConfigurationFile(final File karafHome, final String location);

    protected abstract String[] toStrings(Object strings);

}
