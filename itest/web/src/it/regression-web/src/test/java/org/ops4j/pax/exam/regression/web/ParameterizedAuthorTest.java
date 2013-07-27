/*
 * Copyright 2013 Harald Wellmann
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

package org.ops4j.pax.exam.regression.web;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.ops4j.pax.exam.junit.PaxExamParameterized;
import org.ops4j.pax.exam.sample4.model.Book;
import org.ops4j.pax.exam.sample4.service.LibraryService;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

@RunWith(PaxExamParameterized.class)
@ExamReactorStrategy(PerSuite.class)
public class ParameterizedAuthorTest {

    @Inject
    private LibraryService service;
    
    private String authorLastName;
    private String bookTitle;
    
    
    public ParameterizedAuthorTest(String authorLastName, String bookTitle) {
        this.authorLastName = authorLastName;
        this.bookTitle = bookTitle;            
    }
    
    
    @Parameters
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
            {"Mann", "Buddenbrooks"},
            {"Steinbeck", "East of Eden"}
        });
    }

    @Before
    public void setUp() {
        service.fillLibrary();
    }

    @Test
    public void byAuthor() {
        List<Book> books = service.findBooksByAuthor(authorLastName);
        assertEquals(1, books.size());

        Book book = books.get(0);
        assertEquals(bookTitle, book.getTitle());
    }
}
