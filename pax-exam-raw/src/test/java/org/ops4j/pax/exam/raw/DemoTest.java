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

import org.junit.Test;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.nat.internal.NativeTestContainer;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.raw.extender.ProbeInvoker;
import org.ops4j.pax.exam.spi.container.TestContainer;

import static org.ops4j.pax.exam.Constants.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.raw.DefaultRaw.*;

/**
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public class DemoTest
{

    @Test
    public void nativeDemo()
        throws Exception
    {

        TestProbeBuilder probe = createProbe()
            .addTest( call( MyCode.class, "runMe" ) )
            .addTest( call( MyCode.class, "runMeToo" ) )
            .setAnchor( MyCode.class );

        TestContainer container = new NativeTestContainer(
            options( getOptions() )
        );

        container.start();

        // install the probe(s)
        long id = container.installBundle( probe.get().getProbe() );
        container.startBundle( id );

        // run them
        for( ProbeCall call : probe.getTests() )
        {
            execute( container, call );
        }

        container.stop();
    }

    public Option getOptions()
    {

        // always add the junit extender
        final DefaultCompositeOption option = new DefaultCompositeOption(
            mavenBundle()
                .groupId( "org.ops4j.pax.exam" )
                .artifactId( "pax-exam" )
                .version( Info.getPaxExamVersion() )
                .update( Info.isPaxExamSnapshotVersion() )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle()
                .groupId( "org.ops4j.pax.logging" )
                .artifactId( "pax-logging-api" )
                .version( "1.4" )
                //.version( "1.4.1-SNAPSHOT" )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle()
                .groupId( "org.ops4j.pax.logging" )
                .artifactId( "pax-logging-service" )
                .version( "1.4" )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle()
                .groupId( "org.osgi" )
                .artifactId( "org.osgi.compendium" )
                .version( "4.2.0" )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),

            /**

             mavenBundle()
             .groupId( "org.ops4j.pax.exam" )
             .artifactId( "pax-exam-raw-extender" )
             .version( Info.getPaxExamVersion() )
             .update( Info.isPaxExamSnapshotVersion() )
             .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
             **/
            mavenBundle()
                .groupId( "org.ops4j.pax.exam" )
                .artifactId( "pax-exam-raw-extender-impl" )
                .version( Info.getPaxExamVersion() )
                .update( Info.isPaxExamSnapshotVersion() )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES )
        );
        return option;
    }
}
