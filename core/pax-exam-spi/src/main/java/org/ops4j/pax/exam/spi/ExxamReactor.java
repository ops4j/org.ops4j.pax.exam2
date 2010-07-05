package org.ops4j.pax.exam.spi;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.TestProbeBuilder;

/**
 * TODO doc.
 */
public interface ExxamReactor
{

    void addConfiguration( Option[] options );

    void addProbe( TestProbeBuilder addTest );

    StagedExamReactor stage();
}
