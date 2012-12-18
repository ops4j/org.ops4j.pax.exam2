/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam;

import java.io.File;
import java.io.InputStream;

/**
 * Composite of {@link Customizer}s.
 * 
 * Used to treat many {@link Customizer}s as a single {@link Customizer}.
 * 
 * @author Toni Menzel
 */
public class CompositeCustomizer extends Customizer {

    private final Customizer[] customizers;

    public CompositeCustomizer(Customizer[] customizers) {
        this.customizers = customizers;
    }

    @Override
    public void customizeEnvironment(File workingFolder) {
        for (Customizer cust : customizers) {
            cust.customizeEnvironment(workingFolder);
        }
    }

    @Override
    public InputStream customizeTestProbe(InputStream testProbe) throws Exception {
        InputStream inputStream = testProbe;
        for (Customizer cust : customizers) {
            inputStream = cust.customizeTestProbe(inputStream);
        }
        return inputStream;
    }
}
