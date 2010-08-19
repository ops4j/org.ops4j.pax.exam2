package org.ops4j.pax.exam.spi;

/**
 * Separates logical test invocations from underlying reactor strategy.
 */
public interface StagedExamReactor
{

    void invoke( TestAddress call )
        throws Exception;

    void tearDown();

}
