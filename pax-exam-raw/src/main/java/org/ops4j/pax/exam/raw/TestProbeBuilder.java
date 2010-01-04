package org.ops4j.pax.exam.raw;

/**
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public interface TestProbeBuilder
{

    TestProbeBuilder addTest( Class clazz, String method );

    TestProbe get();
}
