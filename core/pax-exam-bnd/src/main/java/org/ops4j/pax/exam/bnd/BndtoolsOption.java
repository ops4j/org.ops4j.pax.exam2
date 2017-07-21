package org.ops4j.pax.exam.bnd;

import aQute.bnd.build.Container;
import aQute.bnd.build.Run;
import aQute.bnd.build.Workspace;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.ops4j.pax.exam.Option;

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.url;


public class BndtoolsOption implements Option {

    private final Workspace workspace;
    private final File location;

    public BndtoolsOption(File location) {
        try {
            workspace = Workspace.getWorkspace(location);
            workspace.setOffline(false);
            this.location = location;
        } catch (Exception e) {
            throw new RuntimeException("Underlying Bnd Exception: ",e);
        }
    }

    /**
     * Add workspace-built bundle of name symbolicName to system-under-test.
     *
     * @param bsn BSN of bundle to be installed. Will be queried from Bnd Workspace.
     * @return this.
     */
    public Option bundle(String bsn) {
        Collection<Option> urls = new ArrayList<>();
        try {
            for (File file : workspace.getProject(bsn).getBuildFiles()) {
                urls.add(url(file.toURI().toASCIIString()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Underlying Bnd Exception: ",e);
        }
        return composite(urls.toArray(new Option[urls.size()]));
    }

    /**
     * Add all bundles resolved by bndrun file to system-under-test.
     *
     * @param runFileSpec bndrun file to be used.
     *
     * @return this.
     */
    public Option fromBndrun(String runFileSpec)  {

        try {
            File runFile = new File(location, runFileSpec);
            Run bndRunInstruction = new Run(workspace, runFile.getParentFile(), runFile);
            return bndToExam(bndRunInstruction);
        } catch (Exception e) {
            throw new RuntimeException("Underlying Bnd Exception: ",e);
        }
    }

    private Option bndToExam(Run bndRunInstruction) throws Exception {
        Collection<Option> urls = new ArrayList<>();
        Collection<Container> bundles = bndRunInstruction.getRunbundles();
        for (Container container : bundles) {
            urls.add(url(container.getFile().toURI().toASCIIString()));
        }
        return composite(urls.toArray(new Option[urls.size()]));
    }
}
