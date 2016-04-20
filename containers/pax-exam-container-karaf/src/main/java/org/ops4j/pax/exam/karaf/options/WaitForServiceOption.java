/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.karaf.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.RelativeTimeout;

/**
 * Per default the folder pax-exam is deleting the test directories after a test is over. If you want to keep those
 * directories (for later evaluation) simply set this option.
 */
public class WaitForServiceOption implements Option {
    final private String serviceClassName;

    final private RelativeTimeout timeout;

    public WaitForServiceOption(String serviceClassName, long timeout) {
        this.serviceClassName = serviceClassName;
        this.timeout = new RelativeTimeout(timeout);
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public RelativeTimeout getTimeout() {
        return timeout;
    }
}
