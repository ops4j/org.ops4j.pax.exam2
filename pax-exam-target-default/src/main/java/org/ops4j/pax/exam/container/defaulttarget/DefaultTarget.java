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
package org.ops4j.pax.exam.container.defaulttarget;

import java.io.InputStream;
import org.ops4j.pax.exam.spi.container.TestContainerException;
import org.ops4j.pax.exam.spi.container.TestTarget;

/**
 * @author Toni Menzel
 * @since Jan 25, 2010
 */
public class DefaultTarget implements TestTarget
{

    /**
     * 
     */
    public DefaultTarget()
    {
           
    }

    public <T> T getService( Class<T> serviceType, String filter, long timeoutInMillis )
        throws TestContainerException
    {
        return null; 
    }

    public long installBundle( InputStream stream )
    {
        return 0; 
    }
}
