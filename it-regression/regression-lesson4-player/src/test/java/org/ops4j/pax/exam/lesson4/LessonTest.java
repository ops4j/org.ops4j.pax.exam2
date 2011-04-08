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
package org.ops4j.pax.exam.lesson4;

import org.junit.Test;
import org.osgi.service.log.LogService;
import org.ops4j.pax.exam.swoosh.Player;
import org.ops4j.pax.exam.swoosh.probes.WaitForService;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * This is the latest face of Pax Exam. Its mostly written to:
 * 1. Show how simple it is to use the API from Lesson 1 and 2 to create a small layer on top that is very simple to use and also quite small (look at {@link Player} class.
 * 2. Have a very comprehensive way to run pre-built tests.
 *
 * You see pretty much the essence of Pax Exam packed into a very small space:
 * - add Options
 * - add Tests/Probe
 * .. and you are ready to "play" (see  {@link org.ops4j.pax.exam.swoosh.Player#play()}
 *
 * This can be used anywwhere, not only in JUnit tests.
 */
public class LessonTest {

    @Test
    public void lessonTest()
        throws Exception
    {
        new Player().with(
            options(
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-api" ).version( "1.6.1" ).startLevel( 1 ),
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-service" ).version( "1.6.1" ).start()
            )
        ).test( WaitForService.class, LogService.class.getName(), 5000 ).play();

    }
}
