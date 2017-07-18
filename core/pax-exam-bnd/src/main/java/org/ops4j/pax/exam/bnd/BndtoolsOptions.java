package org.ops4j.pax.exam.bnd;

import java.io.File;

public class BndtoolsOptions {

    private BndtoolsOptions() {

    }

    public static BndtoolsOption workspace() {
        return new BndtoolsOption( new File("."));
    }

    public static BndtoolsOption workspace(String location) {
        return new BndtoolsOption( new File(location));
    }

}
