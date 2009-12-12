package org.ops4j.pax.exam.quickbuild;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Snapshot is being used as a reference to calculate changes against.
 *
 * Must be taken when jarfile and referenced folders are considered to be "in-sync".
 *
 * Snapshot must contain all available information to the original jar.
 *
 * Also it must be available to reconstruct the entire jar just from snapshot.
 * So the we do not have to touch the original jar again (self mantained snapshot).
 */
public interface Snapshot extends Iterable<SnapshotElement>
{

    long timestamp();

    void write( OutputStream out )
        throws IOException;
}
