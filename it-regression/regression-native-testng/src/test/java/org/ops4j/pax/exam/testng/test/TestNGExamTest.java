package org.ops4j.pax.exam.testng.test;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.testng.Configuration;
import org.testng.annotations.Test;

public class TestNGExamTest
{
    @Configuration
    public Option[] config()
    {
        return options( mavenBundle( "org.testng", "testng", "6.2" ) );
    }

    @Test
    public void helloTestNG()
    {
        System.out.println( "Hello TestNG!" );
    }

    @Test
    public void helloPaxExam()
    {
        System.out.println( "Hello Pax Exam!" );
    }
}
