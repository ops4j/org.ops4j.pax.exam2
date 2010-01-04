/*
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.ops4j.pax.exam.raw;

import java.io.InputStream;

/**
 * A test probe is the client side, final representation of a test you want to trigger.
 * It provides the probe itself and a list of tests you can invoke on it.
 *
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public interface TestProbe
{

    TestHandle[] getTestHandles();

    InputStream getProbe();
}
