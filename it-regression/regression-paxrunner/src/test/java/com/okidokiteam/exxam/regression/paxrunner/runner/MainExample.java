/*
 * Copyright (C) 2011 Toni Menzel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okidokiteam.exxam.regression.paxrunner.runner;

import java.io.IOException;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import static org.ops4j.pax.exam.spi.container.PaxExamRuntime.*;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.profile;

/**
 * This is a very short example how to use Pax Exam API to launch some kind of server like container.
 * It does not show anything new, just the ability that starting a container works and is not shutdown automatically.
 */
public class MainExample {
    public static void main(String[] args) throws IOException {
        Option[] opts = options(
                profile("gogo")
        );
        final TestContainer container = createContainer( createSystem() );
        container.start();
    }
}
