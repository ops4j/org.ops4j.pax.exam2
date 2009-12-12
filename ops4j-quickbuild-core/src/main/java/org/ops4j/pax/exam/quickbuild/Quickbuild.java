package org.ops4j.pax.exam.quickbuild;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.io.IOException;

/**
 *
 */
public interface Quickbuild
{

    /**
     * Based on an anchor file, which is a jar file that resulted from previous maven builds,
     * implementations should crawl for updates of classes found in known output folders (relative to location of anchor)
     * and replace them inside anchor.
     *
     * @param snapshot what we use as a reference
     * @param folder   content folder that will make up the changes
     *
     * @return result of merge between snapshot and folder
     *
     * @throws java.io.IOException problems
     */
    public InputStream update( Snapshot snapshot, File folder )
        throws IOException;
}
