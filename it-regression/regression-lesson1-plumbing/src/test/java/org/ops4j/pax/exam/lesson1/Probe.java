/*
 * Copyright (C) 2011 Toni Menzel
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
package org.ops4j.pax.exam.lesson1;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Your first "probe". Well not really Probe. Its the class that will be put in a jar on-the-fly.
 * Pax Exam will calculate a fairly relaxed Manifest Metadata for you.
 * You don't have access to the manifest at this point.
 *
 * Everything that you use (classes) must be exported by othher bundles in your setup.
 * Its import to understand that this class will be put into a bundle, possibly transfered to another JVM (depending on which TestContainer you pick), and executed remotly.
 * Consider this, specially regarding Exceptions, Parameters etc.
 *
 * A Probe must also have a public no argument constructor, like the one you get by default (no -constructor specified).
 *
 * Tests that are being add later must have public visibility.
 *
 * Parameters:
 * They have a special meaning.
 * You can have:
 * - no argument
 * - BundleContext, which you will get from the OSGi Runtime
 * - BundlContext + more parameters: those are being passed in at "call" time. Specially useful when writing tests that are being re-used. This gives you some flexibility.
 */
public class Probe {

    @SuppressWarnings( "unused" )
    public void probe1()
    {
        System.out.println( "----- > Inside OSGi. No Bundle Context :( " );
    }

    @SuppressWarnings( "unused" )
    public void probe2( BundleContext ctx )
    {
        System.out.println( "----- > This Bundles name is " + ctx.getBundle().getSymbolicName() );
        for( Bundle b : ctx.getBundles() ) {
            System.out.println( "Bundle : " + b.getSymbolicName() );
        }
    }

    @SuppressWarnings( "unused" )
    public void probe3( BundleContext ctx, String parameter )
    {
        System.out.println( "Parameter: " + parameter );
    }
}
