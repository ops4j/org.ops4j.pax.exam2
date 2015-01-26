/*
 * Copyright 2015 Harald Wellmann.
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
package org.ops4j.pax.exam.regression.multi.category;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;


/**
 * @author Harald Wellmann
 *
 */
public class CategoriesInvokerTest {

    private List<String>  methodNames = new ArrayList<>();
    private JUnitCore junit;

    @Before
    public void before() {
        junit = new JUnitCore();
        junit.addListener(new RunListener() {
            @Override
            public void testStarted(Description description) throws Exception {
                methodNames.add(description.getMethodName());
            }
        });
    }

    @Test
    public void runIncludeBlueSuite() {
        Result result = junit.run(IncludeBlue.class);
        assertThat(result.getRunCount(), is(2));
        assertThat(methodNames, hasItems("shouldRunWhenBlue", "shouldRunClassWhenBlue"));
    }

    @Test
    public void runIncludeYellowSuite() {
        Result result = junit.run(IncludeYellow.class);
        assertThat(result.getRunCount(), is(1));
        assertThat(methodNames, hasItems("shouldRunWhenYellow"));
    }

    @Test
    public void runExcludeYellowSuite() {
        Result result = junit.run(ExcludeYellow.class);
        assertThat(result.getRunCount(), is(2));
        assertThat(methodNames, hasItems("shouldRunWhenBlue", "shouldRunClassWhenBlue"));
    }

    @Test
    public void runExcludeBlueSuite() {
        Result result = junit.run(ExcludeBlue.class);
        assertThat(result.getRunCount(), is(1));
        assertThat(methodNames, hasItems("shouldRunWhenYellow"));
    }



}
