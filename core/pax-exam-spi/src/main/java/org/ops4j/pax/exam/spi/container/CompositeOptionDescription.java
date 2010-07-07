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
package org.ops4j.pax.exam.spi.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.TestContainer;

/**
 * Created by IntelliJ IDEA.
 * User: tonit
 * Date: Jul 5, 2010
 * Time: 9:37:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class CompositeOptionDescription implements OptionDescription
{

    private List<OptionDescription> m_list;
    private Option[] m_used;
    private Option[] m_unused;

    public CompositeOptionDescription( List<OptionDescription> descriptions )
    {
        m_list = descriptions;
        final List<Option> used = new ArrayList<Option>();
        final List<Option> unused = new ArrayList<Option>();

        for( OptionDescription option : m_list )
        {
            used.addAll( Arrays.asList( option.getUsedOptions() ) );
            unused.addAll( Arrays.asList( option.getIgnoredOptions() ) );
        }
        m_used = used.toArray( new Option[used.size()] );
        m_unused = unused.toArray( new Option[unused.size()] );

    }

    public Option[] getUsedOptions()
    {
        return m_used;
    }

    public Option[] getIgnoredOptions()
    {
        return m_unused;
    }


}
