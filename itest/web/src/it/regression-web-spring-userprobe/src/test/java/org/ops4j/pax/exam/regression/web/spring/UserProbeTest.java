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

package org.ops4j.pax.exam.regression.web.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.warProbe;
import static org.ops4j.pax.exam.Info.getOps4jBaseVersion;
import static org.ops4j.pax.exam.Info.getPaxExamVersion;
import static org.ops4j.pax.exam.spi.Probes.builder;

import java.util.List;

import javax.inject.Inject;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.sample6.model.Book;
import org.ops4j.pax.exam.sample6.service.LibraryService;
import org.ops4j.pax.exam.spi.container.ContainerConstants;

@RunWith(PaxExam.class)
public class UserProbeTest {

    @Inject
    private LibraryService service;

    @ProbeBuilder
    public TestProbeBuilder probe(TestProbeBuilder defaultProbe) {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        return builder(warProbe()
            .library("target/test-classes")
            .overlay(
                maven("org.ops4j.pax.exam.samples", "pax-exam-sample6-web", getPaxExamVersion())
                    .type("war"))
            .library(maven("org.ops4j.pax.exam", "pax-exam-servlet-bridge", getPaxExamVersion()))
            .library(maven("org.ops4j.pax.exam", "pax-exam-spring", getPaxExamVersion()))
            .library(maven("org.ops4j.pax.exam", "pax-exam", getPaxExamVersion()))
            .library(maven("org.ops4j.base", "ops4j-base-spi", getOps4jBaseVersion()))
            .library(maven("junit", "junit", "4.9")));
    }

    @Before
    public void setUp() {
        service.fillLibrary();
    }

    @Test
    public void byAuthor() {
        List<Book> books = service.findBooksByAuthor("Mann");
        assertEquals(1, books.size());

        Book book = books.get(0);
        assertEquals("Buddenbrooks", book.getTitle());
    }

    @Test
    public void numAuthors() {
        assertEquals(2, service.getNumAuthors());
        service.createAuthor("Theodor", "Storm");
        assertEquals(3, service.getNumAuthors());
    }

    /**
     * Gets books.html which contains the list of books in a library.
     * <p>
     * The html content is based on the defined books.jsp in the sample web module.
     */
    @Test
    public void testGetBooksHttpContent() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:9080" + ContainerConstants.EXAM_CONTEXT_ROOT + "/books.html");
        CloseableHttpResponse response = httpclient.execute(httpGet);
        try {
            // verify http status code
            assertTrue("Status code must be 200 - OK", 
                    response.getStatusLine().getStatusCode() == 200);

            // verify html content
            String content = EntityUtils.toString(response.getEntity());
            assertNotNull("Content must be provided", content);
            assertTrue("Content must contain: Steinbeck", content.matches("[\\s\\S]+<td>Steinbeck</td>[\\s\\S]+"));
        } finally {
            response.close();
        }
        httpclient.close();
    }
}
