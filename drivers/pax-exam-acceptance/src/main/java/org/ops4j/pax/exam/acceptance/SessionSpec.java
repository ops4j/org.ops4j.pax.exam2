package org.ops4j.pax.exam.acceptance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionSpec {

    @NonNull
    private String host;

    @NonNull
    private int port;

    private int retries = 5;

    // This should come from lombok..
    public SessionSpec(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
