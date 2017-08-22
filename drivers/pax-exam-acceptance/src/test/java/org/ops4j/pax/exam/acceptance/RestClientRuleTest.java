package org.ops4j.pax.exam.acceptance;

import org.junit.Rule;
import org.junit.Test;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.acceptance.junit4.ClientRule;
import org.ops4j.pax.exam.acceptance.junit4.OSGiTestSubjectRule;
import org.ops4j.pax.exam.acceptance.rest.api.RestClient;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.acceptance.rest.api.RestRequest.request;

public class RestClientRuleTest {

    public @Rule
    ClientRule<RestClient> subject =
            new ClientRule<>(new OSGiTestSubjectRule(
                    composite(
                            bundle("mvn:org.apache.felix/org.apache.felix.eventadmin/1.4.8"),
                            bundle("mvn:org.apache.felix/org.apache.felix.scr/2.0.12"),
                            bundle("mvn:org.apache.felix/org.apache.felix.http.jetty/3.1.6"),
                            bundle("mvn:org.apache.felix/org.apache.felix.http.servlet-api/1.1.2"),
                            bundle("mvn:org.apache.felix/org.apache.felix.webconsole/4.2.16/jar/all"),
                            bundle("mvn:org.apache.felix/org.apache.felix.configadmin/1.8.14")
                    ), getSessionSpec()
            ), RestClient.class, new ClientConfiguration("admin", "admin")
            );

    private SessionSpec getSessionSpec() {
        return new SessionSpec("localhost",new FreePort(8287,8380).getPort(),5);
    }

    @Test
    public void workflowTest() {
        subject.client().getWithRetry(request("/foo")).then().statusCode(404);
        subject.client().getWithRetry(request("/system/console")).then().statusCode(200);
    }
}
