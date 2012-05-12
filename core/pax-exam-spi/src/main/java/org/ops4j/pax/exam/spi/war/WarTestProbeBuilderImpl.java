/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.spi.war;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.embeddable.archive.ScatteredArchive.Type;
import org.ops4j.io.ZipExploder;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.intern.DefaultTestAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class WarTestProbeBuilderImpl implements TestProbeBuilder
{
    private static final Logger LOG = LoggerFactory.getLogger( WarTestProbeBuilderImpl.class );

    private static String[] classPathExcludes = {
        ".cp",
        "scattered-archive-api",
        "simple-glassfish-api",
        "jersey-client",
        "pax-exam-container",
        "tinybundles",
        "geronimo-atinject",
        "bndlib"
    };

    private ClasspathFilter classpathFilter;
    private List<File> metadataFiles;
    private File tempDir;
    private File warBase;
    private final Map<TestAddress, TestInstantiationInstruction> probeCalls = new HashMap<TestAddress, TestInstantiationInstruction>();

    public WarTestProbeBuilderImpl()
    {
        this.classpathFilter = new ClasspathFilter( classPathExcludes );
        this.metadataFiles = new ArrayList<File>();
        this.tempDir = Files.createTempDir();
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    public TestAddress addTest( Class clazz, String methodName, Object... args )
    {
        TestAddress address = new DefaultTestAddress( clazz.getSimpleName() + "." + methodName, args );
        probeCalls.put( address, new TestInstantiationInstruction( clazz.getName() + ";" + methodName ) );
        return address;
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    public TestAddress addTest( Class clazz, Object... args )
    {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    public List<TestAddress> addTests( Class clazz, Method... m )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestProbeBuilder setHeader( String key, String value )
    {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    public TestProbeBuilder ignorePackageOf( Class... classes )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestProbeProvider build()
    {
        try
        {
            createDefaultMetadata();
            String warName = "pax-exam-" + UUID.randomUUID().toString();
            ScatteredArchive sar;
            File webResourceDir = getWebResourceDir();
            if( webResourceDir.exists() && webResourceDir.isDirectory() )
            {
                sar = new ScatteredArchive( warName, Type.WAR, webResourceDir );
            }
            else
            {
                sar = new ScatteredArchive( warName, Type.WAR );
            }
            String classpath = System.getProperty( "java.class.path" );
            String[] pathElems = classpath.split( File.pathSeparator );

            for( String pathElem : pathElems )
            {
                File file = new File( pathElem );
                if( file.exists() && classpathFilter.accept( file ) )
                {
                    LOG.info( "including classpath element {}", file );
                    sar.addClassPath( file );
                }
            }
            for( File metadata : metadataFiles )
            {
                if( metadata.exists() )
                {
                    sar.addMetadata( metadata );
                }
            }
            URI warUri = sar.toURI();
            LOG.info( "probe WAR at {}", warUri );
            return new WarTestProbeProvider( warUri, getTests() );
        }
        catch ( IOException exc )
        {
            throw new TestContainerException( exc );
        }
    }
    
    private TestAddress[] getTests()
    {
        return probeCalls.keySet().toArray( new TestAddress[ probeCalls.size() ] );
    }
    

    private File getWebResourceDir() throws IOException
    {
        File webResourceDir;
        // FIXME create configuration property
        // String warBase = config.getWarBase();
        if( warBase == null )
        {
            webResourceDir = new File( "src/main/webapp" );
        }
        else
        {
            ZipExploder exploder = new ZipExploder();
            webResourceDir = new File( tempDir, "exploded" );
            webResourceDir.mkdir();
            exploder.processFile( warBase.getAbsolutePath(),
                webResourceDir.getAbsolutePath() );
        }
        return webResourceDir;
    }

    private void createDefaultMetadata()
    {

        File webInf = new File( "src/main/webapp/WEB-INF" );
        metadataFiles.add( new File( webInf, "web.xml" ) );
        File beansXml = new File( webInf, "beans.xml" );
        if( !beansXml.exists() )
        {
            beansXml = new File( tempDir, "beans.xml" );
            try
            {
                beansXml.createNewFile();
            }
            catch ( IOException exc )
            {
                throw new TestContainerException( "cannot create " + beansXml );
            }
        }
        metadataFiles.add( beansXml );
    }
}
