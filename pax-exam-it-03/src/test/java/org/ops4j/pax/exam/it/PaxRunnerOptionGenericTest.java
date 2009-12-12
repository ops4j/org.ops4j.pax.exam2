package org.ops4j.pax.exam.it;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * @author Toni Menzel (tonit)
 * @since Apr 21, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class PaxRunnerOptionGenericTest
{

    @Configuration
    public static Option[] configure()
    {

        return options(
            // get from env:
            rawPaxRunnerOption( "platform=equinox" ),
            rawPaxRunnerOption( "--profiles=web" ),
            rawPaxRunnerOption( "--foo" )

        );
    }

    @Test
    public void use()
    {

    }

}
