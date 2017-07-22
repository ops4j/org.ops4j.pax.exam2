package org.ops4j.pax.exam.acceptance.rest.api;

public interface RestResult {
    RestResult then();

    void statusCode(int status);
}
