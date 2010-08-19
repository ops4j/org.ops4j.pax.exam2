package org.ops4j.pax.exam.spi;

/**
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public interface TestProbeBuilder extends TestProbeProvider
{

    TestProbeBuilder addTest( TestAddress... calls );

    TestProbeBuilder addTest( Class... clazz );
    
    TestProbeBuilder setAnchor( Class anchor );
}
