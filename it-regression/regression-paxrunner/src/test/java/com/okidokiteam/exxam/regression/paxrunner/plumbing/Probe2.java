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
package com.okidokiteam.exxam.regression.paxrunner.plumbing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Another SingleTestProbe.
 */
public class Probe2
{

    private static Logger LOG = LoggerFactory.getLogger( SingleTestProbe.class );

    public void myOwnClass()
    {
        LOG.info( "INSIDE OSGI " + Probe2.class.getName() + " Method withoutBCTest" );
    }
}
