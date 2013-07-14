package org.ops4j.pax.exam.regression.javaee;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

@Ignore
public class CalculatorInvokerTest {
    
    @Test
    public void invokeParameterizedTest() {
        JUnitCore junit = new JUnitCore();
        Result result = junit.run(CalculatorTest.class);
        assertThat(result.getRunCount(), is(2));
        assertThat(result.getFailureCount(), is(0));
    }
}
