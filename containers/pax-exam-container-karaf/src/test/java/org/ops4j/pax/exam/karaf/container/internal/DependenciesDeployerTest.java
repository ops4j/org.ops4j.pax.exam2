package org.ops4j.pax.exam.karaf.container.internal;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import java.io.StringWriter;

import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
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
        DependenciesDeployer.writeDependenciesFeature(wr, option);
        Assert.assertThat(wr.toString(), StringContains.containsString("<bundle>wrap:mvn:mygroup/myArtifactId/1.0$overwrite=MERGE&amp;Export-Package=my.package.*</bundle>"));
    }
    
    @Test
    public void testDependencyFeature() {
        MavenArtifactProvisionOption option = mavenBundle().groupId("mygroup").artifactId("myArtifactId").version("1.0");
        StringWriter wr = new StringWriter();
        DependenciesDeployer.writeDependenciesFeature(wr, option);
        Assert.assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<features xmlns=\"http://karaf.apache.org/xmlns/features/v1.0.0\" name=\"test-dependencies\">\n" + 
            "<feature name=\"test-dependencies\">\n" + 
            "<bundle>mvn:mygroup/myArtifactId/1.0</bundle>\n" + 
            "</feature>\n" + 
            "</features>\n", wr.toString());
    }
}
