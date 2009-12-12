/*
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.exam.options;

import org.ops4j.pax.exam.Option;

/**
 * Option specifying a timeout (in milliseconds).
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0 April 23, 2009
 */
public class TimeoutOption
    implements Option
{

    /**
     * Timeout in milliseconds (cannot be null).
     */
    private final long m_timeout;

    /**
     * Constructor.
     *
     * @param timeoutInMillis timeout (in millis)
     */
    public TimeoutOption( final long timeoutInMillis )
    {
        m_timeout = timeoutInMillis;
    }

    /**
     * Getter.
     *
     * @return timeout in millis (cannot be null)
     */
    public long getTimeout()
    {
        return m_timeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( this.getClass().getSimpleName() )
            .append( "{timeout=" ).append( m_timeout ).append( '}' )
            .toString();
    }

}