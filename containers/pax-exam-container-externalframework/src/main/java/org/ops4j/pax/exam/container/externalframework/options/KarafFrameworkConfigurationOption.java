package org.ops4j.pax.exam.container.externalframework.options;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import org.apache.maven.shared.model.fileset.FileSet;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.container.externalframework.internal.Helper;

public class KarafFrameworkConfigurationOption extends ExternalFrameworkConfigurationOption<KarafFrameworkConfigurationOption> implements CompositeOption {
	
	
	public KarafFrameworkConfigurationOption(String name) {
		super(name);
	}
	
    public KarafFrameworkConfigurationOption() {
		super();
	}
    
    public KarafFrameworkConfigurationOption home(String karafHome, String karafBase, String karafData, boolean startRemoteShell) {
    	add(systemProperty("karaf.home").value(karafHome));
		add(systemProperty("karaf.base").value(karafBase));
		add(systemProperty("karaf.data").value(karafData));
		add(systemProperty("karaf.startRemoteShell").value(Boolean.toString(startRemoteShell)));
		return this;
	}
    
    public KarafFrameworkConfigurationOption home(String karafHome, String karafBase, String karafData) {
    	home(karafHome, karafBase, karafData, true);
    	return this;
    }
	
	public KarafFrameworkConfigurationOption home(String karafHome) {
		return home(karafHome, karafHome);
	}
	
	public KarafFrameworkConfigurationOption home(String karafHome, String karafBase) {
		return home(karafHome, karafBase, new File("target/karaf-data-"+super.getName()).getAbsolutePath());
	}
	
	public KarafFrameworkConfigurationOption home(File karafHome) {
		return home(karafHome.getAbsolutePath());
	}
	
    public KarafFrameworkConfigurationOption home() {
		return home(getDefaultKarafHome());
	}

    private static String getDefaultKarafHome() {
        String ret = null;
        ret = System.getProperty("karaf.home");
        if (ret != null)
            return ret;
        File target = new File("target");
        if (target.exists()) {
            target = new File(target, "karaf-"+System.currentTimeMillis());
            target.mkdir();
            return target.getAbsolutePath();
        }

        target = new File("karaf");
        target.mkdir();
        return target.getAbsolutePath();
    }
	
	public KarafFrameworkConfigurationOption defaultConf() {
		add(systemProperty("java.endorsed.dirs").value("${java.home}/jre/lib/endorsed"+File.pathSeparator+"${java.home}/lib/endorsed"+File.pathSeparator+"${karaf.home}/lib/endorsed"));
		add(systemProperty("java.ext.dirs").value("${java.home}/jre/lib/ext"+File.pathSeparator+"${java.home}/lib/ext"+File.pathSeparator+"${karaf.home}/lib/ext"));
		add(systemProperty("karaf.instances").value("${karaf.home}/instances"));
		//add(systemProperty("java.util.logging.config.file").value("${karaf.base}/etc/java.util.logging.properties"));
		add(systemProperty("karaf.maven.convert").value("false"));
		add(systemProperty("karaf.lock").value("false"));
        return this;
	}
	
	protected boolean mustExtract(File f, File workDir, File osgiFrameworkHomeDir) {
        return f != null && (osgiFrameworkHomeDir.listFiles() == null || osgiFrameworkHomeDir.listFiles().length < 3);
    }
    protected void configure(File karafHomeDir) {
	}
    
    @Override
    protected String getMainClass() {
    	return "org.apache.karaf.main.Main";
    }
    
    protected File getOsgiFrameworkHomeDir(Properties config) {
		String karafHome = config.getProperty("karaf.home");
		File osgiFrameworkHomeDir = new File(karafHome);
		return osgiFrameworkHomeDir;
	}

	protected File getData(Properties config) {
    	String karafData = config.getProperty("karaf.data");
    	karafData = Helper.substVars(karafData, "karaf.data", new HashMap<String, String>(), config);
		if (karafData != null && karafData.trim().length() != 0)
			return new File(karafData);
    	File karafBase = getBase(config);
		return new File(karafBase, "data");
	}

	protected File getBase(Properties config) {
		String karafBase = config.getProperty("karaf.base");
		if (karafBase != null && karafBase.trim().length() != 0)
			return new File(karafBase);
    	return getOsgiFrameworkHomeDir(config);
	}

	protected void configureClasspath(FileSet fileset) {
		fileset.addInclude( "lib/karaf*.jar");
	}
	
	public String getAutoInstallProperty() {
		return "karaf.auto.install.";
	}
	public String getAutoStartProperty() {
		return "karaf.auto.start.";
	}
}
