package org.ops4j.pax.exam.spi.war;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;

import com.google.common.io.Files;

public class WarTestProbeBuilderTest {

    @Test
    public void createWarTestProbe() throws IOException {
        TestProbeBuilder builder = new WarTestProbeBuilderImpl(Files.createTempDir());
        TestProbeProvider provider = builder.build();
        InputStream is = provider.getStream();
        StreamUtils.copyStream(is, new FileOutputStream("target/out.war"), true);
    }
}
