/*
 * Copyright 2013 Christoph Läubrich
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
package org.ops4j.pax.exam.osgi;

/**
 * A service that will be present when OBR starts and allows to validate an OBR
 * operation has finished with success
 */
public interface OBRValidation {

    /**
     * can be used to validate that the OBR Operation finished without any
     * error, throws an {@link RuntimeException} if any error was recorded,
     * returns with a no-op otherwhise. Can be used in After/Before methods.
     */
    public void validate() throws RuntimeException;
}
