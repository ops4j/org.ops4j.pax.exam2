/*
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
package org.ops4j.pax.exam.container.eclipse.impl.repository;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.ops4j.pax.exam.container.eclipse.impl.parser.AbstractParser;
import org.osgi.framework.Version;

/**
 * Serializable {@link Version}
 * 
 * @author Christoph Läubrich
 *
 */
public abstract class VersionSerializable implements Serializable {

    private static final long serialVersionUID = 5434221657609769247L;
    private transient Version version;

    public VersionSerializable(Version version) {
        this.version = version;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(version.toString());
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        version = AbstractParser.stringToVersion((String) in.readObject());
    }

    private void readObjectNoData() throws ObjectStreamException {

    }

    public final Version getVersion() {
        return version;
    }

}
