package org.ops4j.pax.exam.container.externalframework.internal;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.TestContainerException;

/**
 * 
 * This code is extract from pax runner.
 * 
 * Handles the workflow of creating the platform. 
 * Concrete platforms should implement only the PlatformBuilder
 * interface.
 * 
 */
public class Download
{
	 /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( Download.class );
   
    
    
    /**
     * Downloads files from urls.
     *
     * @param workDir          the directory where to download bundles
     * @param url              of the file to be downloaded
     * @param displayName      to be shown during download
     * @param overwrite        if the bundles should be overwritten
     * @param checkAttributes  whether or not to check attributes in the manifest
     * @param failOnValidation if validation fails should or not fail with an exception (or just return null)
     * @param downloadFeeback  whether or not downloading process should display fine grained progres info
     *
     * @return the File corresponding to the downloaded file, or null if the bundle is invalid (not an osgi bundle)
     *
     * @throws PlatformException if the url could not be downloaded
     */
    public static File download( final File workDir,
                           final URL url,
                           final Boolean overwrite,
                           final boolean checkAttributes,
                           final boolean failOnValidation,
                           final boolean downloadFeeback)
        
    {
        LOGGER.debug( "Downloading [" + url + "]" );
        String protocol = url.getProtocol();
        String name = null;
        if ("mvn".equals(protocol)) {
             String path = url.toExternalForm();
             String[] parts = path.split("/");
             StringBuilder sb = new StringBuilder();
             sb.append(parts[0].substring(4));
             sb.append(".");
             sb.append(parts[1]);
             sb.append("-");
             sb.append(parts[2]);
             if (parts.length == 5) {
                sb.append("-");
                sb.append(parts[4]);
             }
             sb.append(".");
             if (parts.length >= 4)
            	 sb.append(parts[3]);
             else
            	 sb.append("jar");
             name = sb.toString();
        } else if ("file:".equals(protocol)) {
            String path = url.getPath();
            name = path.substring(path.lastIndexOf("/"));
        } else {
        	String path = url.getPath();
            name = path.substring(path.lastIndexOf("/"));
        }
        if (name == null) {
            throw new TestContainerException("Cannot compute the name form "+url);
        }
        File destination = new File( workDir, name );

        // download the bundle only if is a forced overwrite or the file does not exist or the file is there but is
        // invalid
        boolean forceOverwrite = overwrite || !destination.exists();
        if ( forceOverwrite )
        {
            try
            {
                LOGGER.debug( "Creating new file at destination: " + destination.getAbsolutePath() );
                destination.getParentFile().mkdirs();
                destination.createNewFile();
                FileOutputStream os = new FileOutputStream( destination );
                
                StreamUtils.copyStream(url.openStream(), os, true );
                LOGGER.debug( "Succesfully downloaded to [" + destination + "]" );
               
            }
            catch ( IOException e )
            {
                throw new TestContainerException( "[" + url + "] could not be downloaded", e );
            }
        }
        
        return destination;
    }

   
}
