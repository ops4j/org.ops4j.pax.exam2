/*
 * Copyright 2009,2010 Toni Menzel.
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
package org.ops4j.pax.exam;

/**
 * This references a test. Use it to call a test on TestContainers.
 *
 * @author Toni Menzel
 * @since Jan 11, 2010
 */
public interface TestAddress
{

    /**
     * Identifier of a single addressable test.
     *
     * @return a signature. Never Null.
     */
    public String signature();

    /**
     * This contains encoded instruction (if required) in order to instantiate, configure and invoke a test.
     *
     * For example:
     * Foo;myTest could encode the fact that you need to instantiate Foo and invoke myTest.
     *
     * @return valid instruction or null.
     */
    String getInstruction();
}
