package org.ops4j.pax.exam;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class TestFilterTest {

    @Test
    public void testParse() {
        TestFilter filter = new TestFilter(null);
        assertThat(filter, equalTo(TestFilter.parse(filter.toString())));

        filter = new TestFilter("custom filter", Collections.emptyList());
        assertThat(filter, equalTo(TestFilter.parse(filter.toString())));

        filter = new TestFilter("custom filter", Arrays.asList("uniqueId1", "uniqueId2"));
        assertThat(filter, equalTo(TestFilter.parse(filter.toString())));
    }

}
