package org.ops4j.pax.exam.spi;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.*;
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

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.exam.options.TimeoutOption;
import org.ops4j.pax.exam.options.extra.CleanCachesOption;
import org.ops4j.pax.exam.options.extra.WorkingDirectoryOption;
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

    final private Stack<ExamSystem> m_subsystems;

    final private RelativeTimeout m_timeout;

    private Set<Class> m_requestedOptionTypes = new HashSet<Class>();

    private CleanCachesOption m_clean;

    private File m_cache;

    

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
        LOG.info( "Pax Exam System (Version: " + Info.getPaxExamVersion() + ") created." );
        return new DefaultExamSystem( options );
       
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
        m_subsystems = new Stack<ExamSystem>();
        m_combinedOptions = expand( options );
        m_configDirectory = new File( System.getProperty( "user.home" ) + "/.pax/exam/" );
        m_configDirectory.mkdirs();
        
        WorkingDirectoryOption work = getSingleOption(WorkingDirectoryOption.class);
        if (work != null) {
            m_cache = createTemp( new File ( work.getWorkingDirectory() ) );
        }else  {
            m_cache = createTemp(null);
        }
        
        m_store = new TemporaryStore( m_cache, false );

        TimeoutOption timeoutOption = getSingleOption( TimeoutOption.class );
        if (timeoutOption != null) {
            m_timeout = new RelativeTimeout( timeoutOption.getTimeout() );
        }else {
            m_timeout = RelativeTimeout.TIMEOUT_DEFAULT;
        }
        m_clean = getSingleOption(CleanCachesOption.class);
        
    }

    /**
     * Creates a fresh temp folder under the mentioned working folder.
     * If workingFolder is null, system wide temp location will be used.
     * 
     * @param workingDirectory
     * @return
     * @throws IOException 
     */
    private synchronized File createTemp( File workingDirectory ) throws IOException
    {
        if (workingDirectory == null) {
            return Files.createTempDir();
        }else {
            workingDirectory.mkdirs();
            return workingDirectory;
        }
    }

    /**
     * Helper method for single options. First occurence has precedence.
     * 
     * @param <T>
     * @param optionType
     * @return The first option found of type T or null if none was found.
     */
    public <T extends Option> T getSingleOption( final Class<T> optionType )
    {
        m_requestedOptionTypes.add( optionType );
        T[] filter = filter( optionType, m_combinedOptions );
        
        if (filter.length > 0) {
            return filter[0];
        }else {
            return null;
        }
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
        return m_cache;
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

            if (m_clean == null || m_clean.getValue() == Boolean.TRUE ) {
                for ( ExamSystem sys : m_subsystems )
                {
                    sys.clear();
                }
                Files.deleteRecursively( m_cache.getCanonicalFile() );
                
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
