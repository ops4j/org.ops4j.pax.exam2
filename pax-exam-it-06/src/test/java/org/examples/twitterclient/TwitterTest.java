package org.examples.twitterclient;

import org.junit.Test;
import com.google.inject.Injector;
import com.google.inject.Guice;
import org.examples.twitterclient.api.TwitterService;
import org.examples.twitterclient.application.TwitterTestingModule;

/**
 * Created by IntelliJ IDEA.
 * User: tonit
 * Date: Sep 1, 2009
 * Time: 10:18:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterTest
{

    @Test
    public void doit()
    {
        Injector injector = Guice.createInjector( new TwitterTestingModule() );
        TwitterService service = injector.getInstance( TwitterService.class );
        service.send("Hello World");
    }
}
