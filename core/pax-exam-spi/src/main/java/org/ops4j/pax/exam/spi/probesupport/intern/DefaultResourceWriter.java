/*
 * Copyright 2011 Toni Menzel.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import com.google.common.io.Closeables;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.io.StreamUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.exam.spi.probesupport.ContentCollector;
import org.ops4j.pax.exam.spi.probesupport.ResourceWriter;

/**
 * Abstract Locator
 */
public class DefaultResourceWriter implements ResourceWriter {

    private final Logger LOG = LoggerFactory.getLogger( DefaultResourceWriter.class );
    private Map<String, URL> m_items;

    public DefaultResourceWriter( ContentCollector collector )
        throws IOException
    {
        m_items = new LinkedHashMap<String, URL>();
        collector.collect( m_items );
    }

    /**
     * Writes all items to the Jar Input Stream.
     *
     * @param target to write to
     */
    public void write( JarOutputStream target )
        throws IOException
    {
        NullArgumentException.validateNotNull( target, "target" );

        for( String name : m_items.keySet() ) {
            evictManifest();
            InputStream in = m_items.get( name ).openStream();
            try {
                write( name, in, target );
            } catch( Exception e ) {
                Closeables.closeQuietly( in );
            }
        }
    }

    private void evictManifest()
    {
        // check if manifest is being added manually. we need to overwrite it! (warn user)
    }

    void write( String name, InputStream fileIn, JarOutputStream target )
        throws IOException
    {
        // TODO link ASM transformation for classes here.
        if( name.endsWith( ".class" ) ) {
            fileIn = transform( fileIn );
        }
        target.putNextEntry( new JarEntry( name ) );
        StreamUtils.copyStream( fileIn, target, false );
    }

    private InputStream transform( InputStream inputStream )
        throws IOException
    {
        ClassReader cr = new ClassReader( inputStream );
        ClassWriter cw = new ClassWriter( cr, 0 );
        ClassVisitor transformer = new EvilTypeRemover( cw );
        cr.accept( transformer, 0 );
        return new ByteArrayInputStream( cw.toByteArray() );
    }

    private class EvilTypeRemover extends ClassAdapter {

        public EvilTypeRemover( ClassVisitor classVisitor )
        {
            super( classVisitor );
        }

        public MethodVisitor visitMethod( int i, String s, String s1, String s2, String[] strings )
        {
            if( s1.equals( "()[Lorg/ops4j/pax/exam/Option;" ) ) {
                LOG.debug( "Successfully removed configuration method \"" + s + "\" from regression plan." );
                return null;
            }
            else {
                return super.visitMethod( i, s, s1, s2, strings );
            }
        }
    }
}
