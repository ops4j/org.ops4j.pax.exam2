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

import static org.junit.Assert.assertArrayEquals;
import java.io.File;
import org.junit.Test;

public class KarafConfigFileTest extends KarafConfigurationFileTest {

    @Override
    protected KarafConfigurationFile newKarafConfigurationFile(File karafHome, String location) {
        return new KarafConfigFile(karafHome, location + ".config");
    }

    @Override
    protected String[] toStrings(Object strings) {
        return (String[])strings;
    }

    @Test
    public void loadInts() throws Exception {
        final int[] expected = {1, 2, 3, 4};
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(KARAF_ETC, "etc/ints_4");
        karafConfigFile.load();
        final int[] ints = (int[]) karafConfigFile.get("ints");
        assertArrayEquals(expected, ints);
    }

    @Test
    public void extendEmptyInts() throws Exception {
        final Integer[] expected = {1};
        final int i = 1;
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(KARAF_ETC, "etc/na");
        karafConfigFile.load();
        karafConfigFile.extend("ints", i);
        final Integer[] ints = (Integer[]) karafConfigFile.get("ints");
        assertArrayEquals(expected, ints);
    }

    @Test
    public void extendExistingInts() throws Exception {
        final int[] expected = {1, 2, 3, 4, 5};
        final KarafConfigurationFile karafConfigFile = newKarafConfigurationFile(KARAF_ETC, "etc/ints_4");
        karafConfigFile.load();
        karafConfigFile.extend("ints", 5);
        final int[] ints = (int[]) karafConfigFile.get("ints");
        assertArrayEquals(expected, ints);
    }

}