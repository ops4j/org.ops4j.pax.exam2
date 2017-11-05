/*
 * Copyright 2016 Harald Wellmann.
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
package org.ops4j.pax.exam.junit5;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtendWith;

/**
 * @author hwellmann
 *
 */
@ExtendWith(MockExtension.class)
public class YellowTest {

    @BeforeAll
    public static void beforeAll() {
        System.out.println("Yellow beforeAll");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("Yellow beforeEach");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("Yellow afterAll");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("Yellow afterEach");
    }

    @Test
    public void yellow1() {
        System.out.println("yellow1");
    }

    @Test
    public void yellow2() {
        System.out.println("yellow2");
    }

}
