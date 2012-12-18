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
package org.ops4j.pax.exam.regression.javaee;

import static org.testng.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.ops4j.pax.exam.sample1.model.Book;
import org.ops4j.pax.exam.sample1.service.LibraryService;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(PaxExam.class)
public class TitleTest {

    @Inject
    private LibraryService service;

    @BeforeMethod
    public void setUp() {
        System.out.println("********** beforeMethod");
    }

    @AfterMethod
    public void tearDown() {
        System.out.println("********** afterMethod");
    }

    @Test
    public void byTitle() {
        System.out.println("********** byTitle");
        List<Book> books = service.findBooksByTitle("East of Eden");
        assertEquals(1, books.size());

        Book book = books.get(0);
        assertEquals("Steinbeck", book.getAuthor().getLastName());
    }
}
