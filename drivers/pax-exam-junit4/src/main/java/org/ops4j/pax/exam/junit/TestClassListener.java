package org.ops4j.pax.exam.junit;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClassListener extends RunListener
{
    private static Logger LOG = LoggerFactory.getLogger( TestClassListener.class );
    
    private Class<?> testClass;
    
    public TestClassListener(Class<?> testClass)
    {
        this.testClass = testClass;
    }
    
    @Override
    public void testRunFinished( Result result ) throws Exception
    {
        LOG.info ("finished {}", testClass);
        ReactorManager.getInstance().testClassFinished( testClass );
    }
}
