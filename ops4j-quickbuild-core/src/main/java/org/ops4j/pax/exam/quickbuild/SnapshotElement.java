package org.ops4j.pax.exam.quickbuild;

import java.net.URI;

/**
 * @author Toni Menzel
 */
public interface SnapshotElement
{

    String name();

    URI reference();

    String checksum();

    QType type();
}
