/*
 * Copyright (C) 2010 Toni Menzel
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
package org.ops4j.pax.exam.nat.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.ProvisionOption;

import static org.ops4j.pax.exam.Constants.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.*;

/**
 * Eats Options and spits out meta data used in factory
 * Its also the place where NativeContainer default options should be set.
 */
public class NativeTestContainerParser
{

    final private List<ProvisionOption> m_bundles = new ArrayList<ProvisionOption>();
    final private List<SystemPropertyOption> m_systemProperties = new ArrayList<SystemPropertyOption>();   
    final private Map<String, String> m_frameworkProperties = new HashMap<String, String>();

    public NativeTestContainerParser( final Option[] optionsIn )
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        final Option[]  options = expand( combine( localOptions(), optionsIn ) );
        SystemPropertyOption[] systemProperties = filter( SystemPropertyOption.class, options );
        for( SystemPropertyOption opt : systemProperties )
        {
        	// TODO: make dedicated option for framework properties.
        	// or get rid of it at all.
        	
        	m_frameworkProperties.put( opt.getKey(), opt.getValue() );
        	m_systemProperties.add(opt);
        }
        BootDelegationOption[] bootDelegations = filter( BootDelegationOption.class, options );
        StringBuilder sb = new StringBuilder();
        for ( BootDelegationOption opt : bootDelegations )
        {
            if ( sb.length() > 0 )
            {
                sb.append(",");
            }
            sb.append( opt.getPackage() );
        }
        if ( sb.length() > 0 ) {
        	m_frameworkProperties.put( "org.osgi.framework.bootdelegation" , sb.toString() );
        }
    }

    public List<ProvisionOption> getBundles()
    {
        return m_bundles;
    }

    public Map<String, String> getFrameworkProperties()
    {
        return m_frameworkProperties;
    }
    

    public List<SystemPropertyOption> getSystemProperties()
    {
        return m_systemProperties;
    }

    private Option[] localOptions()
    {
        return new Option[]{
            bootDelegationPackage( "sun.*" ),
            
            mavenBundle()
                .groupId( "org.osgi" )
                .artifactId( "org.osgi.compendium" )
                .version( "4.2.0" )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES )
            ,
            mavenBundle()
            	.groupId( "org.ops4j.pax.logging" )
            	.artifactId( "pax-logging-api" )
            	.version( "1.6.2" )
            	.startLevel( START_LEVEL_SYSTEM_BUNDLES )
        ,
            mavenBundle()
                .groupId( "org.ops4j.pax.exam" )
                .artifactId( "pax-exam-extender-service" )
                .version( Info.getPaxExamVersion() )
                .update( Info.isPaxExamSnapshotVersion() )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES )
        };
    }
}
