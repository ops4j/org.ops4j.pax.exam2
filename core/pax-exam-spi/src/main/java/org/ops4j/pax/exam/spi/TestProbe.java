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
package org.ops4j.pax.exam.spi;

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

    InputStream getProbe();
}
