/*
 * Copyright (C) 2011 Harald Wellmann
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
package org.ops4j.pax.exam.regression.multi.reference;

import static org.junit.Assume.assumeTrue;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.isEquinox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleException;

public class NoReferenceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void exceptionWithoutCustomHandler() throws BundleException, IOException {
        assumeTrue( isEquinox() );
        assumeTrue(System.getProperty("java.protocol.handler.pkgs") == null);
        
        expectedException.expect(MalformedURLException.class);
        expectedException.expectMessage("unknown protocol");

        String reference = "reference:file:" + PathUtils.getBaseDir() +
                "/target/regression-pde-bundle";
        new URL(reference);
    }
}
