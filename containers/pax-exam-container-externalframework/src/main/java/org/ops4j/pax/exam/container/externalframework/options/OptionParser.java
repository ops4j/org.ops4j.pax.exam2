package org.ops4j.pax.exam.container.externalframework.options;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Properties;

import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.def.options.WorkingDirectoryOption;
import org.ops4j.pax.exam.options.FrameworkOption;

public interface OptionParser {

	/**
	 * @return Pax Runner arguments
	 */
	public abstract String[] getArguments();

	public abstract void addBundleOption(int sl, Option[] options)
			throws MalformedURLException;

	public abstract File getWorkingFolder();

	public abstract Customizer[] getCustomizers();

	public abstract String[] getVmOptions();

	/**
	 * @see Configuration#getJavaHome()
	 */
	public abstract String getJavaHome();

	public abstract ExternalFrameworkConfigurationOption<?> getExternalConfigurationOption();

	public abstract FrameworkOption getFrameworkOption();

	public abstract Properties getConfig();

	public abstract WorkingDirectoryOption getWorkingDirOption();

	public abstract long getRMITimeout();

	public abstract long getStartTimeout();

}