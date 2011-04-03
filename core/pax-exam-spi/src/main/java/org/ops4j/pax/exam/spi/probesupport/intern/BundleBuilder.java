/*
 * Copyright 2008,2009 Toni Menzel.
 * Copyright 2008 Alin Dreghiciu.
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
import java.util.jar.JarOutputStream;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.exam.spi.container.DuplicateAwareJarOutputStream;
import org.ops4j.pax.exam.spi.probesupport.ResourceWriter;

/**
 * Responsible for creating the on-the fly bundle.
 *
 * @author Toni Menzel (toni@okidokiteam.com)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since May 29, 2008
 */
public class BundleBuilder
{

    private ResourceWriter m_resourceWriter;

    private Properties[] m_props;

    /**
     * Constructor.
     *
     * @param resourceWriter locator that gathers all resources that have to be inside the regression probe
     * @param p               properties to be merged.
     */
    public BundleBuilder( final ResourceWriter resourceWriter, Properties... p )
    {

        NullArgumentException.validateNotNull( resourceWriter, "resourceWriter" );

        m_resourceWriter = resourceWriter;
        m_props = p;

    }

    /**
     * Builds an osgi bundle out of settings given while creating this instance.
     * Output is being asynchronously in a new thread when reading from the InputStream returned by this method.
     *
     * @return an inputstream that must be flushed in order to actually invoke the bundle getStream process.
     */
    public InputStream build()
    {
        try
        {
            // 1. create a basic jar with all classes in it..
            final PipedOutputStream pout = new PipedOutputStream();
            PipedInputStream fis = new PipedInputStream( pout );
            new Thread()
            {

                public void run()
                {
                    JarOutputStream jos;
                    try
                    {
                        jos = new DuplicateAwareJarOutputStream( pout );
                        m_resourceWriter.write( jos );
                        jos.close();
                    } catch( IOException e )
                    {
                        //throw new RuntimeException( e );
                    } finally
                    {
                        try
                        {
                            pout.close();
                        } catch( Exception e )
                        {
                            //  throw new TestExecutionException( "Cannot close builder stream ??", e );
                        }
                    }
                }
            }.start();

            //      m_refs.setProperty( Constants.BUNDLE_SYMBOLICNAME, "BuiltByDirUrlHandler" );

            InputStream result = BndWrapper.createBundle( fis, m_resourceWriter.toString(), m_props );
            fis.close();
            pout.close();
            return result;
        } catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }


}