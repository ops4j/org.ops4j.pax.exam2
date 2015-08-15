/******************************************************************************
 * * This data and information is proprietary to, and a valuable trade secret
 * * of, Basis Technology Corp.  It is given in confidence by Basis Technology
 * * and may only be used as permitted under the license agreement under which
 * * it has been distributed, and in no other way.
 * *
 * * Copyright (c) 2015 Basis Technology Corporation All rights reserved.
 * *
 * * The technical data and information provided herein are provided with
 * * `limited rights', and the computer software provided herein is provided
 * * with `restricted rights' as those terms are defined in DAR and ASPR
 * * 7-104.9(a).
 ******************************************************************************/

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
