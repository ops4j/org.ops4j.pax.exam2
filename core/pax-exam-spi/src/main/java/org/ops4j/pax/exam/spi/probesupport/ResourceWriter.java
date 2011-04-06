/*
 * Copyright 2008 Toni Menzel.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.spi.probesupport;

import java.io.IOException;
import java.util.jar.JarOutputStream;

/**
 * @author Toni Menzel (tonit)
 * @since Dec 10, 2008
 */
public interface ResourceWriter {

    /**
     * Can write contents as jar
     *
     * @param jos to write to
     *
     * @throws IOException problems
     */
    void write( JarOutputStream jos )
        throws IOException;
}