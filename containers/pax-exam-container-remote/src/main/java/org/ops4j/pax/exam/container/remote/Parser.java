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
package org.ops4j.pax.exam.container.remote;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.remote.options.RBCLookupTimeoutOption;
import org.ops4j.pax.exam.container.remote.options.RBCPortOption;

import static org.ops4j.pax.exam.OptionUtils.*;

/**
 * Minimal parser for the rbcremote fragment.
 */ 
public class Parser
{
    private String m_host;

    private Integer m_port;

    private long m_timeout;

    public Parser( Option[] options )
    {
        extractArguments( filter( RBCPortOption.class, options ) );
        extractArguments( filter( RBCLookupTimeoutOption.class, options ) );
        assert m_port != null : "Port should never be null.";
        assert m_host != null : "Host should never be null.";

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

    public Integer getPort()
    {
        return m_port;
    }
}
