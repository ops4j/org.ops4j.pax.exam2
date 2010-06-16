package org.ops4j.pax.exam.junit;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.container.TestProbeBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: tonit
 * Date: Jun 16, 2010
 * Time: 6:15:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ExxamReactor
{

    void addConfiguration( Option[] options );

    void addProbe( TestProbeBuilder addTest );

    StagedExamReactor stage();
}
