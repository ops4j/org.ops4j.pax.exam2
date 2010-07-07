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
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.spi.BuildingOptionDescription;

import static org.ops4j.pax.exam.OptionUtils.*;

/**
 * Eats Options and spits out meta data used in factory as well as a OptionDescription
 */
public class NativeTestContainerParser
{

    final private ArrayList<String> m_bundles = new ArrayList<String>();
    final private BuildingOptionDescription m_desc;

    public NativeTestContainerParser( Option[] options )
    {
        m_desc = new BuildingOptionDescription( options );
        ProvisionOption[] bundleOptions = filter( ProvisionOption.class, options );
        for( ProvisionOption opt : bundleOptions )
        {
            m_bundles.add( opt.getURL() );
            m_desc.markAsUsed( opt );
        }
    }

    public ArrayList<String> getBundles()
    {
        return m_bundles;
    }

    public OptionDescription getDescription()
    {
        return new OptionDescription()
        {

            public Option[] getUsedOptions()
            {
                return m_desc.getUsedOptions();
            }

            public Option[] getIgnoredOptions()
            {
                return m_desc.getIgnoredOptions();
            }
        };
    }
}
