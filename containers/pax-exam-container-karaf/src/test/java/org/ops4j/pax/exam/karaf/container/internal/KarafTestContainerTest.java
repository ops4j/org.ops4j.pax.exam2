package org.ops4j.pax.exam.karaf.container.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import org.junit.Test;
import org.mockito.Mockito;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption.OverwriteMode;


public class KarafTestContainerTest {

    /**
     * See https://ops4j1.jira.com/browse/PAXEXAM-572
     */
    @Test
    public void testEncoding() {
        WrappedUrlProvisionOption option = wrappedBundle(mavenBundle("mygroup", "myArtifactId", "1.0"));
        option.instructions("Export-Package=my.package.*");
        option.overwriteManifest(OverwriteMode.MERGE);
        
        KarafTestContainer container = new KarafTestContainer(null, null, null, null);
        ExamSystem system = mock(ExamSystem.class);
        Mockito.when(system.getOptions(ProvisionOption.class)).thenReturn(new ProvisionOption<?>[]{option});
        String extensions = container.extractExtensionString(system);
        assertEquals("<bundle>wrap:mvn:mygroup/myArtifactId/1.0$overwrite=MERGE&amp;Export-Package=my.package.*</bundle>", extensions);
    }
}
