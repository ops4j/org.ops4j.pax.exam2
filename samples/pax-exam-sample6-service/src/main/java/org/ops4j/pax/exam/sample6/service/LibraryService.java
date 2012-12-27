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

package org.ops4j.pax.exam.sample6.service;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.ops4j.pax.exam.sample6.model.Author;
import org.ops4j.pax.exam.sample6.model.Book;

/**
 * NOTE: By the EJB 3.1 specification, all public business methods are transactional by default,
 * with a REQUIRED transaction type, so the <code>@TransactionAttribute</code> annotations on the
 * methods of this class are redundant.
 * <p>
 * However, by adding these annotations, we can use this class both in a Java EE 6 and in a Spring
 * 3.0.x container. Of course, the preferred solution would be if Spring supported the
 * <code>@Stateless</code> and other EJB annotations <a
 * href="https://jira.springframework.org/browse/SPR-3858">out-of-the-box</a>.
 * 
 * @author hwellmann
 * 
 */
@Stateless
public class LibraryService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute
    public void fillLibrary() {
        if (getNumBooks() != 0) {
            return;
        }
        Author mann = createAuthor("Thomas", "Mann");
        Author steinbeck = createAuthor("John", "Steinbeck");

        createBook("Buddenbrooks", mann);
        createBook("East of Eden", steinbeck);
    }

    @TransactionAttribute
    public List<Book> findBooks() {
        String jpql = "select b from Book b";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        List<Book> books = query.getResultList();
        return books;
    }

    @TransactionAttribute
    public List<Book> findBooksByAuthor(String lastName) {
        String jpql = "select b from Book b where b.author.lastName = :lastName";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("lastName", lastName);
        List<Book> books = query.getResultList();
        return books;
    }

    @TransactionAttribute
    public List<Book> findBooksByTitle(String title) {
        String jpql = "select b from Book b where b.title = :title";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("title", title);
        List<Book> books = query.getResultList();
        return books;
    }

    @TransactionAttribute
    public Author createAuthor(String firstName, String lastName) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        em.persist(author);
        return author;
    }

    @TransactionAttribute
    public Book createBook(String title, Author author) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        author.getBooks().add(book);
        em.persist(book);
        return book;
    }

    @TransactionAttribute
    public long getNumBooks() {
        String jpql = "select count(b) from Book b";
        Long numBooks = (Long) em.createQuery(jpql).getSingleResult();
        return numBooks;
    }

    @TransactionAttribute
    public long getNumAuthors() {
        String jpql = "select count(a) from Author a";
        Long numAuthors = (Long) em.createQuery(jpql).getSingleResult();
        return numAuthors;
    }
}
