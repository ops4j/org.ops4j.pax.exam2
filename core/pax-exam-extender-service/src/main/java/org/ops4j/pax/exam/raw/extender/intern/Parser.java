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
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.ProbeInvokerFactory;
import org.ops4j.pax.swissbox.extender.ManifestEntry;
import org.ops4j.pax.swissbox.framework.ServiceLookup;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Toni Menzel
 * @since Jan 10, 2010
 */
public class Parser {
    final private Logger LOG = LoggerFactory.getLogger(Probe.class);

    final private Probe[] probes;

    public Parser(BundleContext ctx, String sigs, List<ManifestEntry> manifestEntries) {
        List<String> signatures = new ArrayList<String>();
        List<Probe> probes = new ArrayList<Probe>();
        signatures.addAll(Arrays.asList(sigs.split(",")));

        for (ManifestEntry manifestEntry : manifestEntries) {
            LOG.debug("Test " + manifestEntry.getKey() + " to be in " + sigs);
            if (signatures.contains(manifestEntry.getKey())) {
                probes.add(make(ctx, manifestEntry.getKey(), manifestEntry.getValue()));
            }
        }

        this.probes = probes.toArray(new Probe[probes.size()]);
    }

    private Probe make(BundleContext ctx, String sig, String expr) {
        // should be a service really
        // turn this expression into a service detail later
        LOG.debug("Registering Service: " + ProbeInvoker.class.getName() + " with Probe-Signature=\"" + sig + "\" and expression=\"" + expr + "\"");
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put("Probe-Signature", sig);
        return new Probe(ProbeInvoker.class.getName(), createInvoker( ctx, expr ), props);
    }

    private ProbeInvoker createInvoker( BundleContext ctx, String expr )
    {
        String invokerType = System.getProperty("pax.exam.invoker");
        if (invokerType == null) {
            return new ProbeInvokerImpl(expr, ctx);
        }
        else {
            Map<String,String> props = new HashMap<String, String>();
            props.put( "driver", invokerType );
            ProbeInvokerFactory factory = ServiceLookup.getService( ctx, ProbeInvokerFactory.class, props );
            return factory.createProbeInvoker( ctx, expr );
        }
    }

    public Probe[] getProbes() {
        return probes;
    }
}
