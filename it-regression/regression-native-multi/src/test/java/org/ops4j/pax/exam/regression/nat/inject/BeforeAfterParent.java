package org.ops4j.pax.exam.regression.nat.inject;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.util.PathUtils;

public class BeforeAfterParent
{
    @Configuration
    public Option[] config()
    {
        return options(
            url( "reference:file:" + PathUtils.getBaseDir() +
                    "/../regression-pde-bundle/target/regression-pde-bundle.jar" ),
            mavenBundle( "org.apache.geronimo.specs", "geronimo-atinject_1.0_spec", "1.0" ),
            mavenBundle( "org.ops4j.pax.exam", "pax-exam-inject", "2.2.1-SNAPSHOT" ),
            mavenBundle( "org.ops4j.pax.exam", "pax-exam-invoker-junit", "2.2.1-SNAPSHOT" ),
            systemProperty( "pax.exam.inject" ).value( "true" ),
            systemProperty( "pax.exam.invoker" ).value( "junit" ),
            junitBundles() );
    }

    @Before
    public void before()
    {
        addMessage( "Before in parent" );
    }

    @After
    public void after()
    {
        addMessage( "After in parent" );
    }

    public static void clearMessages() throws BackingStoreException
    {
        Preferences prefs = Preferences.userNodeForPackage( BeforeAfterParent.class );
        prefs.clear();
        prefs.sync();
    }

    public static void addMessage( String message )
    {
        Preferences prefs = Preferences.userNodeForPackage( BeforeAfterParent.class );
        int numMessages = prefs.getInt( "numMessages", 0 );
        prefs.put( "message." + numMessages, message );
        prefs.putInt( "numMessages", ++numMessages );
        try
        {
            prefs.sync();
        }
        catch ( BackingStoreException exc )
        {
            throw new TestContainerException( exc );
        }
    }
}
