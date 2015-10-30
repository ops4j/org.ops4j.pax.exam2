/*
 * Copyright 2015 Benson Margulies.
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

package org.ops4j.pax.exam;

import org.osgi.framework.FrameworkEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods related to {@link FrameworkEvent}.
 */
public class FrameworkEventUtils {
    private static final Map<Integer, String> EVENT_STRINGS;

    static {
        EVENT_STRINGS = new HashMap<>();
        for (Field field : FrameworkEvent.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                Integer value;
                try {
                    value = (Integer)field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to obtain value of FrameworkEvent." + field.getName());
                }
                EVENT_STRINGS.put(value, field.getName());
            }
        }
    }

    /**
     * Return a readable representation of the type of a {@link FrameworkEvent}.
     * @param frameworkEventType a value from {@link FrameworkEvent#getType()}.
     * @return the name of the field that corresponds to this type.
     */
    public static String getFrameworkEventString(int frameworkEventType) {
        return EVENT_STRINGS.get(frameworkEventType);
    }

    private FrameworkEventUtils() {
        //
    }
}
