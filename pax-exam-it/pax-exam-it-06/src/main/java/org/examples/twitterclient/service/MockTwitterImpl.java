package org.examples.twitterclient.service;

import com.google.inject.Inject;
import org.examples.twitterclient.api.TwitterService;
import org.examples.twitterclient.api.TwitterBackend;

/**
 * Created by IntelliJ IDEA.
 * User: tonit
 * Date: Sep 1, 2009
 * Time: 10:17:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class MockTwitterImpl implements TwitterService
{

    private TwitterBackend m_backend;

    @Inject
    public MockTwitterImpl( TwitterBackend backend )
    {
        m_backend = backend;
    }

    public void send( String s )
    {
        System.out.println( "Sending " + s + " with engine: " + m_backend );
    }

    public static class Foo
    {

        public Foo()
        {
            System.out.println( "Hello Magic" );
        }
    }
}
