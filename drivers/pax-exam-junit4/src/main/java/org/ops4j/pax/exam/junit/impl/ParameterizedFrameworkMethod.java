/*
 * Copyright 2013 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.junit.impl;

import java.text.MessageFormat;

import org.junit.runners.model.FrameworkMethod;
import org.ops4j.pax.exam.TestAddress;

/**
 * Decorates a framework method with a parameter index. Used for parameterized tests
 * with {@code PaxExamParameterized}.
 * 
 * @author Harald Wellmann
 * 
 */
class ParameterizedFrameworkMethod extends DecoratedFrameworkMethod {

    ParameterizedFrameworkMethod(TestAddress address, FrameworkMethod frameworkMethod) {
        super(address, frameworkMethod);
    }

    @Override
    public String getName() {
        return String.format("%s%s", frameworkMethod.getName(), nameFor((Integer) address.arguments()[0], (String) address.arguments()[1], (Object[]) address.arguments()[2]));
    }

    private String nameFor(int index, String namePattern, Object[] parameters) {
        String finalPattern = namePattern.replaceAll("\\{index\\}", Integer.toString(index));
        String name = MessageFormat.format(finalPattern, parameters);
        return "[" + name + "]";
    }
}
