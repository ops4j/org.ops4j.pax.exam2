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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.isEquinox;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleException;

public class EquinoxInternalReferenceTest
{

    @Test
    public void equinoxInternalReferenceHandler() throws BundleException, IOException
    {
        assumeTrue( isEquinox() );

        System.setProperty( "java.protocol.handler.pkgs",
            "org.eclipse.osgi.framework.internal.protocol" );
        String reference = "reference:file:" + PathUtils.getBaseDir() +
                "/target/regression-pde-bundle";
        URL url = new URL( reference );
        assertEquals( "reference", url.getProtocol() );
        InputStream is = url.openStream();
        assertNotNull( is );
        assertEquals( "org.eclipse.osgi.framework.internal.core.ReferenceInputStream", is
            .getClass().getName() );
    }
}
