package org.ops4j.pax.exam.acceptance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientConfiguration {
    String user;
    String password;
}
