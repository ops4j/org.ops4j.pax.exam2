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
package org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository;

import org.ops4j.pax.exam.container.eclipse.impl.repository.Unit;

/**
 * Represents a Unit in the P2 repro
 * 
 * @author Christoph Läubrich
 *
 */
public class P2Unit {

    private final Unit unit;
    private final String reproName;

    public P2Unit(Unit unit, String reproName) {
        this.unit = unit;
        this.reproName = reproName;
    }

    public Unit getUnit() {
        return unit;
    }

    public String getReproName() {
        return reproName;
    }
}
