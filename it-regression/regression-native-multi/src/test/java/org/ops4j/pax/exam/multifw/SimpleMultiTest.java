package org.ops4j.pax.exam.multifw;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.ops4j.pax.exam.spi.container.PlumbingContext;

import static org.ops4j.pax.exam.LibraryOptions.*;

/**
 *
 */
public class SimpleMultiTest {

    @Test
    public void bareRunTest()
        throws Exception
    {
        Option[] options = new Option[]{
            junitBundles(),
            easyMockBundles()
        };

        TestProbeProvider p = makeProbe();

        // the parse will split all single containers
        for( TestContainer testContainer : PaxExamRuntime.getTestContainerFactory().parse( options ) ) {
            try {
                testContainer.start();
                testContainer.install( p.getStream() );
                for( TestAddress test : p.getTests() ) {
                    testContainer.call( test );
                }
            } finally {
                testContainer.stop();
            }
        }
    }

    private TestProbeProvider makeProbe()
        throws IOException
    {
        TestProbeBuilder probe = new PlumbingContext().createProbe();
        probe.addTests( Probe.class, getAllMethods( Probe.class ) );
        return probe.build();
    }

    private Method[] getAllMethods( Class c )
    {
        List<Method> methods = new ArrayList<Method>();
        for( Method m : c.getDeclaredMethods() ) {
            if( m.getModifiers() == Modifier.PUBLIC ) {
                methods.add( m );
            }
        }
        return methods.toArray( new Method[ methods.size() ] );

    }
}
