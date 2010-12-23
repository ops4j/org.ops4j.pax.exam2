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
package org.ops4j.pax.exam.nat.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.options.FrameworkOption;

/**
 * Stateful
 *
 * If would be really cool if this native runner would accept more than one osgi fw in classpath. Though this might not work due to dangerous cp
 * issues.
 *
 * @author Toni Menzel
 * @since Jan 7, 2010
 */
public class NativeTestContainerFactory implements TestContainerFactory
{

    public TestContainer[] parse( Option... options )
        throws TestContainerException
    {
        final FrameworkOption[] frameworkOptions = OptionUtils.filter(FrameworkOption.class, options);
        final Option[] filteredOptions = OptionUtils.remove( FrameworkOption.class, options );

        if( frameworkOptions.length == 0 )
        {
            return new TestContainer[] {
                    createContainer( filteredOptions )
            };
        }
        else
        {
            TestContainer[] containers = new TestContainer[ frameworkOptions.length ];
            for( int i = 0; i < containers.length; i++ )
            {
                Option[] opts = OptionUtils.combine( filteredOptions, frameworkOptions[ i ] );
                containers[i] = createContainer( opts );
            }
            return containers;
        }
    }

    private TestContainer createContainer( Option[] options )
    {
        NativeTestContainerParser parser = new NativeTestContainerParser( options );
        List<String> bundles = parser.getBundles();
        Map<String, String> props = parser.getProperties();
        return new NativeTestContainer( bundles, props );
    }

}
