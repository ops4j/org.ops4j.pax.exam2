/*
 * Copyright (C) 2010 Toni Menzel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.spi.intern;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;

/**
 * Static local provider.
 */
public class DefaultTestProbeProvider implements TestProbeProvider {

    private Handle probe;
    private Store<InputStream> store;
    private String formattedInfo = "";

    public DefaultTestProbeProvider(Store<InputStream> store, Handle probe) {
        this.store = store;
        this.probe = probe;

        // Prepare text info
        formattedInfo = constuctInfo();
    }

    @Override
    public InputStream getStream() throws IOException {
        return store.load(probe);
    }

    @Override
    public String toString() {
        return formattedInfo;
    }

    // TODO: use some flat JSON Tree Model so we don't have to mess with formatting here.
    private String constuctInfo() {
        JarInputStream in = null;
        String info = "";
        try {
            info += "[Probe ID: " + probe.getIdentification() + "]\n";
            info += "[Probe Location: " + store.getLocation(probe).toASCIIString() + "]\n";

            in = new JarInputStream(store.load(probe));
            Manifest man = in.getManifest();
            Attributes mainAttributes = man.getMainAttributes();

            info += "[Headers: " + "\n";
            for (Object key : mainAttributes.keySet()) {
                info += "    " + key + "=" + mainAttributes.get(key) + "\n";
            }
            info += "]" + "\n";

            // surround
            info = "\n--\n" + info + "--\n";
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                //
            }
        }
        return info;
    }
}
