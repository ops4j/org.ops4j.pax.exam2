/*
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

import java.io.IOException;
import java.io.InputStream;

/**
 * A Reference that provides a Stream of data to be used
 * 
 * @author Christoph Läubrich
 *
 */
public interface StreamReference  {

    /**
     * Creates a new {@link InputStream} to read from, each call to this method creates a fresh stream
     * @return a stream from which data can be read, the caller is responsible of closing the stream after use
     * @throws IOException
     */
    InputStream createStream() throws IOException;
}
