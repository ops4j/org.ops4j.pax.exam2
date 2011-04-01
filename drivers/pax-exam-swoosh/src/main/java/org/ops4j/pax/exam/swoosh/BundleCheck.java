/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.swoosh;

import java.io.IOException;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.swoosh.probes.ResolverTestSet;

/**
 *
 */
public class BundleCheck extends AbstractProbe {

    public BundleCheck()
        throws IOException
    {
        super();
    }

    public BundleCheck allResolved()
        throws NoSuchMethodException
    {
        // assemble the test for this test
        addTest( ResolverTestSet.class, ResolverTestSet.class.getMethod( "resolveAll", BundleContext.class ) );
        return this;
    }


}
