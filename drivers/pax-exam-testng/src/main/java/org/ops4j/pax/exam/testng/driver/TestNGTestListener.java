/*
 * Copyright 2016 Harald Wellmann.
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
package org.ops4j.pax.exam.testng.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestEvent;
import org.ops4j.pax.exam.TestEventType;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;

/**
 * @author hwellmann
 *
 */
public class TestNGTestListener implements TestListener {

    private Map<TestDescription, TestEvent> resultMap = new HashMap<>();

    @Override
    public void testStarted(TestDescription description) {
        // TODO Auto-generated method stub

    }

    @Override
    public void testFinished(TestDescription description) {
        if (!resultMap.containsKey(description)) {
            resultMap.put(description, new TestEvent(TestEventType.TEST_FINISHED, description));
        }
    }

    @Override
    public void testFailure(TestFailure failure) {
        TestDescription description = failure.getDescription();
        if (!resultMap.containsKey(description)) {
            resultMap.put(description, new TestEvent(TestEventType.TEST_FAILED, description, failure.getException()));
        }
    }

    @Override
    public void testAssumptionFailure(TestFailure failure) {
        // TODO Auto-generated method stub

    }

    @Override
    public void testIgnored(TestDescription description) {
        if (!resultMap.containsKey(description)) {
            resultMap.put(description, new TestEvent(TestEventType.TEST_IGNORED, description));
        }
    }

    public TestEvent getResult(TestDescription description) {
        return resultMap.get(description);
    }

    public Set<TestDescription> getKeys() {
        return resultMap.keySet();
    }
}
