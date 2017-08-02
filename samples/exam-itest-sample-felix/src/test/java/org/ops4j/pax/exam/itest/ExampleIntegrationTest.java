package org.ops4j.pax.exam.itest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class ExampleIntegrationTest {

    @Configuration
    public Option[] config() {
        return options(
                junitBundles()
        );
    }

    @Test
    public void testExample() {
        // TODO implement a test here
        System.out.println("Called!");
    }

    @Test
    public void testExample2() {
        // TODO implement a test here
        System.out.println("Called2!");
    }

}
