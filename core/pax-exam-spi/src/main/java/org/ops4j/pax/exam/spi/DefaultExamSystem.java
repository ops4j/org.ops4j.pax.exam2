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
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.probesupport.intern.TestProbeBuilderImpl;
import org.ops4j.store.Store;
import org.ops4j.store.intern.TemporaryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class DefaultExamSystem implements ExamSystem {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultExamSystem.class);

	final private Store<InputStream> m_store;
	final private File m_configDirectory;
	final private Option[] m_options;

	final private Stack<File> m_tempStack;
	final private Stack<ExamSystem> m_subsystems;

	final private RelativeTimeout m_timeout;

	public static ExamSystem create(Option[] options) throws IOException {
		LOG.info("!! Pax Exam System (Version: " + Info.getPaxExamVersion() + ") created.");
		return new DefaultExamSystem(options);
	}

	public ExamSystem fork(Option[] options) throws IOException {
		ExamSystem sys = new DefaultExamSystem(combine(m_options, options));
		m_subsystems.add(sys);
		return sys;
	}

	private DefaultExamSystem(Option[] options) throws IOException {
		m_options = expand(combine(localOptions(), options));
		m_tempStack = new Stack<File>();
		m_subsystems = new Stack<ExamSystem>();

		m_configDirectory = new File(System.getProperty("user.home") + "/.pax/exam/");
		m_configDirectory.mkdirs();
		m_store = new TemporaryStore(getTempFolder(), false);
		m_timeout = RelativeTimeout.TIMEOUT_DEFAULT;
	}

	private Option[] localOptions() {
		
		/**
		url("link:classpath:META-INF/links/org.ops4j.pax.exam.rbc.link")
				.startLevel(START_LEVEL_SYSTEM_BUNDLES),
		url(
				"link:classpath:META-INF/links/org.ops4j.pax.extender.service.link")
				.startLevel(START_LEVEL_SYSTEM_BUNDLES),
		url("link:classpath:META-INF/links/org.osgi.compendium.link")
				.startLevel(START_LEVEL_SYSTEM_BUNDLES),
		url(
				"link:classpath:META-INF/links/org.ops4j.pax.logging.api.link")
				.startLevel(START_LEVEL_SYSTEM_BUNDLES), };
				**/
		
		return new Option[] {
				bootDelegationPackage("sun.*"),
				url("mvn:org.ops4j.pax.exam/pax-exam-container-rbc/" + Info.getPaxExamVersion() ).startLevel(START_LEVEL_SYSTEM_BUNDLES),
				url("mvn:org.ops4j.pax.exam/pax-exam-extender-service/" + Info.getPaxExamVersion() ).startLevel(START_LEVEL_SYSTEM_BUNDLES),
				url("mvn:org.ops4j.pax.logging/pax-logging-api/1.6.2").startLevel(START_LEVEL_SYSTEM_BUNDLES),
				url("mvn:org.osgi/org.osgi.compendium/4.2.0" ).startLevel ( START_LEVEL_SYSTEM_BUNDLES ) };
				
	}

	public <T extends Option> T[] getOptions(final Class<T> optionType) {
		return filter(optionType, m_options);
	}

	/**
	 * 
	 * @return the basic directory that Exam should use to look at
	 *         user-defaults.
	 */
	public File getConfigFolder() {
		return m_configDirectory;
	}

	/**
	 * 
	 * @return the basic directory that Exam should use for all IO write
	 *         activities.
	 */
	public File getTempFolder() {
		File tmp = Files.createTempDir();
		m_tempStack.add(tmp);
		return tmp;
	}

	/**
	 * 
	 * @return a relative indication of how to deal with timeouts.
	 */
	public RelativeTimeout getTimeout() {
		return m_timeout;
	}

	/**
	 * 
	 * Clears up resources taken by system (like temporary files)
	 * 
	 */
	public void clear()  {
		try {
			for (ExamSystem sys : m_subsystems) {
				sys.clear();
			}
			while (!m_tempStack.isEmpty()) {
				Files.deleteRecursively(m_tempStack.pop().getCanonicalFile());
			}
		} catch(IOException e) {
			//LOG.info("Clearing " + this.toString() + " failed." ,e);
		}
	}

	public TestProbeBuilder createProbe(Properties p) throws IOException {
		return new TestProbeBuilderImpl(p, m_store);
	}

	public String createID(String purposeText) {
		return UUID.randomUUID().toString();
	}
}
