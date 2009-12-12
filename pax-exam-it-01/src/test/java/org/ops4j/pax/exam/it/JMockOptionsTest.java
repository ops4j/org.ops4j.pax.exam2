package org.ops4j.pax.exam.it;

import java.util.List;
import org.jmock.Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import static org.ops4j.pax.exam.junit.JUnitOptions.*;

/**
 * Simpliest usecase for jmock bundles support.
 *
 * @author Toni Menzel (tonit)
 * @since Mar 15, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class JMockOptionsTest
{

    @Configuration
    public static Option[] rootConfig()
    {
        return options(
            jmockBundles().version( "2.5.1" )
        );
    }

    @Test
    public void usage()
    {
        Mockery context = new Mockery();
        List list = context.mock( List.class );

        context.assertIsSatisfied();
    }
}
