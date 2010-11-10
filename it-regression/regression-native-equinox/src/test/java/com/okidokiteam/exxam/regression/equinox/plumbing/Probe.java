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
package com.okidokiteam.exxam.regression.equinox.plumbing;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * External TestProbe.
 * Assemble yourself using:
 * createProbe().addTest( Probe.class )
 */
public class Probe
{

    private static Logger LOG = LoggerFactory.getLogger( Probe.class );

    public void withoutBCTest()
    {
        LOG.info( "INSIDE OSGI " + Probe.class.getName() + " Method withoutBCTest" );
    }

    public void withBCTest( BundleContext ctx )
    {
        LOG.info( "INSIDE OSGI " + Probe.class.getName() + " Method withBCTest Context: " + ctx );
    }

    private void neverCall()
    {
        fail( "Don't call me !" );
    }
}
