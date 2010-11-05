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
package org.ops4j.pax.exam.options;

/**
 * @author Toni Menzel
 * @since Jan 26, 2010
 */
public enum ReUsePolicy
{

    NEVER, // the pax exam 1.x behaviour
    TEST,  // new regression container per regression class
    ALWAYS // just create one container (-connection) for the entire JUnit Run

}
