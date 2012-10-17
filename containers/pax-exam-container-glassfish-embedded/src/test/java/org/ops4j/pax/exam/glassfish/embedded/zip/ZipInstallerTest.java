package org.ops4j.pax.exam.glassfish.embedded.zip;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.zip.ZipInstaller;

import com.google.common.io.Files;

public class ZipInstallerTest
{

    private File installDir;

    @Before
    public void setUp()
    {
        installDir = Files.createTempDir();
    }

    @After
    public void tearDown() throws IOException
    {
        FileUtils.delete( installDir );
    }

    @Test
    public void downloadAndInstall() throws IOException
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );

        URL url = new URL( "mvn:org.ops4j.base/ops4j-base-io/1.2.3/jar/sources" );
        ZipInstaller installer = new ZipInstaller( url, installDir.getAbsolutePath() );
        installer.downloadAndInstall();

        assertThat( new File( installDir, "META-INF/MANIFEST.MF" ).exists(), is( true ) );
        assertThat( new File( installDir, "org/ops4j/io/ZipLister.java" ).exists(), is( true ) );
    }
}
