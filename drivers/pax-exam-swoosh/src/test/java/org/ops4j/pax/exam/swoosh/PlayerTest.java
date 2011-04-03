package org.ops4j.pax.exam.swoosh;

import org.junit.Test;
import org.ops4j.pax.exam.nat.internal.NativeTestContainerFactory;

/**
 * An entire test harness in a tweet.
 */
public class PlayerTest {

    @Test
    public void play()
        throws Exception
    {
        new Player().with( new PaxLoggingParts( "1.6.1" ) ).play( new BundleCheck().allResolved() );
    }
}
