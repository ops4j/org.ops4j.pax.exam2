package org.ops4j.pax.exam.acceptance.rest.api;

import java.util.HashMap;
import java.util.Map;
import lombok.Value;

@Value
public class RestRequest {

    public static RestRequest request(String path) {
        return new RestRequest(path);
    }

    private Object body = "";

    private Map<String, Object> headers = new HashMap<>();

    private String path;

    public RestRequest(String path) {
        this.path = path;
    }

    public RestRequest addHeader(String key, Object value) {
        headers.put(key,value);
        return this;
    }

}
