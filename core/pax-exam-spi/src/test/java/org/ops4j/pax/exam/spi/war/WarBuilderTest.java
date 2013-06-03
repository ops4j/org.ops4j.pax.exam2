package org.ops4j.pax.exam.spi.war;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.warProbe;
import static org.ops4j.pax.exam.spi.Probes.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

public class WarBuilderTest {

    private ZipFile war;
    
    @BeforeClass
    public static void setUp() throws IOException {
        File pomProperties = new File("target/classes/META-INF/maven/org.ops4j.pax.exam/pax-exam-spi/pom.properties");
        if (!pomProperties.exists()) {
            pomProperties.getParentFile().mkdirs();
            PrintWriter writer = new PrintWriter(new FileOutputStream(pomProperties));
            writer.println("artifactId = pax-exam-spi");
            writer.println("version = " + Info.getPaxExamVersion());
            writer.close();
        }
    }

    @After
    public void tearDown() {
        if (war != null) {
            try {
                war.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }

    @Test
    public void buildWar() throws MalformedURLException, IOException {
        war = localCopy(warProbe().library("target/classes"));
        assertThat(war.getEntry("WEB-INF/beans.xml"), is(notNullValue()));
        assertThat(war.getEntry("WEB-INF/lib/pax-exam-spi-" + Info.getPaxExamVersion() + ".jar"),
            is(notNullValue()));
    }

    private ZipFile localCopy(WarProbeOption option) throws IOException {
        TestProbeProvider provider = builder(option).build();
        InputStream is = provider.getStream();
        File out = new File("target/out.war");
        StreamUtils.copyStream(is, new FileOutputStream(out), true);
        return new ZipFile(out);
    }

    @Test
    public void buildWarWithWebResource() throws MalformedURLException, IOException {
        war = localCopy(warProbe().library("target/classes").webInfResource(
            "src/test/assets/web.xml"));
        assertThat(war.getEntry("WEB-INF/beans.xml"), is(notNullValue()));
        assertThat(war.getEntry("WEB-INF/web.xml"), is(notNullValue()));
    }

    @Test
    public void buildWarWithOverlay() throws MalformedURLException, IOException {
        war = localCopy(warProbe().overlay("target/test-war-dump.war"));
        assertThat(war.getEntry("WEB-INF/beans.xml"), is(notNullValue()));
        assertThat(war.getEntry("WEB-INF/web.xml"), is(notNullValue()));
    }

    @Test
    public void buildWarWithOverlayFromMaven() throws MalformedURLException, IOException {
        System.setProperty("java.protocol.handler.pkgs", "sun.net.www.protocol|org.ops4j.pax.url");

        war = localCopy(warProbe().overlay(
            maven("com.googlecode.jeeunit", "jeeunit-example-test", "0.8.0").type("war")));
        System.clearProperty("java.protocol.handler.pkgs");

        assertThat(war.getEntry("WEB-INF/beans.xml"), is(notNullValue()));
        ZipEntry webXml = war.getEntry("WEB-INF/web.xml");
        assertThat(webXml, is(notNullValue()));
    }

    @Test
    public void buildWarAutoClassPath() throws MalformedURLException, IOException {
        war = localCopy(warProbe().classPathDefaultExcludes());
        assertThat(war.getEntry("WEB-INF/beans.xml"), is(notNullValue()));
        assertThat(war.getEntry("WEB-INF/lib/mockito-all-1.9.5.jar"), is(notNullValue()));
        assertThat(war.getEntry("WEB-INF/lib/tinybundles-1.0.0.jar"), is(nullValue()));
    }

    @Test
    public void buildWarAutoClassPathNoFilter() throws MalformedURLException, IOException {
        war = localCopy(warProbe().exclude());
        assertThat(war.getEntry("WEB-INF/beans.xml"), is(notNullValue()));
        assertThat(war.getEntry("WEB-INF/lib/tinybundles-2.0.0-SNAPSHOT.jar"), is(notNullValue()));
    }

    @Test
    public void buildWarAutoClassPathWindows() throws MalformedURLException, IOException {
        war = localCopy(warProbe().exclude());
        Enumeration<? extends ZipEntry> e = war.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            assertThat(entry.getName().contains("\\"), is(false));
        }
    }

    @Test
    public void buildWarWithClasses() throws MalformedURLException, IOException {
        war = localCopy(warProbe().classes(ExamReactor.class, PerSuite.class));
        assertThat(war.getEntry("WEB-INF/beans.xml"), is(notNullValue()));
        assertThat(war.getEntry("WEB-INF/classes/org/ops4j/pax/exam/spi/ExamReactor.class"),
            is(notNullValue()));
        assertThat(war.getEntry("WEB-INF/classes/org/ops4j/pax/exam/spi/reactors/PerSuite.class"),
            is(notNullValue()));
        assertThat(war.getEntry("WEB-INF/classes/org/ops4j/pax/exam/spi/reactors/PerClass.class"),
            is(nullValue()));
    }

    @Test
    public void buildWarAutoClassPathCustomFilter() throws MalformedURLException, IOException {
        war = localCopy(warProbe().exclude("mockito", ".cp"));
        assertThat(war.getEntry("WEB-INF/lib/mockito-all-1.9.5.jar"), is(nullValue()));
    }
    
    @Test
    public void buildEmptyWar() throws IOException {
        war = localCopy(warProbe());
        ZipEntry beansXml = war.getEntry("WEB-INF/beans.xml");
        assertThat(beansXml, is(notNullValue()));
        assertThat(beansXml.getSize(), is(0L));
    }

    @Test
    public void buildWarWithName() throws MalformedURLException, IOException {
        WarBuilder warBuilder = new WarBuilder(warProbe().library("target/classes").name("foo"));
        URI uri = warBuilder.buildWar();
        assertThat(new File(uri).getName(), is("foo.war"));
    }
}
