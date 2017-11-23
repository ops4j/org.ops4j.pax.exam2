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
package org.ops4j.pax.exam.container.eclipse;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.ops4j.pax.exam.Option;

/**
 * Allows to ignore artifacts by a pattern, this is very low level but might be required in setups
 * where the eclipse application provides artifacts that interfere with the ones provided by equinox
 * 
 * Example:
 * 
 * <pre>
 * new IgnoreItems().ignore("org.apache.geronimo.specs.atinject.link")
 *     .ignore("org.osgi.compendium.link").ignore("org.ops4j.pax.logging.api.link");
 * </pre>
 * 
 * @author Christoph Läubrich
 *
 */
public class IgnoreItems implements Option {

    private final Set<Pattern> items = new HashSet<>();

    public IgnoreItems ignore(String pattern) {
        items.add(Pattern.compile(pattern));
        return this;
    }

    public boolean isIgnored(String name) {
        for (Pattern pattern : items) {
            if (pattern.matcher(name).find()) {
                return true;
            }
        }
        return false;
    }
}
