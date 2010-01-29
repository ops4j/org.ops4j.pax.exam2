package org.ops4j.pax.exam.spi.container;

import java.io.InputStream;

/**
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public interface TestProbeBuilder extends TestProbeProvider
{

    TestProbeBuilder addTest( ProbeCall... calls );

    TestProbeBuilder addTest( Class... clazz );
    
    TestProbeBuilder setAnchor( Class anchor );

}
