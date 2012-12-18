package org.ops4j.pax.exam.regression.multi;

import org.junit.Test;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.*;

public class OptionTest {

    @Test
    public void compositeEquality() {
        Option composite1 = composite(RegressionConfiguration.regressionDefaults());
        Option composite2 = composite(RegressionConfiguration.regressionDefaults());
        assertThat(composite1, is(equalTo(composite2)));
    }
}
