package org.ops4j.pax.exam.acceptance.rest.api;

public interface RestClient {

    RestResult getWithRetry(String s, Object... payload);

    RestResult get(String s, Object... payload);

    RestResult post(String s, Object... payload);

    RestResult put(String s, Object... payload);

}
