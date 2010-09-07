package org.ops4j.pax.exam.spi;

/**
 * Separates logical test invocations from underlying reactor strategy.
 * You get an instance from {@link org.ops4j.pax.exam.spi.ExxamReactor}
 */
public interface StagedExamReactor {

    /**
     * Invoke an actual test. The reactor implementation will take care of (perhaps) instantiating a TestContainer or
     * reusing an existing one and passing the call.
     * You get the {@link TestAddress} from {@link org.ops4j.pax.exam.spi.TestProbeBuilder#getTests()}.
     *
     * @param address reference to a concrete, single test.
     * @throws Exception in case of a problem.
     */
    void invoke(TestAddress address) throws Exception;

    /**
     * When you are done with using your reactor make sure to call this method so underlying resources (like TestContainers
     * and connections) can be cleaned up.
     */
    void tearDown();

}
