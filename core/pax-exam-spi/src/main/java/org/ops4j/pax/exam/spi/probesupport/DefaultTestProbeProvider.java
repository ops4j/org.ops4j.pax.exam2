/*
 * Copyright (C) 2010 Okidokiteam
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
package org.ops4j.pax.exam.spi.probesupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;

/**
 * Static local provider.
 */
public class DefaultTestProbeProvider implements TestProbeProvider
{

    private static Logger LOG = LoggerFactory.getLogger( DefaultTestProbeProvider.class );

    private TestAddress[] m_tests;
    private Handle m_probe;
    private Store<InputStream> m_store;
    private String m_formattedInfo = "";

    public DefaultTestProbeProvider( TestAddress[] tests, Store<InputStream> store, Handle probe )
    {
        m_tests = tests;
        m_store = store;
        m_probe = probe;

        // Prepare text info

        m_formattedInfo = constuctInfo();

    }

    public TestAddress[] getTests()
    {
        return m_tests;
    }

    public InputStream getStream()
        throws IOException
    {
        return m_store.load( m_probe );
    }

    public String toString()
    {
        return m_formattedInfo;
    }

    // TODO: use some flat JSON Tree Model so we don't have to mess with formatting here.
    private String constuctInfo()
    {
        JarInputStream in = null;
        String info = "";
        try
        {
            info += "[Probe ID: " + m_probe.getIdentification() + "]\n";
            info += "[Probe Location: " + m_store.getLocation( m_probe ).toASCIIString() + "]\n";

            in = new JarInputStream( m_store.load( m_probe ) );
            Manifest man = in.getManifest();
            Attributes mainAttributes = man.getMainAttributes();

            info += "[Tests: " + "\n";
            for( TestAddress t : m_tests )
            {
                info += "    SIG=" + t + "\n";
            }
            info += "]" + "\n";

            info += "[Headers: " + "\n";
            for( Object key : mainAttributes.keySet() )
            {
                info += "    " + key + "=" + mainAttributes.get( key ) + "\n";
            }
            info += "]" + "\n";

            // surround
            info = "\n--\n" + info + "--\n";
        } catch( IOException e )
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                in.close();
            } catch( IOException e )
            {
                //
            }
        }
        return info;
    }
}
