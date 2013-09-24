/*
 * Copyright 2010 Harald Wellmann
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

package org.ops4j.pax.exam.regression.cdi.library;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.sample1.model.Author;
import org.ops4j.pax.exam.sample1.model.Book;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class AuthorTest {

    @Inject
    private EntityManager em;

    @Before
    public void setUp() {
        if (getNumBooks() != 0)
            return;

        Author mann = new Author();
        mann.setFirstName("Thomas");
        mann.setLastName("Mann");

        Author steinbeck = new Author();
        steinbeck.setFirstName("John");
        steinbeck.setLastName("Steinbeck");

        Book buddenbrooks = new Book();
        buddenbrooks.setTitle("Buddenbrooks");
        buddenbrooks.setAuthor(mann);
        mann.getBooks().add(buddenbrooks);

        Book eden = new Book();
        eden.setTitle("East of Eden");
        eden.setAuthor(steinbeck);
        steinbeck.getBooks().add(eden);

        em.getTransaction().begin();
        em.persist(mann);
        em.persist(steinbeck);
        em.persist(buddenbrooks);
        em.persist(eden);
        em.getTransaction().commit();
    }

    @Test
    public void byAuthor() {
        String jpql = "select b from Book b where b.author.lastName = :lastName";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("lastName", "Mann");
        List<Book> books = query.getResultList();
        assertEquals(1, books.size());

        Book book = books.get(0);
        assertEquals("Buddenbrooks", book.getTitle());
    }

    public long getNumBooks() {
        String jpql = "select count(b) from Book b";
        Long numBooks = (Long) em.createQuery(jpql).getSingleResult();
        return numBooks;
    }
}
