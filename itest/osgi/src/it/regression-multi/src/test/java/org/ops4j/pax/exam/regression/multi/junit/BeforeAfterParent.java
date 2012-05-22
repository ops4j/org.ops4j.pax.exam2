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
package org.ops4j.pax.exam.regression.multi.junit;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.util.PathUtils;

public class BeforeAfterParent
{
    @Configuration
    public Option[] config()
    {
        return options(
            regressionDefaults(),
            url( "reference:file:" + PathUtils.getBaseDir() +
                    "/target/regression-pde-bundle.jar" ),
            junitBundles() );
    }

    @Before
    public void before()
    {
        addMessage( "Before in parent" );
    }

    @After
    public void after()
    {
        addMessage( "After in parent" );
    }

    public static void clearMessages() throws BackingStoreException
    {
        Preferences prefs = Preferences.userNodeForPackage( BeforeAfterParent.class );
        prefs.clear();
        prefs.sync();
    }

    public static void addMessage( String message )
    {
        Preferences prefs = Preferences.userNodeForPackage( BeforeAfterParent.class );
        int numMessages = prefs.getInt( "numMessages", 0 );
        prefs.put( "message." + numMessages, message );
        prefs.putInt( "numMessages", ++numMessages );
        try
        {
            prefs.sync();
        }
        catch ( BackingStoreException exc )
        {
            throw new TestContainerException( exc );
        }
    }
}
