package org.ops4j.pax.exam.acceptance.demo;

import org.junit.Rule;
import org.junit.Test;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.acceptance.SessionSpec;
import org.ops4j.pax.exam.acceptance.rest.api.RestClient;

public class LionAcceptanceTest {

    public @Rule Lion lion = new Lion(new SessionSpec("localhost",new FreePort(8282,8380).getPort()));

    @Test
    public void workflowTest() {
        RestClient loggedIn = lion.createClient( RestClient.class, lion.correctCredentials());
        loggedIn.get("/foo").then().statusCode(404);
        loggedIn.get("/system/console").then().statusCode(200);

        RestClient badUser = lion.createClient( RestClient.class, lion.wrongCredentials());
        loggedIn.get("/foo").then().statusCode(404);
        badUser.get("/system/console").then().statusCode(401);
    }
}
