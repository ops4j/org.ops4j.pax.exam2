package org.ops4j.pax.exam.nat.internal;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;

/**
 * Created by IntelliJ IDEA.
 * User: tonit
 * Date: Jul 5, 2010
 * Time: 1:28:11 PM
 * To change this template use File | Settings | File Templates.
 */
public interface OptionProvider
{

    Iterable<OptionDescription> getContainers( Option[] options );
}
