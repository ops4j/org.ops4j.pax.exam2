/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.spi.war;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.ops4j.pax.exam.TestProbeProvider;

/**
 * Provides a WAR probe as an input stream.
 *
 * @author Harald Wellmann
 *
 */
public class WarTestProbeProvider implements TestProbeProvider {

    private URI warUri;

    public WarTestProbeProvider(URI uri) {
        this.warUri = uri;
    }

    @Override
    public InputStream getStream() throws IOException {
        return warUri.toURL().openStream();
    }
}
