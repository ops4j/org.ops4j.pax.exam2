package org.ops4j.pax.exam.acceptance.demo;

import org.ops4j.pax.exam.acceptance.ClientConfiguration;
import org.ops4j.pax.exam.acceptance.SessionSpec;
import org.ops4j.pax.exam.acceptance.junit4.OSGiTestSubjectRule;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;

public class Lion extends OSGiTestSubjectRule {
    public Lion(SessionSpec spec) {
        super(composite(
                bundle("mvn:org.apache.felix/org.apache.felix.eventadmin/1.4.8"),
                bundle("mvn:org.apache.felix/org.apache.felix.scr/2.0.12"),
                bundle("mvn:org.apache.felix/org.apache.felix.http.jetty/3.1.6"),
                bundle("mvn:org.apache.felix/org.apache.felix.http.servlet-api/1.1.2"),
                bundle("mvn:org.apache.felix/org.apache.felix.webconsole/4.2.16/jar/all"),
                bundle("mvn:org.apache.felix/org.apache.felix.configadmin/1.8.14")
        ), spec);
    }

    public ClientConfiguration correctCredentials() {
        return new ClientConfiguration("admin","admin");
    }

    public ClientConfiguration wrongCredentials() {
        return new ClientConfiguration("wrong","wrong");
    }
}
