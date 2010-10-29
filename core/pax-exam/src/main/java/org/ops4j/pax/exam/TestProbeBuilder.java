package org.ops4j.pax.exam;

/**
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public interface TestProbeBuilder
{

    TestProbeBuilder addTest( TestAddress... calls );

    TestProbeBuilder addTest( Class... clazz );

    TestProbeBuilder setAnchor( Class anchor );

    TestProbeBuilder setHeader( String key, String value );

    TestProbeBuilder ignorePackageOf( Class... classes );

    TestProbeProvider build();
}
