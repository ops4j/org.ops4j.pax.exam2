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
package org.ops4j.pax.exam;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Toni Menzel
 * @since Dec 4, 2009
 */
public interface ProbeInvoker {

    /**
     * Executes the regression method.
     *
     * @param args Arguments to the called address.
     *
     * @throws ClassNotFoundException - If the regression class cannot be loaded
     * @throws InstantiationException - If an instance of the regression class cannot be created
     * @throws IllegalAccessException - Re-thrown from reflective invokation of regression method
     * @throws java.lang.reflect.InvocationTargetException
     *                                - Re-thrown from reflective invokation of regression method
     */
    void call( Object... args )
        throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException;
}
