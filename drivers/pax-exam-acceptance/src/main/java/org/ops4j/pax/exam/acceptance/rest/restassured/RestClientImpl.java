package org.ops4j.pax.exam.acceptance.rest.restassured;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.ops4j.pax.exam.acceptance.ClientConfiguration;
import org.ops4j.pax.exam.acceptance.SessionSpec;
import org.ops4j.pax.exam.acceptance.rest.api.RestClient;
import org.ops4j.pax.exam.acceptance.rest.api.RestResult;

import static io.restassured.RestAssured.given;

import java.util.concurrent.ExecutionException;


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
        int retries = this.env.getRetries() * 3;
        Exception retryException = null;
        for (int i = 0;i<retries;i++) {
            try {
                res = given().auth().basic(clientConfig.getUser(), clientConfig.getPassword()).when().get(s);
                if (res.statusCode() != 404) {
                    return new RestResultImpl(res, null, retries);
                }
                Thread.sleep(200);

            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (Exception e ) {
                // retries..
            	retryException = e;
            }
        }
        return new RestResultImpl(res, retryException, this.env.getRetries());
    }

    private class RestResultImpl implements RestResult {
        private final Response response;
		private Exception e;
		private int retries;

        public RestResultImpl(Response res, Exception e, int retries) {
            this.response = res;
			this.e = e;
			this.retries = retries;
        }

        @Override
        public RestResult then() {
            return this;
        }

        @Override
        public void statusCode(int status) {
        	if (response == null) {
        		if (e != null) {
        			throw new RuntimeException("failed after maximum retries "+retries, e);
        		} else {
        			throw new IllegalStateException("no response");
        		}
        	}
            response.then().statusCode(status);
        }
    }
}
