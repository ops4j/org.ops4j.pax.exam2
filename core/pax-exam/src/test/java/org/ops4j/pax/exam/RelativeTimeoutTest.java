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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * 
 * @author Toni Menzel (toni@okidokiteam.com)
 * 
 */
public class RelativeTimeoutTest {

    @Test
    public void testDefaults() {
        RelativeTimeout rel = RelativeTimeout.TIMEOUT_DEFAULT;
        assertThat(rel.isDefault(), is(equalTo(Boolean.TRUE)));
        assertThat(rel.isNoWait(), is(equalTo(Boolean.FALSE)));
        assertThat(rel.isNoTimeout(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void testNoWait() {
        RelativeTimeout rel = RelativeTimeout.TIMEOUT_NOWAIT;
        assertThat(rel.isDefault(), is(equalTo(Boolean.FALSE)));
        assertThat(rel.isNoWait(), is(equalTo(Boolean.TRUE)));
        assertThat(rel.isNoTimeout(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void testNoTimeout() {
        RelativeTimeout rel = RelativeTimeout.TIMEOUT_NOTIMEOUT;
        assertThat(rel.isDefault(), is(equalTo(Boolean.FALSE)));
        assertThat(rel.isNoWait(), is(equalTo(Boolean.FALSE)));
        assertThat(rel.isNoTimeout(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void testEquality() {
        RelativeTimeout rel1 = new RelativeTimeout(1000);
        RelativeTimeout rel2 = new RelativeTimeout(1000);

        assertThat(rel1, is(equalTo(rel2)));
    }
}
