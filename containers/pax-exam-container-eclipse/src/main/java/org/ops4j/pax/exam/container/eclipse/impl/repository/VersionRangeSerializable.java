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
import org.osgi.framework.VersionRange;

/**
 * Serializable {@link VersionRange}
 * 
 * @author Christoph Läubrich
 *
 */
public abstract class VersionRangeSerializable implements Serializable {

    private static final long serialVersionUID = 5434221657609769245L;
    private transient VersionRange versionRange;

    public VersionRangeSerializable(VersionRange version) {
        this.versionRange = version;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(versionRange.toString());
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        versionRange = AbstractParser.stringToVersionRange((String) in.readObject());
    }

    private void readObjectNoData() throws ObjectStreamException {
    }

    public final VersionRange getVersionRange() {
        return versionRange;
    }

}
