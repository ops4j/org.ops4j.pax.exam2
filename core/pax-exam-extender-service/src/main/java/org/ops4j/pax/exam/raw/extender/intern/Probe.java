/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.raw.extender.intern;

import java.util.Dictionary;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ops4j.pax.exam.ProbeInvoker;

/**
 * @author Toni Menzel
 * @since Jan 10, 2010
 */
public class Probe {

    private final String service;
    private final ProbeInvoker impl;
    private final Dictionary<String, ?> dict;

    public Probe(String service, ProbeInvoker impl, Dictionary<String, ?> dict) {
        this.service = service;
        this.impl = impl;
        this.dict = dict;
    }

    public ServiceRegistration register(BundleContext ctx) {
        return ctx.registerService(service, impl, dict);
    }
}
