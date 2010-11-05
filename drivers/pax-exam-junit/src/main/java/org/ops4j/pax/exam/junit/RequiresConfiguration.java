/*
 * Copyright 2009 Toni Menzel
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
package org.ops4j.pax.exam.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks methods belonging into a certain context (those methods marked with {@link org.junit.Test}).
 * For example, you can have multiple configuration (see {$link org.ops4j.pax.exam.junit.Configuration}) but they belong
 * to different contexts. So you annate both of them with this annotation. Then you annote the actual regression method with
 * exactly the same annotation.
 * This approach makes configuration+regression method coupling independent from nameing of methods (different from AppliesTo Annotation).
 *
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, January 9, 2009
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
public @interface RequiresConfiguration
{

    /**
     * A string that is matched against any other context marker.
     * By default (value not specified) the value is an empty string.
     * So if you skip the annotation completely it still glues together all methods.
     * (in this case you should not have clashing methods in your regression class like multiple configurations).
     *
     * @return an arbitrary named string referencing this context.
     */
    public abstract String[] value() default { "" };

}
