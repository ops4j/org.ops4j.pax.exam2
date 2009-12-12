package org.ops4j.pax.exam.quickbuild;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 *
 */
public interface SnapshotBuilder
{

    /**
     * Take a new snapshot from a reference build and a corresponding workingfolder(s). (classes output usually)
     *
     * @param referenceBuild uri to a jar that will be the reference (build by a real build management tool like maven
     * @param workFolder    classes output folders (usually it is just one)
     * @return produced snapshot
     */
    Snapshot take( InputStream referenceBuild, File workFolder );

    /**
     * load a previously stored snapshot from disk
     *
     * @param load reference to what has been produced by {@link Snapshot#write}
     *
     * @return loaded snapshot instance.
     */
    Snapshot load( InputStream load )
        throws IOException;


}
