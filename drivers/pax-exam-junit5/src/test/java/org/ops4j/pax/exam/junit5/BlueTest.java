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
public class BlueTest {

    @BeforeAll
    public static void beforeAll() {
        System.out.println("Blue beforeAll");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("Blue beforeEach");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("Blue afterAll");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("Blue afterEach");
    }

    @Test
    public void blue1() {
        System.out.println("blue1");
    }

    @Test
    public void blue2() {
        System.out.println("blue2");
    }

}
