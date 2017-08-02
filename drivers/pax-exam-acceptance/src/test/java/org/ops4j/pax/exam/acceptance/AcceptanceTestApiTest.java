package org.ops4j.pax.exam.acceptance;

import org.junit.Rule;
import org.junit.Test;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.acceptance.junit4.OSGiTestSubjectRule;
import org.ops4j.pax.exam.acceptance.junit4.TestSubjectRule;
import org.ops4j.pax.exam.acceptance.rest.api.RestClient;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;

public class AcceptanceTestApiTest {


    public @Rule
    TestSubjectRule subject = new OSGiTestSubjectRule(
            composite(
                    bundle("mvn:org.apache.felix/org.apache.felix.scr/2.0.12"),
                    bundle("mvn:org.apache.felix/org.apache.felix.http.jetty/3.1.6"),
                    bundle("mvn:org.apache.felix/org.apache.felix.http.servlet-api/1.1.2"),
                    bundle("mvn:org.apache.felix/org.apache.felix.webconsole/4.2.16/jar/all"),
                    bundle("mvn:org.apache.felix/org.apache.felix.configadmin/1.8.14")
            )
    ,getSessionSpec());

    // kinda like docker port exposure?
    private SessionSpec getSessionSpec() {
        SessionSpec sessionSpec = new SessionSpec();
        sessionSpec.setHost("localhost");
        sessionSpec.setPort( new FreePort(8280,8380).getPort());
        return sessionSpec;
    }

    @Test
    public void workflowTest() {
        ClientConfiguration correct = new ClientConfiguration("admin","admin");
        RestClient rest = subject.createClient(RestClient.class,correct);
        rest.getWithRetry("/foo").then().statusCode(404);
        rest.getWithRetry("/system/console").then().statusCode(200);
    }
}
