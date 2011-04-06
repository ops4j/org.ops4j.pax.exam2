/*
 * Copyright 2009 Alin Dreghiciu.
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
 * Pax Exam related constants.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 22, 2009
 */
public interface Constants {

    /**
     * The start level at which Pax Exam system bundles are to be started.
     */
    static final int START_LEVEL_SYSTEM_BUNDLES = 2;

    /**
     * The start level at which Pax Exam test bundle is to be started.
     */
    static final int START_LEVEL_DEFAULT_PROVISION = 3;

    /**
     * The start level at which Pax Exam test bundle is to be started. This is also the startlevel, the test container reaches in start.
     */
    static final int START_LEVEL_TEST_BUNDLE = 5;

    /**
     * Timeout specifing that there should be no waiting.
     */
    long NO_WAIT = 0;
    /**
     * Timeout specifing that it should wait forever.
     */
    long WAIT_FOREVER = Long.MAX_VALUE;
    /**
     * Timeout specifing that it should wait .
     */
    long WAIT_5_MINUTES = 5 * 60 * 1000;
}
