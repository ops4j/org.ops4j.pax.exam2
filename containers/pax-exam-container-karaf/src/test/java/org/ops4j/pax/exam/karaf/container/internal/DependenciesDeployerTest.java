package org.ops4j.pax.exam.karaf.container.internal;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.Scanner;

import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption.OverwriteMode;

public class DependenciesDeployerTest {

    /**
     * See https://ops4j1.jira.com/browse/PAXEXAM-572
     */
    @Test
    public void testEncoding() {
        WrappedUrlProvisionOption option = wrappedBundle(mavenBundle("mygroup", "myArtifactId", "1.0"));
        option.instructions("Export-Package=my.package.*");
        option.overwriteManifest(OverwriteMode.MERGE);
        StringWriter wr = new StringWriter();
        DependenciesDeployer.writeDependenciesFeature(wr, new ProvisionOption<?>[] {option}, new KarafFeaturesOption[] {});
        Assert.assertThat(wr.toString(), StringContains.containsString("<bundle>wrap:mvn:mygroup/myArtifactId/1.0$overwrite=MERGE&amp;Export-Package=my.package.*</bundle>"));
    }
    
    @Test
    public void testDependencyFeature() {
        MavenArtifactProvisionOption option = mavenBundle().groupId("mygroup").artifactId("myArtifactId").version("1.0");
        StringWriter wr = new StringWriter();
        DependenciesDeployer.writeDependenciesFeature(wr, new ProvisionOption<?>[] { option },
            new KarafFeaturesOption[] {});
        Assert.assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.0.0\" name=\"test-dependencies\">\n" + 
            "<feature name=\"test-dependencies\">\n" + 
            "<bundle>mvn:mygroup/myArtifactId/1.0</bundle>\n" + 
            "</feature>\n" + 
            "</features>\n", wr.toString());
    }

    @Test
    public void testDependencyFeatureWithBundleStartLevel() {
        MavenArtifactProvisionOption option = mavenBundle().groupId("mygroup").artifactId("myArtifactId").version("1.0").startLevel(42);
        StringWriter wr = new StringWriter();
        DependenciesDeployer.writeDependenciesFeature(wr, new ProvisionOption<?>[] { option },
            new KarafFeaturesOption[] {});
        Assert.assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.0.0\" name=\"test-dependencies\">\n" + 
            "<feature name=\"test-dependencies\">\n" + 
            "<bundle start-level=\"42\">mvn:mygroup/myArtifactId/1.0</bundle>\n" + 
            "</feature>\n" + 
            "</features>\n", wr.toString());
    }

    @Test
    public void testDependencyFeatureWithFeatureDependencies() throws FileNotFoundException {

        // Given a KarafFeatureOption exists
        KarafFeaturesOption option = KarafDistributionOption
            .features("mvn:mygroup/myArtifactId/1.0/xml/features", "myFeature");

        ExamSystem subsystem = Mockito.mock(ExamSystem.class);
        Mockito.when(subsystem.getOptions(Mockito.eq(ProvisionOption.class)))
            .thenReturn(new ProvisionOption[0]);
        Mockito.when(subsystem.getOptions(Mockito.eq(KarafFeaturesOption.class)))
            .thenReturn(new KarafFeaturesOption[] { option });
        File karafBase = new File(System.getProperty("java.io.tmpdir"));
        File karafHome = karafBase;
        DependenciesDeployer dependenciesDeployer = new DependenciesDeployer(subsystem, karafBase,
            karafHome);

        // When test-dependencies.xml is generated 
        KarafFeaturesOption dependenciesFeature = dependenciesDeployer.getDependenciesFeature();

        // Then include any feature dependencies configured
        File outputFeaturesFile = new File(karafBase, "test-dependencies.xml");
        Assert.assertEquals("file:" + outputFeaturesFile, dependenciesFeature.getURL());
        Assert.assertArrayEquals(new String[] { "test-dependencies" },
            dependenciesFeature.getFeatures());

        // Read XML output into a String
        String xmlOutputString;
        try (Scanner scanner = new Scanner(outputFeaturesFile)) {
            xmlOutputString = scanner.useDelimiter("\\Z").next() + "\n";
        }

        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.0.0\" name=\"test-dependencies\">\n"
            + "<feature name=\"test-dependencies\">\n"
            + "<feature>myFeature</feature>\n"
            + "</feature>\n"
            + "</features>\n",
            xmlOutputString);
    }
}
