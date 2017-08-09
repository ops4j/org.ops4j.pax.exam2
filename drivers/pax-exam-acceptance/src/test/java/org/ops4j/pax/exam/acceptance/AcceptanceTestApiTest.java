package org.ops4j.pax.exam.acceptance;

import java.io.IOException;
import java.io.InputStream;
import org.junit.Rule;
import org.junit.Test;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.acceptance.junit4.OSGiTestSubjectRule;
import org.ops4j.pax.exam.acceptance.junit4.TestSubjectRule;
import org.ops4j.pax.exam.acceptance.rest.api.RestClient;
import org.ops4j.pax.exam.acceptance.serviceinjection.DemoService;
import org.ops4j.pax.exam.acceptance.serviceinjection.IDemoService;
import org.ops4j.pax.tinybundles.core.BuildStrategy;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.wireadmin.WireAdmin;

import static junit.framework.TestCase.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.acceptance.rest.api.RestRequest.request;

public class AcceptanceTestApiTest {


    public @Rule
    TestSubjectRule subject = new OSGiTestSubjectRule(
            composite(
                    bundle("mvn:org.apache.felix/org.apache.felix.scr/2.0.12"),
                    bundle("mvn:org.apache.felix/org.apache.felix.http.jetty/3.1.6"),
                    bundle("mvn:org.apache.felix/org.apache.felix.http.servlet-api/1.1.2"),
                    bundle("mvn:org.apache.felix/org.apache.felix.webconsole/4.2.16/jar/all"),
                    bundle("mvn:org.apache.felix/org.apache.felix.configadmin/1.8.14")
                    //vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=7788"),
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
    public void testRestClient() throws IOException, InvalidSyntaxException {
        InputStream in = TinyBundles.bundle()
                .add(IDemoService.class)
                .add(DemoService.class)
                .set("Export-Packages","*")
                .build(TinyBundles.withBnd());

        subject.getTestContainer().installProbe(in);

        ClientConfiguration correct = new ClientConfiguration("admin","admin");
        RestClient rest = subject.createClient(RestClient.class,correct);
        rest.getWithRetry(request("/foo")).then().statusCode(404);
        rest.getWithRetry(request("/system/console")).then().statusCode(200);

        IDemoService service = subject.createClient(IDemoService.class);
        int sum = service.sum(1,2);
        assertEquals(3,sum);
    }

    @Test
    public void testRemoteServiceCall() throws IOException, InvalidSyntaxException {
        InputStream in = TinyBundles.bundle()
                .add(IDemoService.class)
                .add(DemoService.class)
                .set("Private-Packages","*")
                .build(TinyBundles.withBnd());

        subject.getTestContainer().installProbe(in);

        IDemoService service = subject.createClient(IDemoService.class);
        int sum = service.sum(1,2);
        assertEquals(3,sum);
    }
}
