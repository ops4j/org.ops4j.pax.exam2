/*
 * Copyright 2015 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.wildfly80;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

public class WildFly80PortConflictTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldNotStartContainerWhenPortIsTaken() throws IOException  {
        try (ServerSocket socket = new ServerSocket(9990)) {
            ExamSystem system = PaxExamRuntime.createTestSystem();
            WildFly80TestContainer tc = new WildFly80TestContainer(system, null);

            thrown.expect(TestContainerException.class);
            thrown.expectMessage("Port 9990 is already taken.");
            tc.start();
        }
    }
}
