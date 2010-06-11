/*
 * Copyright (C) 2010 Okidokiteam
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
package com.okidokiteam.exxam.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * Simple test
 */
@RunWith( JUnit4TestRunner.class )
public class A1
{

    @Test
    public void runTest1()
    {
        System.out.println( "************ runTest1" );
    }

    @Test
    public void runTest2()
    {
        System.out.println( "************ runTest2" );
    }

    @Test
    public void runTest3()
    {
        System.out.println( "************ runTest3" );
    }
}
