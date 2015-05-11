#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;

import javax.inject.Inject;

import org.apache.karaf.features.BootFinished;
import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class MyTest {

	@Inject
	private BundleContext bc;
	

	@Inject
	protected FeaturesService featuresService;
	
	/**
	 * To make sure the tests run only when the boot features are fully
	 * installed
	 */
	@Inject
	BootFinished bootFinished;

	@Configuration
	public static Option[] configuration() throws Exception {
		return new Option[] {
				karafDistributionConfiguration().frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf")
						.type("zip").version("3.0.3"))
						.unpackDirectory(new File("target/paxexam/unpack/"))
						.useDeployFolder(false),
				configureConsole().ignoreLocalConsole(),
				logLevel(LogLevel.INFO),
				keepRuntimeFolder(),
				features(
						maven().groupId("org.apache.karaf.features")
								.artifactId("standard").type("xml")
								.classifier("features").version("3.0.3"),
						"eventadmin")
				};
	}

	@Test
	public void shouldHaveBundleContext() {
		assertThat(bc, is(notNullValue()));
	}
	
	@Test
	public void checkEventFeature() throws Exception {
		assertThat(featuresService.isInstalled(featuresService.getFeature("eventadmin")), is(true));
	}

}
