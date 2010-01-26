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
package org.ops4j.pax.exam.raw.extender.intern;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.raw.extender.ProbeInvoker;
import org.ops4j.pax.swissbox.extender.ManifestEntry;

/**
 * @author Toni Menzel
 * @since Jan 10, 2010
 */
public class Parser
{

    private Probe[] m_probes;

    public Parser( BundleContext ctx, String sigs, List<ManifestEntry> manifestEntries )
    {
        List<String> signatures = new ArrayList<String>();
        List<Probe> probes = new ArrayList<Probe>();

        // read signatures
        for( String sig : sigs.split( "," ) )
        {
            signatures.add( sig );
        }
        for( ManifestEntry manifestEntry : manifestEntries )
        {
            if( signatures.contains( manifestEntry.getKey() ) )
            {
                probes.add( make( ctx, manifestEntry.getKey(), manifestEntry.getValue() ) );
            }
        }

        m_probes = probes.toArray( new Probe[probes.size()] );
    }

    private Probe make( BundleContext ctx, String sig, String expr )
    {
        // should be a service really
        // turn this expression into a service detail later
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put( "Probe-Signature", sig );
        return new Probe( ProbeInvoker.class.getName(), new ProbeInvokerImpl( expr, ctx ), props );
    }

    public Probe[] getProbes()
    {
        return m_probes;
    }
}
