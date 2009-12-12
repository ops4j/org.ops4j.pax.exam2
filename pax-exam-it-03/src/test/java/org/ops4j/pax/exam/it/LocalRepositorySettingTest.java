package org.ops4j.pax.exam.it;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * @author Toni Menzel (tonit)
 * @since Apr 21, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class LocalRepositorySettingTest
{

    @Configuration
    public static Option[] configure()
    {
        String env = System.getProperty( "user.home" ) + "/.m2/repository";

        System.out.println( "Set to " + env );

        return options(
            // get from env:

            localRepository( env )
        );
    }

    @Test
    public void use()
    {

    }

}
