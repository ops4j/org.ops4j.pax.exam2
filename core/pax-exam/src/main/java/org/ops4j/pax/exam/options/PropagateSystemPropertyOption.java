/*
 * Copyright 2013 Harald Wellmann
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

import org.ops4j.pax.exam.Option;

/**
 * Option for propagating a system property from the driver VM to the container VM.
 * Only meaningful for remote containers.
 * <p>
 * If the given system property is set in the driver VM, Pax Exam will set the system property
 * with the same key to the same value in the container VM.
 * 
 * @author Harald Wellmann
 */
public class PropagateSystemPropertyOption implements Option {

    /**
     * System property key (cannot be null or empty).
     */
    private final String key;

    /**
     * Constructor.
     * 
     * @param key
     *            system property key (cannot be null or empty)
     * 
     */
    public PropagateSystemPropertyOption(final String key) {
        this.key = key;
    }

    /**
     * Getter.
     * 
     * @return system property key (cannot be null or empty)
     */
    public String getKey() {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PropagateSystemPropertyOption");
        sb.append("{key='").append(key).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PropagateSystemPropertyOption other = (PropagateSystemPropertyOption) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        }
        else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }
}
