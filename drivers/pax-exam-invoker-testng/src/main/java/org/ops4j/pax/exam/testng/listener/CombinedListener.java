/*
 * Copyright 2016 Harald Wellmann
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
package org.ops4j.pax.exam.testng.listener;

import org.testng.IClassListener;
import org.testng.IConfigurable;
import org.testng.IHookable;
import org.testng.IMethodInterceptor;
import org.testng.ISuiteListener;

/**
 * Union of all relevant TestNG listener interfaces.
 *
 * @author Harald Wellmann
 * @since 5.0.0
 */
public interface CombinedListener
    extends ISuiteListener, IClassListener, IMethodInterceptor, IHookable, IConfigurable {

}
