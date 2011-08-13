/*
 * Copyright 2011 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.regression.pde.impl;

import java.util.Hashtable;

import org.ops4j.pax.exam.regression.pde.EnglishHelloService;
import org.ops4j.pax.exam.regression.pde.HelloService;
import org.ops4j.pax.exam.regression.pde.LatinHelloService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext bc) throws Exception {
        EnglishHelloService service = new EnglishHelloService();
        Hashtable<String,String> props = new Hashtable<String, String>();
        props.put("language", "en");
        bc.registerService(HelloService.class.getName(), service, props);

        LatinHelloService latinService = new LatinHelloService();
        Hashtable<String,String> latinProps = new Hashtable<String, String>();
        latinProps.put("language", "la");
        bc.registerService(HelloService.class.getName(), latinService, latinProps);
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
    }
}
