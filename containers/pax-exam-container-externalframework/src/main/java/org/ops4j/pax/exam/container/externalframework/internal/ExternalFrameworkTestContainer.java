/*
 * Copyright 2011 Stephane Chomat.
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
package org.ops4j.pax.exam.container.externalframework.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.container.def.AbstractTestContainer;
import org.ops4j.pax.exam.container.externalframework.internal.javarunner.DefaultJavaRunner;
import org.ops4j.pax.exam.container.externalframework.internal.javarunner.JavaRunner;
import org.ops4j.pax.exam.container.externalframework.internal.javarunner.StoppableJavaRunner;
import org.ops4j.pax.exam.container.externalframework.options.ExternalFrameworkConfigurationOption;
import org.ops4j.pax.exam.container.externalframework.options.OptionParser;
import org.osgi.framework.BundleException;

/**
 * {@link TestContainer} implementation using url connector with pax runner, start a karaf configuration or other.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 09, 2008
 */
class ExternalFrameworkTestContainer extends AbstractTestContainer
    implements TestContainer
{

    private static final Log LOG = LogFactory.getLog( ExternalFrameworkTestContainer.class );

    /**
     * Java runner to be used to start up Pax Runner.
     */
    private JavaRunner m_javaRunner;

    /**
     * Pax Runner arguments, out of options.
     */
    private  OptionParser m_arguments;

    private ExternalFrameworkConfigurationOption<?> m_config;

    /**
     * Constructor.
     * @param rmiRegistry 
     * 
     * @param javaRunner java runner to be used to start up Pax Runner
     * @param options user startup options
     */
    ExternalFrameworkTestContainer(String host,
    		int port, 
    		ExternalFrameworkConfigurationOption<?> config, final Option... options )
    {
       
    	super(host, port, options );
    	m_config = config;
    }
    
    @Override
    protected String getInfo() {
    	return "External Osgi framework "+m_config.getName();
    }
    
    @Override
    protected void parseOption(String m_host2, int m_port2, Option[] args) {
    	 m_arguments = m_config.parseOption(m_host, m_port, args);
    }
    
    @Override
    protected long getRMITimeout() {
    	return m_arguments.getRMITimeout();
    }
    
    @Override
    protected long getSystemBundleId() {
    	return m_config.getSystemBundleId();
    }
    
    @Override
    protected long getStartTimeout() {
    	return m_arguments.getStartTimeout();
    }

	protected void startProcess() throws BundleException,
			MalformedURLException {
	    //start url handlers like mvn:, dir: ...
		startURLHandler();
		m_arguments.addBundleOption(60, m_options);
		//start osgi framework
		startOsgiFramework();
	}
    
    private void startURLHandler() throws BundleException {
    	System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
		try {
		   // new URL("aether:foo/bar");
			new URL("mvn:foo/bar");
		}catch(Exception e) {
		    throw new RuntimeException( e );
		}
	}

    private void startOsgiFramework() {
    	m_javaRunner = getJavaRunner();
        String[] arguments = m_arguments.getArguments();
        printExtraBeforeStart(arguments);
		try {
            long startedAt = System.currentTimeMillis();
            m_config.run(
            		m_arguments.getJavaHome(), 
            		m_arguments.getWorkingFolder(), 
            		m_javaRunner, 
            		m_arguments.getVmOptions(), 
            		arguments, 
            		m_arguments.getConfig());
            LOG.info( "Test container ( "+m_config.getName() +") started in "
                     + ( System.currentTimeMillis() - startedAt ) + " millis" );
        } catch (NoSuchArchiverException ex) {
            Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Cannot run "+ExternalFrameworkTestContainer.toString(m_arguments.getJavaHome(), m_arguments.getWorkingFolder(), m_arguments.getVmOptions(), arguments, m_arguments.getConfig()), ex);
        } catch (ArchiverException ex) {
            Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Cannot run "+ExternalFrameworkTestContainer.toString(m_arguments.getJavaHome(), m_arguments.getWorkingFolder(), m_arguments.getVmOptions(), arguments, m_arguments.getConfig()), ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Cannot run "+ExternalFrameworkTestContainer.toString(m_arguments.getJavaHome(), m_arguments.getWorkingFolder(), m_arguments.getVmOptions(), arguments, m_arguments.getConfig()), ex);
        } catch (Exception ex) {
            Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Cannot run "+ExternalFrameworkTestContainer.toString(m_arguments.getJavaHome(), m_arguments.getWorkingFolder(), m_arguments.getVmOptions(), arguments, m_arguments.getConfig()), ex);
        } 
	}

	protected JavaRunner getJavaRunner() {
		String javaRunnerClass =  m_arguments.getConfig().getProperty("org.apache.karaf.testing.javarunner", "org.ops4j.pax.runner.platform.DefaultJavaRunner");
        JavaRunner javaRunner;
		if ("DefaultJavaRunner".equals(javaRunnerClass) || "org.ops4j.pax.runner.platform.DefaultJavaRunner".equals(javaRunnerClass)) {
            javaRunner = new DefaultJavaRunner( false );
        } else {
            try {
                javaRunner = (JavaRunner) Class.forName(javaRunnerClass).newInstance();
            } catch (InstantiationException ex) {
                Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException("Cannot instanciate "+javaRunnerClass, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException("Cannot instanciate "+javaRunnerClass, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException("Cannot instanciate "+javaRunnerClass, ex);
            }
        }
        return javaRunner;
	}

	@Override
	protected void stopProcess() {
		if( m_javaRunner != null && m_javaRunner instanceof StoppableJavaRunner) {
        	((StoppableJavaRunner) m_javaRunner).shutdown();
        }
	}

    @Override
    public String toString()
    {
        return "PaxRunnerTestContainer{}";
    }

    static private String toString(String javaHome, File workingFolder, String[] vmOptions, String[] arguments, Properties config) {
        StringBuilder sb = new StringBuilder();
        sb.append("cd ").append(toCygwin(workingFolder)).append("\n");
        for (Object str : config.keySet()) {
            sb.append(str).append("=").append(config.get(str)).append("\n");
        }
        sb.append("JAVA_EXEC=").append(toCygwin(new File(javaHome))).append("/bin/java\n");
        sb.append("RUN_JAVA_OPTS=\\\n");
        for (String vmOpt : vmOptions) {
            if (vmOpt.startsWith("-D")) {
                int index = vmOpt.indexOf("=");
                if (index != -1) {
                    index++;
                    String key = vmOpt.substring(0, index);
                    String value = vmOpt.substring(index);
                    sb.append(" ").append(key).append("\"").append(value.replaceAll("\"", "\\\"")).append("\"\\\n");
                }
            }
            sb.append("\\n").append(vmOpt).append("\\\n");
        }
        sb.setLength(sb.length()-2);
        sb.append("\n");
        sb.append("$JAVA_EXEC $RUN_JAVA_OPTS $MAIN_CLASS");
        for (String arg : arguments) {
            sb.append(" ").append(arg);
        }
        sb.append("\n");
        
        return sb.toString();
    }

    static private String toCygwin(File pathFile) {
        String path = pathFile.getPath();
        path = path.replaceAll("\\\\", "/");
        if (pathFile.isAbsolute()) {
            return "/cygdrive/"+path.substring(0,1)+"/"+path.substring(3);
        }
        return path;
    }

}
