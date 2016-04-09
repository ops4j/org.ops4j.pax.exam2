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
package org.ops4j.pax.exam;

import java.io.Serializable;

public class TestEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TestEventType type;
    private final TestDescription description;
    private final Throwable exception;



    public TestEvent(TestEventType event, TestDescription description) {
        this(event, description, null);
    }

    public TestEvent(TestEventType type, TestDescription description, Throwable exception) {
        this.type = type;
        this.description = description;
        this.exception = exception;
    }


    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }


    /**
     * @return the type
     */
    public TestEventType getType() {
        return type;
    }


    /**
     * @return the description
     */
    public TestDescription getDescription() {
        return description;
    }


    /**
     * @return the exception
     */
    public Throwable getException() {
        return exception;
    }
}
