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
package org.ops4j.pax.exam.container.remote;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.container.remote.options.RBCLookupTimeoutOption;
import org.ops4j.pax.exam.container.remote.options.RBCPortOption;
import org.ops4j.pax.exam.spi.BuildingOptionDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.OptionUtils.*;

/**
 * Minimal parser for the rbcremote fragment.
 */
public class Parser
{

    private static Logger LOG = LoggerFactory.getLogger(Parser.class);
    private BuildingOptionDescription m_optionDescription;

    private String m_host = RBCPortOption.DEFAULTHOST;

    private Integer m_port = RBCPortOption.DEFAULTPORT;

    private long m_timeout;

    public Parser( Option[] options )
    {
        m_optionDescription = new BuildingOptionDescription( options );
        extractArguments( markingFilter( RBCPortOption.class, options ) );
        extractArguments( markingFilter( RBCLookupTimeoutOption.class, options ) );
    }

    private void extractArguments( RBCLookupTimeoutOption[] options )
    {
        for( RBCLookupTimeoutOption op : options )
        {
            m_timeout = op.getTimeout();
        }
    }

    private void extractArguments( RBCPortOption[] rbcPortOptions )
    {
        for( RBCPortOption op : rbcPortOptions )
        {
            m_host = op.getHost();
            m_port = op.getPort();
        }
    }

    public <T extends Option> T[] markingFilter( final Class<T> optionType,
                                                 final Option... options )
    {
        T[] inner = filter( optionType, options );
        m_optionDescription.markAsUsed( inner );
        return inner;
    }

    public OptionDescription getOptionDescription()
    {
        return m_optionDescription;
    }

    public String getHost()
    {
        return m_host;
    }

    public Integer getRMIPort()
    {
        return m_port;
    }

    public long getRMILookupTimpout()
    {
        return m_timeout;
    }
}
