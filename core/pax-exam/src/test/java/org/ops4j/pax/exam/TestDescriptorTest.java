package org.ops4j.pax.exam;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class TestDescriptorTest {

    @Test
    public void testParse() {
        TestDescription descriptor = new TestDescription("className");
        assertThat(descriptor, equalTo(TestDescription.parse(descriptor.toString())));

        descriptor = new TestDescription("className", "methodName");
        assertThat(descriptor, equalTo(TestDescription.parse(descriptor.toString())));

        descriptor = new TestDescription("className", "methodName", 3);
        assertThat(descriptor, equalTo(TestDescription.parse(descriptor.toString())));

        descriptor = new TestDescription("className", "methodName", 3, new TestFilter("All methods", Arrays.asList("1", "2", "3")));
        assertThat(descriptor, equalTo(TestDescription.parse(descriptor.toString())));
    }

}