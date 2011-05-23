/*
 * Copyright 2010 Toni Menzel.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.spi.probesupport.intern;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Properties;
import java.util.jar.Manifest;
import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Jar;
import org.ops4j.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class BndWrapper
{

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger( BndWrapper.class );

    /**
     * Utility class. Ment to be used using static methods
     */
    private BndWrapper()
    {
        // utility class
    }

    /**
     * Processes the input jar and generates the necessary OSGi headers using specified instructions.
     *
     * @param jarInputStream input stream for the jar to be processed. Cannot be null.
     * @param instructions   bnd specific processing instructions. Cannot be null.
     * @param jarInfo        information about the jar to be processed. Usually the jar url. Cannot be null or empty.
     *
     * @return an input stream for the generated bundle
     *
     * @throws NullArgumentException if any of the parameters is null
     * @throws IOException           re-thron during jar processing
     */
    public static InputStream createBundle( final InputStream jarInputStream,

                                            final String jarInfo,

                                            final Properties... instructions )
        throws Exception
    {
        NullArgumentException.validateNotNull( jarInputStream, "Jar URL" );
        NullArgumentException.validateNotNull( instructions, "Instructions" );
        NullArgumentException.validateNotEmpty( jarInfo, "Jar info" );

        LOG.debug( "Creating bundle for [" + jarInfo + "]" );

        final Jar jar = new Jar( "dot", jarInputStream );
        final Manifest manifest = jar.getManifest();

        // Make the jar a bundle if it is not already a bundle

        final Analyzer analyzer = new Analyzer();
        analyzer.setJar( jar );
        for( Properties in : instructions )
        {
            LOG.trace( "+ Using instructions " + in );
            // Do not use instructions as default for properties because it looks like BND uses the props
            // via some other means then getProperty() and so the instructions will not be used at all
            // So, just copy instructions to properties
            analyzer.setProperties( in );
            analyzer.mergeManifest( manifest );
        }

        checkMandatoryProperties( analyzer, jar, jarInfo );
        analyzer.calcManifest();

        return createInputStream( jar );
    }

    /**
     * Creates an piped input stream for the wrapped jar.
     * This is done in a thread so we can return quickly.
     *
     * @param jar the wrapped jar
     *
     * @return an input stream for the wrapped jar
     *
     * @throws java.io.IOException re-thrown
     */
    private static PipedInputStream createInputStream( final Jar jar )
        throws IOException
    {
        final PipedInputStream pin = new PipedInputStream();
        final PipedOutputStream pout = new PipedOutputStream( pin );

        new Thread()
        {
            public void run()
            {
                try
                {
                    jar.write( pout );
                } catch( Exception e )
                {
                    LOG.error( "Bundle cannot be generated" );
                } finally
                {
                    try
                    {
                        jar.close();
                        pout.close();
                    } catch( IOException ignore )
                    {
                        // if we get here something is very wrong
                        LOG.error( "Bundle cannot be generated", ignore );
                    }
                }
            }
        }.start();

        return pin;
    }

    /**
     * Check if manadatory properties are present, otherwise generate default.
     *
     * @param analyzer     bnd analyzer
     * @param jar          bnd jar
     * @param symbolicName bundle symbolic name
     */
    private static void checkMandatoryProperties( final Analyzer analyzer,
                                                  final Jar jar,
                                                  final String symbolicName )
    {
        final String importPackage = analyzer.getProperty( Analyzer.IMPORT_PACKAGE );
        if( importPackage == null || importPackage.trim().length() == 0 )
        {
        	//analyzer.setProperty( Analyzer.IMPORT_PACKAGE, "*;resolution:=optional" );
            //analyzer.setProperty( Analyzer.IMPORT_PACKAGE, "org.hamcrest;resolution:=optional,org.hamcrest.core;resolution:=optional,org.junit;resolution:=optional,org.junit.runner;resolution:=optional,org.ops4j.pax.exam;resolution:=optional,org.ops4j.pax.exam.container.def;resolution:=optional,org.ops4j.pax.exam.container.def.options;resolution:=optional,org.ops4j.pax.exam.junit;resolution:=optional,org.ops4j.pax.exam.libraryoptions;resolution:=optional,org.ops4j.pax.exam.options;resolution:=optional,org.ops4j.pax.exam.spi;resolution:=optional,org.ops4j.pax.exam.spi.container;resolution:=optional,org.ops4j.pax.exam.spi.driversupport;resolution:=optional,org.ops4j.pax.exam.spi.reactors;resolution:=optional,org.osgi.framework;resolution:=optional,org.slf4j,org.slf4j.impl;resolution:=optional" );
            //org.hamcrest;resolution:=optional,org.hamcrest.core;resolution:=optional,org.junit;resolution:=optional,org.junit.runner;resolution:=optional,org.ops4j.pax.exam;resolution:=optional,org.ops4j.pax.exam.container.def;resolution:=optional,org.ops4j.pax.exam.container.def.options;resolution:=optional,org.ops4j.pax.exam.junit;resolution:=optional,org.ops4j.pax.exam.libraryoptions;resolution:=optional,org.ops4j.pax.exam.options;resolution:=optional,org.ops4j.pax.exam.spi;resolution:=optional,org.ops4j.pax.exam.spi.container;resolution:=optional,org.ops4j.pax.exam.spi.driversupport;resolution:=optional,org.ops4j.pax.exam.spi.reactors;resolution:=optional,org.osgi.framework;resolution:=optional,org.slf4j
           analyzer.setProperty( Analyzer.DYNAMICIMPORT_PACKAGE, "*" );
        }
        final String exportPackage = analyzer.getProperty( Analyzer.EXPORT_PACKAGE );
        if( exportPackage == null || exportPackage.trim().length() == 0 )
        {
            // Never export from probe.
            analyzer.setProperty( Analyzer.EXPORT_PACKAGE, "" );
            // was  analyzer.calculateExportsFromContents( jar )
        }
        final String localSymbolicName = analyzer.getProperty( Analyzer.BUNDLE_SYMBOLICNAME, symbolicName );
        analyzer.setProperty( Analyzer.BUNDLE_SYMBOLICNAME, generateSymbolicName( localSymbolicName ) );
    }

    /**
     * Processes symbolic name and replaces osgi spec invalid characters with "_".
     *
     * @param symbolicName bundle symbolic name
     *
     * @return a valid symbolic name
     */
    private static String generateSymbolicName( final String symbolicName )
    {
        return symbolicName.replaceAll( "[^a-zA-Z_0-9.-]", "_" );
    }

}
