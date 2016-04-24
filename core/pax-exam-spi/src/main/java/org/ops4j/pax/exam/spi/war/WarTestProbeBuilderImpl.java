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

import java.io.File;
import java.net.URI;

import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.pax.exam.spi.DefaultExamSystem;

/**
 * Builds a WAR probe.
 *
 * @author Harald Wellmann
 *
 */
public class WarTestProbeBuilderImpl implements TestProbeBuilder {

    private File tempDir;
    private WarProbeOption option;
    private DefaultExamSystem system;

    public WarTestProbeBuilderImpl(File tempDir, DefaultExamSystem system) {
        this.tempDir = tempDir;
        this.system = system;
    }

    public WarTestProbeBuilderImpl(File tempDir, WarProbeOption option) {
        this.tempDir = tempDir;
        this.option = option;
    }

    @Override
    public void addTest(Class<?> clazz) {
    }

    @Override
    public TestProbeBuilder setHeader(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestProbeBuilder ignorePackageOf(Class<?>... classes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestProbeProvider build() {
        if (option == null) {
            if (system == null) {
                option = new WarProbeOption().classPathDefaultExcludes();
            }
            else {
                option = system.getLatestWarProbeOption();
            }
        }
        WarBuilder warBuilder = new WarBuilder(tempDir, option);
        URI warUri = warBuilder.buildWar();
        return new WarTestProbeProvider(warUri);
    }

    @Override
    public File getTempDir() {
        return tempDir;
    }

    @Override
    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }
}
