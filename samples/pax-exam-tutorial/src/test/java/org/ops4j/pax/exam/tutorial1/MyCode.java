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
package org.ops4j.pax.exam.tutorial1;

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
        System.out.println( "Hello World! Me too !" );
    }

    public void runMeToo( BundleContext ctx )
    {
        System.out.println( "Hello World wdqwqw" );
        boolean found = false;
        String bund = "phoebe";
        for( Bundle b : ctx.getBundles() )
        {
            System.out.println( "Bee " + b.getSymbolicName() );
            if (bund.startsWith( b.getSymbolicName() )) {
                found = true;
            }
        }
        if (!found) {
            //throw new RuntimeException("You need a planon module, dude!");
        }
    }
}
