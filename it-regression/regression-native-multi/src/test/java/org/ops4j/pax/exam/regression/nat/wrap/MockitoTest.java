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
package org.ops4j.pax.exam.regression.nat.wrap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.BundleContext;

/**
 * This test shows
 * <ul>
 * <li>how to use the {@code wrappedBundle()} option</li>
 * <li>how to set the version of exported packages with {@code wrappedBundle()}.</li>
 * <li>how to use both Mockito and JUnit and avoid a conflict with the Hamcrest 
 *     library which is embedded in each of the two by default.</li> 
 * 
 * </ul>
 * See PAXEXAM-274.
 * 
 * @author Harald Wellmann
 *
 */
@RunWith( JUnit4TestRunner.class )
@ExamReactorStrategy( AllConfinedStagedReactorFactory.class )
public class MockitoTest
{

    @Configuration
    public Option[] config1()
    {
        return options(
            // Mockito with Hamcrest and Objenesis embedded
            mavenBundle( "org.mockito", "mockito-all", "1.8.5" ),
            
            // JUnit without Hamcrest
            wrappedBundle( mavenBundle( "junit", "junit-dep", "4.8.1" ) ).exports( "*;version=4.8.1" ) );
    }

    @Configuration
    public Option[] config2()
    {
        return options(
            // Mockito without Hamcrest and Objenesis
            mavenBundle( "org.mockito", "mockito-core", "1.8.5" ),
            
            // Hamcrest with a version matching the range expected by Mockito
            mavenBundle( "org.hamcrest", "com.springsource.org.hamcrest.core", "1.1.0" ),
            
            // Objenesis with a version matching the range expected by Mockito
            wrappedBundle( mavenBundle( "org.objenesis", "objenesis", "1.2" ) ).exports( "*;version=1.2" ),
            
            // The default JUnit bundle also exports Hamcrest, but with an (incorrect) version of
            // 4.8.1 which does not match the Mockito import.
            junitBundles() );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void createMock( BundleContext bc )
    {
        List<String> list = mock( List.class );
        when( list.size() ).thenReturn( 0 );
        int size = list.size();
        assertEquals( 0, size );
    }
}
