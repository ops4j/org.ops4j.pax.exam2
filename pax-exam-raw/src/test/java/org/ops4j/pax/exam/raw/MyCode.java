/*
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.ops4j.pax.exam.raw;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * This class can be a minimal pax exam test using pax exam raw api.
 *
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public class MyCode
{

    public void runMe()
    {
        System.out.println( "Hello World!" );
    }

    public void runMeToo( BundleContext ctx )
    {
        System.out.println( "Hello World 2" );
        for( Bundle b : ctx.getBundles() )
        {
            System.out.println( "Have " + b.getSymbolicName() );
        }
    }
}
