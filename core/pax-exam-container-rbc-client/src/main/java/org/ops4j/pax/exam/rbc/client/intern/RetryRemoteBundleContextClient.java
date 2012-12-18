/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.rbc.client.intern;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.NoSuchObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;

/**
 *
 */
public class RetryRemoteBundleContextClient implements RemoteBundleContextClient {

    private static final Logger LOG = LoggerFactory.getLogger( RetryRemoteBundleContextClient.class );
    private static final int RETRY_WAIT = 500;

    final private RemoteBundleContextClient proxy;

    final private int maxRetry;

    public RetryRemoteBundleContextClient( final RemoteBundleContextClient client, int maxRetries )
    {
        maxRetry = maxRetries;

        proxy = (RemoteBundleContextClient) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{ RemoteBundleContextClient.class },
            new InvocationHandler() {

                public Object invoke( Object o, Method method, Object[] objects )
                    throws Throwable
                {
                    Object ret = null;
                    Exception lastError = null;
                    // invoke x times or fail.
                    boolean retry = false;
                    int triedTimes = 0;
                    do {
                        try {
                            LOG.debug( "Call RBC." + method.getName() + " (retries: " + triedTimes + ")" );
                            triedTimes++;
                            if( retry ) { Thread.sleep( RETRY_WAIT ); }
                            ret = method.invoke( client, objects );
                            retry = false;
                        } catch( Exception ex ) {
                            lastError = ex;
                            Throwable cause = ExceptionHelper.unwind( ex );
                            boolean contain = ExceptionHelper.hasThrowable( ex, NoSuchObjectException.class );
                            if( contain ) {
                                LOG.warn( "Catched (rooted) " + cause.getClass().getName() + " in RBC." + method.getName() );
                                retry = true;
                            }
                            else {
                                LOG.debug( "Exception that does not cause Retry : (rooted) " + cause.getClass().getName() + " in RBC." + method.getName(), cause );
                                // just escape
                                throw lastError;
                            }
                        }
                    } while( retry && maxRetry > triedTimes );
                    // check if we need to throw an exception:

                    if( ( retry ) && ( lastError != null ) ) {
                        throw new Exception( lastError );
                    }
                    LOG.debug( "Return RBC." + method.getName() + " with: " + ret );

                    return ret;
                }
            }
        );
    }

    public long install( String location, InputStream stream )
    {
        return proxy.install( location, stream );
    }

    public void cleanup()
    {
        proxy.cleanup();
    }

    public void setBundleStartLevel( long bundleId, int startLevel )
    {
        proxy.setBundleStartLevel( bundleId, startLevel );
    }

    public void start()
    {
        proxy.start();
    }

    public void stop()
    {
        proxy.stop();
    }

    public void waitForState( long bundleId, int state, RelativeTimeout timeout )
    {
        proxy.waitForState( bundleId, state, timeout );
    }

    public void call( TestAddress address )
    {
        proxy.call( address );
    }
}
