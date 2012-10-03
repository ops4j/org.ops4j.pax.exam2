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
package org.ops4j.pax.exam;

/**
 * Denotes an exception occuring during using regression container that appears due to a timeout.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April, 23, 2009
 */
public class TimeoutException
    extends TestContainerException
{

    private static final long serialVersionUID = 8862461468344204444L;

    /**
     * {@inheritDoc}
     */
    public TimeoutException( final String message )
    {
        super( message );
    }

    /**
     * {@inheritDoc}
     */
    public TimeoutException( final String message,
                             final Throwable cause )
    {
        super( message, cause );
    }

    /**
     * {@inheritDoc}
     */
    public TimeoutException( final Throwable cause )
    {
        super( cause );
    }

}