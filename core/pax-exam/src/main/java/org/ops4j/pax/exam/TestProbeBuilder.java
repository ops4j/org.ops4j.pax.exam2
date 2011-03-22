package org.ops4j.pax.exam;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public interface TestProbeBuilder
{

    TestAddress addTest( Class clazz, Method m );

     List<TestAddress> addTests( Class clazz, Method... m );

    TestProbeBuilder setAnchor( Class anchor );

    TestProbeBuilder setHeader( String key, String value );

    TestProbeBuilder ignorePackageOf( Class... classes );

    TestProbeProvider build();
}
