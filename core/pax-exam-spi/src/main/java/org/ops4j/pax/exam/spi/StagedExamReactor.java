package org.ops4j.pax.exam.spi;

import org.ops4j.pax.exam.spi.container.ProbeCall;

/**
 * Separates logical test invocations from underlying reactor strategy.
 */
public interface StagedExamReactor
{

    void invoke( ProbeCall call )
        throws Exception;

    void tearDown();

}
