/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009 Toni Menzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.container.def.internal;

import static org.ops4j.pax.exam.OptionUtils.filter;

import java.io.IOException;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.CompositeCustomizer;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.container.def.AbstractTestContainer;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.handler.internal.URLUtils;
import org.ops4j.pax.runner.platform.StoppableJavaRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TestContainer} implementation using Pax Runner.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 09, 2008
 */
public class PaxRunnerTestContainer extends AbstractTestContainer
    implements TestContainer {

    private static final Logger LOG = LoggerFactory.getLogger( PaxRunnerTestContainer.class );
    

    /**
     * Underlying Test Target
     */
    final private StoppableJavaRunner m_javaRunner;
    final private String m_frameworkName;


	private ArgumentsBuilder argBuilder;

    /**
     * Constructor.
     *
     * @param javaRunner java runner to be used to start up Pax Runner
     */
    public PaxRunnerTestContainer( final StoppableJavaRunner javaRunner,
                                   String host,
                                   int port,
                                   Option[] options )
    {
    	super(host, port, options);
        LOG.info( "New PaxRunnerTestContainer " );

        m_javaRunner = javaRunner;
       
        // find the framework name:
        FrameworkOption[] frameworkOptions = filter( FrameworkOption.class, options );
        // expect it to be exactly one:
        m_frameworkName = frameworkOptions[ 0 ].getName();
    }
    
    protected String getInfo() {
		return "Pax Runner " + Info.getPaxRunnerVersion() ;
	}

    @Override
    protected void customizer() {
    	new CompositeCustomizer( argBuilder.getCustomizers() ).customizeEnvironment( argBuilder.getWorkingFolder() );
    }
    
    @Override
    protected long getRMITimeout() {
    	return argBuilder.getStartTimeout();
    }
    
    @Override
    protected long getSystemBundleId() {
    	return SYSTEM_BUNDLE;
    }
    
    @Override
    protected long getStartTimeout() {
    	return argBuilder.getStartTimeout();
    }
    @Override
    protected void parseOption(String m_host, int m_port, Option[] args) throws IOException {
    	argBuilder = new ArgumentsBuilder( m_host, m_port, args );
	}
		
    @Override
    protected void startProcess() throws Exception {
    	long startedAt = System.currentTimeMillis();
        URLUtils.resetURLStreamHandlerFactory();
        String[] arguments = argBuilder.getArguments();

        LOG.info( "Pax Runner Arguments: ( " + arguments.length + ")" );
        for( String s : arguments ) {
            LOG.info( "#   " + s );
        }

        Run.start( m_javaRunner, arguments );
        LOG.info( "Test container (Pax Runner " + Info.getPaxRunnerVersion() + ") started in "
                  + ( System.currentTimeMillis() - startedAt ) + " millis"
        );
    }

    @Override
    protected void stopProcess() {
         if( m_javaRunner != null ) {
             m_javaRunner.shutdown();
         }
    }

    @Override
    public String toString()
    {
        return "PaxRunnerTestContainer{" + m_frameworkName + "}";
    }
}
