package org.ops4j.pax.exam.junit;

import java.lang.reflect.Method;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

public class PaxExamServer extends ExternalResource
{
    private Class<?> configClass;
    private TestContainer testContainer;

    public PaxExamServer()
    {
    }

    public PaxExamServer( Class<?> configClass )
    {
        this.configClass = configClass;
    }

    @Override
    public Statement apply( Statement base, Description description )
    {
        if( configClass == null )
        {
            configClass = description.getTestClass();
        }
        return super.apply( base, description );
    }

    @Override
    protected void before() throws Throwable
    {
        Option[] options = getConfigurationOptions();
        ExamSystem system = PaxExamRuntime.createServerSystem( options );
        testContainer = PaxExamRuntime.createContainer( system );
        testContainer.start();
    }

    @Override
    protected void after()
    {
        testContainer.stop();
    }

    private Option[] getConfigurationOptions() throws Exception
    {
        Method m = getConfigurationMethod( configClass );
        Object configClassInstance = configClass.newInstance();
        Option[] options = (Option[]) m.invoke( configClassInstance );
        return options;
    }

    private Method getConfigurationMethod( Class<?> klass )
    {
        Method[] methods = klass.getMethods();
        for( Method m : methods )
        {
            Configuration conf = m.getAnnotation( Configuration.class );
            if( conf != null )
            {
                return m;
            }
        }
        throw new IllegalArgumentException( klass.getName() + " has no @Configuration method" );
    }

}
