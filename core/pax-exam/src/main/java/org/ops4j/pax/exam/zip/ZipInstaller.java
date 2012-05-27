/*
 * Copyright 2012 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.ops4j.io.StreamUtils;
import org.ops4j.io.ZipExploder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipInstaller
{
    private static final Logger LOG = LoggerFactory.getLogger( ZipInstaller.class );

    private URL zipUrl;
    private String installRoot;

    public ZipInstaller( URL zipUrl, String installRoot )
    {
        this.zipUrl = zipUrl;
        this.installRoot = installRoot;
    }

    public void downloadAndInstall() throws IOException
    {
        File installDir = new File( installRoot );
        installDir.mkdirs();

        File tempFile = File.createTempFile( "pax-exam", ".zip" );
        FileOutputStream os = null;
        LOG.info( "downloading {} to {}", zipUrl, tempFile );
        try
        {
            os = new FileOutputStream( tempFile );
            StreamUtils.copyStream( zipUrl.openStream(), os, true );

            LOG.info( "unzipping into {}", installRoot );
            ZipExploder exploder = new ZipExploder();
            exploder.processFile( tempFile.getAbsolutePath(), installRoot );
        }
        finally
        {
            tempFile.delete();
        }
    }
}
