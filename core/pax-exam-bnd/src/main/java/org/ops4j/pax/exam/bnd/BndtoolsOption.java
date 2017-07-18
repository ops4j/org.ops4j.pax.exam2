package org.ops4j.pax.exam.bnd;

import java.io.File;
import org.ops4j.pax.exam.Option;


public class BndtoolsOption implements Option {

    public BndtoolsOption(File location) {

    }

    /**
     * Add workspace-built bundle of name symbolicName to system-under-test.
     *
     * @param symbolicName BSN of bundle to be installed. Will be queried from Bnd Workspace.
     * @return this.
     */
    Option bundle(String symbolicName) {
        return this;
    }

    /**
     * Add all bundles resolved by bndrun file to system-under-test.
     *
     * @param project project containing the bndrun file to be used.
     *
     * @return this.
     */
    Option fromBndrun(String project) {
        return this;
    }

}
