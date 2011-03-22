package org.ops4j.pax.exam.spi.reactors;

import java.util.List;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.StagedExamReactor;

/**
 * 
 */
public class AllConfinedStagedReactorTest extends BaseStagedReactorTest {

    @Override
    protected StagedExamReactor getReactor( List<TestContainer> containers, List<TestProbeProvider> providers )
    {
        return new AllConfinedStagedReactor(containers,providers);
    }
}
