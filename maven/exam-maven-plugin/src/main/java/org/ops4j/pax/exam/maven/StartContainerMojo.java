/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.maven;

import static org.ops4j.pax.exam.maven.Constants.*;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.spi.DefaultExamSystem;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

/**
 * Starts a Pax Exam Forked Container in server mode for the given configuration class.
 * 
 * @goal start-container
 * @phase pre-integration-test
 * @requiresDependencyResolution test
 * @description Starts Pax Exam in server mode
 */
public class StartContainerMojo extends AbstractMojo
{

    /**
     * Fully qualified name of a Java class with a {@code @Configuration} method, providing the test
     * container configuration.
     * 
     * @parameter
     * @required
     */
    private String configClass;

    /**
     * Test classpath.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     */
    protected List<String> classpathElements;

    private ClasspathClassLoader testClassLoader;

    private TestContainer testContainer;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        /*
         * The test classpath of the project using this plugin is not visible to the classloader of
         * this plugin, but it may be needed to loaded classes and resources, e.g. from
         * META-INF/services. So we build our own class loader for the test classpath.
         */
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        try
        {
            ClasspathClassLoader cl = getTestClassLoader();
            Thread.currentThread().setContextClassLoader( cl );
            run( cl );
        }
        catch ( Exception e )
        {
            getLog().error( e );
            throw new MojoExecutionException( "Failed to start Pax Exam server for "
                    + configClass, e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( ccl );
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private void run( ClassLoader ccl ) throws Exception
    {
        /*
         * Make sure we can load use Pax URL protocol handles defined as client project
         * dependencies. 
         */
        URL.setURLStreamHandlerFactory( new PaxUrlStreamHandlerFactory( ccl ) );

        Option[] options = getConfigurationOptions();

        
        ExamSystem system = DefaultExamSystem.create( options ); 
        testContainer = PaxExamRuntime.createContainer( system );
        testContainer.start();

        Map context = getPluginContext();
        context.put( TEST_CONTAINER_KEY, testContainer );
    }

    private Option[] getConfigurationOptions() throws Exception
    {
        Class<?> klass = Class.forName( configClass, true, testClassLoader );
        Method m = getConfigurationMethod( klass );
        Object configClassInstance = klass.newInstance();
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

    protected ClasspathClassLoader getTestClassLoader()
    {
        if( testClassLoader == null )
        {
            try
            {
                testClassLoader = new ClasspathClassLoader( classpathElements );
            }
            catch ( MalformedURLException exc )
            {
                throw new IllegalStateException( "error in classpath", exc );
            }
        }
        return testClassLoader;
    }
}
