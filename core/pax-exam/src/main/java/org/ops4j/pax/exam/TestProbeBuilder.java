/*
 * Copyright 2011 Toni Menzel.
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

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public interface TestProbeBuilder {

    TestAddress addTest( Class clazz, String methodName, Object... args );

    TestAddress addTest( Class clazz, Object... args );

    List<TestAddress> addTests( Class clazz, Method... m );

    TestProbeBuilder setHeader( String key, String value );

    TestProbeBuilder ignorePackageOf( Class... classes );

    TestProbeProvider build();
}
