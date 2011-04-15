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
package org.ops4j.pax.exam.container.remote;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestTarget;
import org.ops4j.pax.exam.TimeoutException;

/**
 * Graceful delegator to underlying target instance.
 * All other TestContainer services are do nothing calls.
 *
 * @author Toni Menzel
 * @since Jan 26, 2010
 */
public class RBCRemoteContainer implements TestContainer
{

    final private TestTarget m_target;

    public RBCRemoteContainer( TestTarget target )
    {
        m_target = target;
    }

    public void call( TestAddress address )
    {
        m_target.call( address );
    }

    public long install( InputStream stream )
    {
        return m_target.install( stream );
    }

    public void cleanup( )
    {
        m_target.cleanup( );
    }

    public void setBundleStartLevel( long bundleId, int startLevel )
        throws TestContainerException
    {
        // do nothing
    }

    public TestContainer start()
        throws TimeoutException
    {
        // do nothing
        return this;
    }

    public TestContainer stop()
        throws TimeoutException
    {
        // implicit cleanup
        cleanup();
        return this;
    }

    public void waitForState( long bundleId, int state, long timeoutInMillis )
        throws TimeoutException
    {
        // do nothing
    }


}
