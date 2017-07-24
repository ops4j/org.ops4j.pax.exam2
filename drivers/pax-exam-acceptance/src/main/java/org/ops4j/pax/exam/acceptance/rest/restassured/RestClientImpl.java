package org.ops4j.pax.exam.acceptance.rest.restassured;

import io.restassured.response.Response;
import org.ops4j.pax.exam.acceptance.ClientConfiguration;
import org.ops4j.pax.exam.acceptance.SessionSpec;
import org.ops4j.pax.exam.acceptance.rest.api.RestClient;
import org.ops4j.pax.exam.acceptance.rest.api.RestResult;

import static io.restassured.RestAssured.given;


public class RestClientImpl implements RestClient {

    private final SessionSpec env;
    private final ClientConfiguration clientConfig;

    // TODO: Setup Restassured here properly.
    public RestClientImpl(SessionSpec sessionSpec, ClientConfiguration clientConfiguration) {
        this.env = sessionSpec;
        this.clientConfig = clientConfiguration;
    }

    @Override
    public RestResult get(String s) {
        return new RestResultImpl(given().auth().basic(clientConfig.getUser(), clientConfig.getPassword()).when().get(s));
    }

    @Override
    public RestResult post(String s) {
        return new RestResultImpl(given().auth().basic(clientConfig.getUser(), clientConfig.getPassword()).when().post(s));
    }

    private class RestResultImpl implements RestResult {
        private final Response response;

        public RestResultImpl(Response res) {
            this.response = res;
        }

        @Override
        public RestResult then() {
            return this;
        }

        @Override
        public void statusCode(int status) {
            response.then().statusCode(status);
        }
    }
}
