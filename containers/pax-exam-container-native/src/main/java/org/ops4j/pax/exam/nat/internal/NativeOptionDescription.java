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
package org.ops4j.pax.exam.nat.internal;

import java.util.ArrayList;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.spi.BuildingOptionDescription;

import static org.ops4j.pax.exam.Constants.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.*;

/**
 *
 */
public class NativeOptionDescription implements OptionDescription
{

    private BuildingOptionDescription m_optionDescription;
    private ArrayList<String> m_bundles;

    public NativeOptionDescription( Option[] options )
    {
        options = expand( combine( localOptions(), options ) );
        // install url handlers:
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        m_optionDescription = new BuildingOptionDescription( options );
        // catch all bundles
        m_bundles = new ArrayList<String>();
        for( Option option : options )
        {
            if( option instanceof ProvisionOption )
            {
                m_optionDescription.markAsUsed( option );
                m_bundles.add( ( (ProvisionOption) option ).getURL() );
            }
        }
    }

    public Option[] getUsedOptions()
    {
        return m_optionDescription.getUsedOptions();
    }

    public Option[] getIgnoredOptions()
    {
        return m_optionDescription.getIgnoredOptions();
    }

    public TestContainer getContainer()
    {
        return new NativeTestContainer( m_bundles );
    }

    private Option[] localOptions()
    {
        return new Option[]{
            bootDelegationPackage( "sun.*" ),
            mavenBundle()
                .groupId( "org.ops4j.pax.logging" )
                .artifactId( "pax-logging-api" )
                .version( "1.4" )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle()
                .groupId( "org.osgi" )
                .artifactId( "org.osgi.compendium" )
                .version( "4.2.0" )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle()
                .groupId( "org.ops4j.pax.exam" )
                .artifactId( "pax-exam-extender-service" )
                .version( Info.getPaxExamVersion() )
                .update( Info.isPaxExamSnapshotVersion() )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES )
        };
    }
}
