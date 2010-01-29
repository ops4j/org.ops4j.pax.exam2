package org.ops4j.pax.exam.showcase;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundles;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * This example shows how you can use {@link Customizer} and {@link TinyBundles} to customize the test probe how you
 * want to. You could also add/remove resources of cause. This example just shows a "common" usage to "fix" some headers
 * in the testprobe.
 *
 * @author Toni Menzel (tonit)
 * @since Oct 02, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class OverwriteTestProbe
{

    @Configuration
    public static Option[] configure()
    {
        return options(

            new Customizer()
            {
                @Override
                public InputStream customizeTestProbe( InputStream testProbe )
                    throws IOException
                {
                    return TinyBundles.modifyBundle( testProbe ).
                        removeHeader( Constants.EXPORT_PACKAGE )
                        .set( Constants.BUNDLE_SYMBOLICNAME, "HelloWorld" )
                        .build();
                }
            }
        );
    }

    @Test
    public void show( BundleContext context )

    {
        // test that our testprobe has been changed like we wanted to:
        assertEquals( "HelloWorld", context.getBundle().getHeaders().get( Constants.BUNDLE_SYMBOLICNAME ) );
        assertNull( context.getBundle().getHeaders().get( Constants.EXPORT_PACKAGE ) );
    }
}
