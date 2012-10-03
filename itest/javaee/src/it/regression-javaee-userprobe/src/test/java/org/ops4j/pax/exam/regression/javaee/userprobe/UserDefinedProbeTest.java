/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.ops4j.pax.exam.regression.javaee.userprobe;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.warProbe;
import static org.ops4j.pax.exam.spi.Probes.builder;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.sample1.model.Book;
import org.ops4j.pax.exam.sample1.service.LibraryService;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.ops4j.pax.exam.util.Transactional;

@RunWith( PaxExam.class )
@ExamReactorStrategy( PerSuite.class )
public class UserDefinedProbeTest
{

    @Inject
    private LibraryService service;

    @ProbeBuilder
    public TestProbeBuilder probe( TestProbeBuilder defaultProbe )
    {
        return builder( warProbe().
            library( "target/test-classes" ).
            library(
                maven( "org.ops4j.pax.exam.samples", "pax-exam-sample1-service", "3.0.0-SNAPSHOT" ) ).
            library(
                maven( "org.ops4j.pax.exam.samples", "pax-exam-sample1-model", "3.0.0-SNAPSHOT" ) ).
            library( maven( "org.ops4j.pax.exam", "pax-exam-servlet-bridge", "3.0.0-SNAPSHOT" ) ).
            library( maven( "org.ops4j.pax.exam", "pax-exam-cdi", "3.0.0-SNAPSHOT" ) ).
            library( maven( "org.ops4j.pax.exam", "pax-exam", "3.0.0-SNAPSHOT" ) ).
            library( maven( "org.ops4j.base", "ops4j-base-spi", "1.4.0" ) ).
            library( maven( "junit", "junit", "4.9" ) ) );
    }

    @Test
    public void byAuthor()
    {
        List<Book> books = service.findBooksByAuthor( "Mann" );
        assertEquals( 1, books.size() );

        Book book = books.get( 0 );
        assertEquals( "Buddenbrooks", book.getTitle() );
    }

    @Test
    @Transactional
    public void numAuthors()
    {
        assertEquals( 2, service.getNumAuthors() );
        service.createAuthor( "Theodor", "Storm" );
        assertEquals( 3, service.getNumAuthors() );
    }

    @Test
    public void numAuthorsAfterTransaction()
    {
        assertEquals( 2, service.getNumAuthors() );
    }
}
