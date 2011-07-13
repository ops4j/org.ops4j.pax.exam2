/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.spi.intern;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.ops4j.pax.exam.spi.ContentCollector;

/**
 *
 */
public class CompositeCollector implements ContentCollector {

    private ContentCollector[] m_collectors;

    public CompositeCollector( ContentCollector... collectors )
    {
        m_collectors = collectors;
    }

    public void collect( Map<String, URL> map )
        throws IOException
    {
        for( ContentCollector c : m_collectors ) {
            c.collect( map );
        }
    }
}
