package org.ops4j.pax.exam.raw;

import java.io.InputStream;

/**
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public interface TestProbeBuilder
{

    TestProbeBuilder addTest( ProbeCall call );

    InputStream get();

    TestProbeBuilder setAnchor( Class anchor );

    ProbeCall[] getTests();
}
