/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.raw.extender;

/**
 * @author Toni Menzel
 * @since Jan 7, 2010
 */
public class Constants
{

    /**
     * Manifest header specifying the full qualified name of the test class.
     */
    public static final String PROBE_TEST_CLASS = "PaxExam-TestClassName";
    /**
     * Manifest header specifying the test method name in the test class.
     */
    public static final String PROBE_TEST_METHOD = "PaxExam-TestMethodName";
    /**
     * Test bundle symbolic name.
     */
    public static final String PROBE_SYMBOLICNAME = "pax-exam-probe";
    /**
     * Test class name service attribute.
     */
    public static final String TEST_CASE_ATTRIBUTE = "org.ops4j.pax.exam.test.class";
    /**
     * Test method name service attribute.
     */
    public static final String TEST_METHOD_ATTRIBUTE = "org.ops4j.pax.exam.test.method";

}
