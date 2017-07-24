package org.ops4j.pax.exam.acceptance.rest.restassured;

import io.restassured.RestAssured;
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
            RestAssured.port = sessionSpec.getPort();

        /**
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(io.restassured.config.ObjectMapperConfig
                .objectMapperConfig().jackson2ObjectMapperFactory(new Jackson2ObjectMapperFactory() {
                    public ObjectMapper create(Class cls, String charset) {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                        return mapper;
                    }
                }));
         **/

    }

    @Override
    public RestResult get(String s) {
        Response res = null;
        int retries = this.env.getRetries();

        for (int i = 0;i<retries;i++) {
            try {
                res = given().auth().basic(clientConfig.getUser(), clientConfig.getPassword()).when().get(s);
                if (res.statusCode() != 404) {
                    return new RestResultImpl(res);
                }
                Thread.sleep(200);

            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (Exception e ) {
                // retries..
            }
        }
        return new RestResultImpl(res);
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
