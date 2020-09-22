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

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public abstract class KarafConfigurationFileTest {

    final static protected File KARAF_ETC = new File("src/test/resources");

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void store() throws Exception {
        final String string = "1234";
        File dir = tempDir.newFolder();
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(dir, "/store");
        karafConfigFile.put("string", string);
        karafConfigFile.store();

        final KarafConfigurationFile loadedKarafConfigFile = newKarafConfigurationFile(dir, "/store");
        loadedKarafConfigFile.load();
        final String loadedString = (String)loadedKarafConfigFile.get("string");
        assertEquals(string, loadedString);
    }

    @Test
    public void load() throws Exception {
        final String[] expected = {"1", "2", "3", "4"};
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(KARAF_ETC, "etc/strings_4");
        karafConfigFile.load();
        final String[] strings = toStrings(karafConfigFile.get("strings"));
        assertArrayEquals(expected, strings);

        // load should overwrite existing data
        karafConfigFile.put("extra", "extra");
        assertEquals("extra", (String) karafConfigFile.get("extra"));
        karafConfigFile.load();
        assertNull(karafConfigFile.get("extra"));
    }

    @Test
    public void replace() throws Exception {
        final String[] expected = {"1", "2", "3", "4"};
        File dir = tempDir.newFolder();
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(dir, "replace");
        karafConfigFile.load();
        assertNull(karafConfigFile.get("strings"));

        final KarafConfigurationFile sourceConfig = newKarafConfigurationFile(KARAF_ETC, "etc/strings_4");
        karafConfigFile.replace(sourceConfig.file);
        String[] strings = toStrings(karafConfigFile.get("strings"));
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
