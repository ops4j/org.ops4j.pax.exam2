package org.ops4j.pax.exam.spi;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.OptionUtils.expand;
import static org.ops4j.pax.exam.OptionUtils.filter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.exam.spi.probesupport.intern.TestProbeBuilderImpl;
import org.ops4j.store.Store;
import org.ops4j.store.intern.TemporaryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class DefaultExamSystem implements ExamSystem
{

    private static final Logger LOG = LoggerFactory.getLogger( DefaultExamSystem.class );

    final private Store<InputStream> m_store;
    final private File m_configDirectory;
    final private Option[] m_combinedOptions;

    final private Stack<File> m_tempStack;
    final private Stack<ExamSystem> m_subsystems;

    final private RelativeTimeout m_timeout;

    private Set<Class> m_requestedOptionTypes = new HashSet<Class>();

    /**
     * Creates a fresh ExamSystem. Your options will be combined with internal defaults.
     * If you need to change the system after it has been created, you fork() it.
     * Forking will not add default options again.
     * 
     * @param options
     * @return
     * @throws IOException
     */
    public static ExamSystem create( Option[] options ) throws IOException
    {
        return create( options, true );
    }

    /**
     * Creates a fresh ExamSystem. Your options will be combined with internal defaults.
     * If you need to change the system after it has been created, you fork() it.
     * Forking will not add default options again.
     * 
     * @param options
     * @return
     * @throws IOException
     */
    public static ExamSystem create( Option[] options, boolean includeDefaults ) throws IOException
    {
        LOG.info( "Pax Exam System (Version: " + Info.getPaxExamVersion() + ") created." );
        if ( includeDefaults )
        {
            return new DefaultExamSystem( combine( options, defaultOptions() ) );
        } else
        {
            return new DefaultExamSystem( options );
        }
    }

    /**
     * Create a new system based on *this*.
     * The forked System remembers the forked instances in order to clear resources up (if desired).
     */
    public ExamSystem fork( Option[] options ) throws IOException
    {
        ExamSystem sys = new DefaultExamSystem( combine( m_combinedOptions, options ) );
        m_subsystems.add( sys );
        return sys;
    }

    private DefaultExamSystem( Option[] options ) throws IOException
    {
        m_combinedOptions = expand( options );
        m_tempStack = new Stack<File>();
        m_subsystems = new Stack<ExamSystem>();

        m_configDirectory = new File( System.getProperty( "user.home" ) + "/.pax/exam/" );
        m_configDirectory.mkdirs();
        m_store = new TemporaryStore( getTempFolder(), false );
        m_timeout = RelativeTimeout.TIMEOUT_DEFAULT;
    }

    private static Option[] defaultOptions()
    {
        return new Option[] {
                bootDelegationPackage( "sun.*" ),
                url( "link:classpath:META-INF/links/org.ops4j.pax.exam.rbc.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.ops4j.pax.extender.service.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.osgi.compendium.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.ops4j.pax.logging.api.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ) };
    }

    public <T extends Option> T[] getOptions( final Class<T> optionType )
    {
        m_requestedOptionTypes.add( optionType );
        return filter( optionType, m_combinedOptions );
    }

    /**
     * 
     * @return the basic directory that Exam should use to look at
     *         user-defaults.
     */
    public File getConfigFolder()
    {
        return m_configDirectory;
    }

    /**
     * 
     * @return the basic directory that Exam should use for all IO write
     *         activities.
     */
    public File getTempFolder()
    {
        File tmp = Files.createTempDir();
        m_tempStack.add( tmp );
        return tmp;
    }

    /**
     * 
     * @return a relative indication of how to deal with timeouts.
     */
    public RelativeTimeout getTimeout()
    {
        return m_timeout;
    }

    /**
     * 
     * Clears up resources taken by system (like temporary files).
     * 
     */
    public void clear()
    {
        try
        {
            warnUnusedOptions();

            for ( ExamSystem sys : m_subsystems )
            {
                sys.clear();
            }
            while ( !m_tempStack.isEmpty() )
            {
                Files.deleteRecursively( m_tempStack.pop().getCanonicalFile() );
            }
        } catch ( IOException e )
        {
            // LOG.info("Clearing " + this.toString() + " failed." ,e);
        }
    }

    private void warnUnusedOptions()
    {
        if ( m_subsystems.isEmpty() )
        {
            for ( String skipped : findOptionTypes() )
            {
                LOG.warn( "Option " + skipped + " has not been recognized." );
            }
        }
    }

    private Set<String> findOptionTypes()
    {
        Set<String> missing = new HashSet<String>();
        for ( Option option : m_combinedOptions )
        {
            boolean found = false;
            for ( Class<?> c : m_requestedOptionTypes )
            {
                try
                {
                    option.getClass().asSubclass( c );
                    found = true;
                    break;
                } catch ( Exception e )
                {

                }
            }
            if ( !found )
            {
                try {
                    option.getClass().asSubclass(  FrameworkOption.class );
                    // false friend
                    continue;
                }catch(Exception e) {
                    
                }
                missing.add( option.getClass().getCanonicalName() );
                
            }
        }
        return missing;
    }

    public TestProbeBuilder createProbe( Properties p ) throws IOException
    {
        return new TestProbeBuilderImpl( p, m_store );
    }

    public String createID( String purposeText )
    {
        return UUID.randomUUID().toString();
    }

    public String toString()
    {
        return "ExamSystem:options=" + m_combinedOptions.length + ";queried=" + m_requestedOptionTypes.size();
    }
}
