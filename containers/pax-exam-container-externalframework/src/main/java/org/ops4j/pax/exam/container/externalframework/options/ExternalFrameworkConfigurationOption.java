package org.ops4j.pax.exam.container.externalframework.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.exam.options.UrlReference;
import org.ops4j.pax.runner.platform.JavaRunner;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.platform.internal.StreamUtils;
import org.ops4j.pax.exam.container.externalframework.internal.DefaultOptionsParser;
import org.ops4j.pax.exam.container.externalframework.internal.Download;

public abstract class ExternalFrameworkConfigurationOption<T extends ExternalFrameworkConfigurationOption<?>> extends FrameworkOption implements CompositeOption {
	private static final String DEFAULT = "default";

	protected UrlReference configuration = null;
    
	protected String pathInArchive;
	protected int systemBundleId = 0;
    /**
     * Composite options (cannot be null).
     */
    private final List<Option> m_options = new ArrayList<Option>();

	public ExternalFrameworkConfigurationOption(String name) {
		super(name);
	}
	
    public ExternalFrameworkConfigurationOption() {
		super(DEFAULT);
	}
    
    
    public T systemBundleId(int id) {
		systemBundleId = id;
		return (T) this;
	}
    
	public int getSystemBundleId() {
		return systemBundleId;
	}
    /**
     * Adds options.
     *
     * @param options composite options to be added (can be null or no options specified)
     *
     * @return itself, for fluent api usage
     */
    public T add( final Option... options )
    {
        if( options != null )
        {
            m_options.addAll( Arrays.asList( options ) );
        }
        return (T) this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "DefaultCompositeOption" );
        sb.append( "{options=" ).append( m_options );
        sb.append( '}' );
        return sb.toString();
    }
    
	public T addConfiguration(UrlReference ref, String path) {
        this.pathInArchive = path;
		this.configuration = ref;
		return (T) this;
	}
	
	public abstract T defaultConf();
	
	protected abstract File getOsgiFrameworkHomeDir(Properties config);
	
	protected abstract String getMainClass();

	
	public OptionParser parseOption(Option[] options) {
		return new DefaultOptionsParser(this, getAutoInstallProperty(), getAutoStartProperty(), options);
	}
	
    protected void configure(File osgiHome) {
	}

	protected File getData(Properties config) {
		return new File(getOsgiFrameworkHomeDir(config),"data");
	}
	
	protected File getBase(Properties config) {
		return getOsgiFrameworkHomeDir(config);
	}
	
	protected String[] getClassPath(File osgiFrameworkHome) {
        FileSetManager fileSetManager = new FileSetManager();
        FileSet fileset = new FileSet();
        fileset.setDirectory( osgiFrameworkHome.getAbsolutePath() );
        configureClasspath(fileset);
        String[] classpathDir = fileSetManager.getIncludedFiles(fileset);
        for (int i = 0; i < classpathDir.length; i++) {
            classpathDir[i] = new File(osgiFrameworkHome, classpathDir[i]).getAbsolutePath();
        }
        return classpathDir;
    }

	protected abstract void configureClasspath(FileSet fileset);
	
	public void run(String javaHome,
			File workDir, 
			JavaRunner runner,
			String[] vmOptions,
			String[] programOptions,
			Properties config) throws PlatformException, NoSuchArchiverException, ArchiverException, IOException {
		
		File osgiFrameworkHomeDir = getOsgiFrameworkHomeDir(config);
        File f = getFile(configuration, workDir);
        if (f != null)
			extractTo(f, workDir, osgiFrameworkHomeDir);
		else
			deleteDir(getData(config));
        configure(osgiFrameworkHomeDir);
        runner.exec(vmOptions, getClassPath(osgiFrameworkHomeDir),
                getMainClass(),
                programOptions, javaHome,  osgiFrameworkHomeDir);
	}

	@Deprecated
	protected File getHome(Properties config) {
		return getOsgiFrameworkHomeDir(config);
	}
	
	protected void extractTo(File f, File tempDir, File osgiFrameworkHome) throws PlatformException  {
        PlexusContainer container = null;
        
        try {
            container = new DefaultPlexusContainer();
            container.initialize();
            container.start();
            ArchiverManager am;
            am = (ArchiverManager) container.lookup(ArchiverManager.ROLE);
            UnArchiver ua = am.getUnArchiver(f);
            ua.setSourceFile(f);
            ua.setDestDirectory(tempDir);
            ua.setOverwrite(true);
            File tempPathFile = new File(tempDir, pathInArchive);
            deleteDir(tempPathFile);
            if (tempPathFile.exists())
                throw new PlatformException("Cannot delete "+tempPathFile.getAbsolutePath());
            deleteDir(osgiFrameworkHome);
            if (osgiFrameworkHome.exists())
                throw new PlatformException("Cannot delete "+osgiFrameworkHome.getAbsolutePath());
            ua.extract();
            osgiFrameworkHome.getParentFile().mkdirs();
            if (!tempPathFile.exists())
                throw new PlatformException("Cannot extract into "+tempPathFile.getAbsolutePath());
            if (osgiFrameworkHome.exists())
                throw new PlatformException("Cannot delete "+osgiFrameworkHome.getAbsolutePath());
            tempPathFile.renameTo(osgiFrameworkHome);
            if (!osgiFrameworkHome.exists())
                throw new PlatformException("Cannot rename dir to "+osgiFrameworkHome.getAbsolutePath());
        } catch (PlatformException ex) {
            Logger.getLogger(ExternalFrameworkConfigurationOption.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (Exception ex) {
            Logger.getLogger(ExternalFrameworkConfigurationOption.class.getName()).log(Level.SEVERE, null, ex);
            throw new PlatformException("Impossible to extract "+f+" to "+osgiFrameworkHome, ex);
        } finally {
            if (container != null)
                container.dispose();
        }
    }

	protected File getFile(UrlReference conf, File workDir) {
		if (conf == null) 
			return null;
		try {
			return Download.download(workDir, new URL(conf.getURL()), true, false, false,false);
		} catch (Throwable e) {
			throw new RuntimeException("Cannot find "+conf);
		}
	}

	public Option[] getOptions() {
		final List<Option> expanded = new ArrayList<Option>();
		expanded.add(this);
		expanded.addAll(Arrays.asList(OptionUtils.expand( m_options.toArray( new Option[m_options.size()] ) )));
        return (Option[]) expanded.toArray(new Option[expanded.size()]);
	}

    public static void deleteDir(File dir) throws IOException {
    	FileSetManager fileSetManager = new FileSetManager();
        FileSet fileset = new FileSet();
        fileset.setDirectory( dir.getAbsolutePath() );
        fileset.setUseDefaultExcludes( false );
        fileset.addInclude("**");
        fileSetManager.delete(fileset);
    }
    
    /**
     * 
     * @param osgiHomeDir
     * @param pathInOsgiFramework
     * @param pathInClassPath
     * @param classLoader can be null.
     * @throws IOException if error.
     */
    public void replaceFile(File osgiHomeDir, String pathInOsgiFramework, String pathInClassPath, ClassLoader classLoader) throws IOException {
    	File feature = new File(osgiHomeDir, pathInOsgiFramework);
		if (classLoader == null)
			classLoader = getClass().getClassLoader();
		replaceFile(feature, classLoader.getResourceAsStream(pathInClassPath));
	}
    
    public void replaceFile(File oldFile, File newFile) throws IOException {
    	replaceFile(oldFile, new FileInputStream(newFile));
    }
    
    public void replaceFile(File oldFile, InputStream in) throws IOException {
    	if (oldFile.isDirectory()) {
    		deleteDir(oldFile);
    	} else {
    		oldFile.delete();
    	}
    	if (oldFile.exists())
            throw new IOException("Cannot delete "+oldFile.getAbsolutePath());
        FileOutputStream out = new FileOutputStream(oldFile);
    	StreamUtils.streamCopy( in, out.getChannel(), null );
    }
    
    public abstract String getAutoInstallProperty();
    public abstract String getAutoStartProperty();
}
