package com.okidokiteam.exxam.regression.paxrunner;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;

/**
 * 
 */
public class CustomFactory implements TestContainerFactory{

    public TestContainer[] parse( Option... options )
        throws TestContainerException
    {
        return new TestContainer[ 0 ];
    }

    public void shutdown()
    {

    }
}
