package com.okidokiteam.exxam.regression.paxrunner.porcelain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.LibraryOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

/**
 *
 */
@RunWith( JUnit4TestRunner.class )
@ExamReactorStrategy( AllConfinedStagedReactorFactory.class )
public class MutliFrameworkTest {

    private static Logger LOG = LoggerFactory.getLogger( MutliFrameworkTest.class );

    @Configuration()
    public Option[] config()
    {
        return options(
            junitBundles(),
            equinox(),
            felix(),
            logProfile()
        );
    }

    @Test
    public void fwTest(  )
    {
        

    }
}

