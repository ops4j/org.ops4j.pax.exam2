package org.ops4j.pax.exam.acceptance.rest.api;

public interface RestClient {

    RestResult getWithRetry( RestRequest request);

    RestResult get( RestRequest request);

    RestResult post(RestRequest request);

    RestResult put(RestRequest request);

}
