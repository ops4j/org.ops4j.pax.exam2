package org.examples.twitterclient.application;

import com.google.inject.AbstractModule;
import org.examples.twitterclient.api.TwitterService;
import org.examples.twitterclient.api.TwitterBackend;
import org.examples.twitterclient.backend.TwitterBackendImpl;
import org.examples.twitterclient.service.MockTwitterImpl;

/**
 * Created by IntelliJ IDEA.
 * User: tonit
 * Date: Sep 1, 2009
 * Time: 10:13:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterTestingModule extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind( TwitterService.class ).to( MockTwitterImpl.class );
        bind( TwitterBackend.class ).to( TwitterBackendImpl.class );
    }
}
