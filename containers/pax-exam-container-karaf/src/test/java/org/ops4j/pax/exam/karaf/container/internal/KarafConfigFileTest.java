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

public class KarafConfigFileTest {

    final static private File KARAF_ETC = new File("src/test/resources");

    @Test
    public void store() throws Exception {
        final int[] ints = {1, 2, 3, 4};
        final Dictionary<String, Object> configuration = new Hashtable<>();
        configuration.put("ints", ints);
        final FileOutputStream fos = new FileOutputStream("target/store.config");
        ConfigurationHandler.write(fos, configuration);
        fos.close();
    }

    @Test
    public void load() throws Exception {
        final int[] expected = {1, 2, 3, 4};
        final KarafConfigFile karafConfigFile = new KarafConfigFile(KARAF_ETC, "etc/ints_4.config");
        karafConfigFile.load();
        final int[] ints = (int[]) karafConfigFile.get("ints");
        assertArrayEquals(expected, ints);
    }

    @Test
    public void put() throws Exception {
        final Double d = 2D;
        final KarafConfigFile karafConfigFile = new KarafConfigFile(KARAF_ETC, "etc/na.config");
        karafConfigFile.put("Double", d);
    }

    @Test
    public void extendEmpty() throws Exception {
        final Integer[] expected = {1};
        final int i = 1;
        final KarafConfigFile karafConfigFile = new KarafConfigFile(KARAF_ETC, "etc/na.config");
        karafConfigFile.load();
        karafConfigFile.extend("ints", i);
        final Integer[] ints = (Integer[]) karafConfigFile.get("ints");
        assertArrayEquals(expected, ints);
    }

    @Test
    public void extendExisting() throws Exception {
        final int[] expected = {1, 2, 3, 4, 5};
        final KarafConfigFile karafConfigFile = new KarafConfigFile(KARAF_ETC, "etc/ints_4.config");
        karafConfigFile.load();
        karafConfigFile.extend("ints", 5);
        final int[] ints = (int[]) karafConfigFile.get("ints");
        assertArrayEquals(expected, ints);
    }

    @Test
    public void get() throws Exception {
        final String in = "value";
        final KarafConfigFile karafConfigFile = new KarafConfigFile(KARAF_ETC, "etc/na.config");
        karafConfigFile.put("key", in);
        final Object out = karafConfigFile.get("key");
        assertEquals(in, out);
    }

}
