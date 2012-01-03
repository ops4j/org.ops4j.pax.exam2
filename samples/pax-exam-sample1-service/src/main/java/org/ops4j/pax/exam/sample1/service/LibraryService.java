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

package org.ops4j.pax.exam.sample1.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.ops4j.pax.exam.sample1.model.Author;
import org.ops4j.pax.exam.sample1.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Harald Wellmann
 * 
 */
@Stateless
public class LibraryService {
    
    private static Logger log = LoggerFactory.getLogger( LibraryService.class );
    
    @PersistenceContext
    private EntityManager em;
    
    public List<Book> findBooks()
    {
        log.info("finding books");
        String jpql = "select b from Book b";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        List<Book> books = query.getResultList();
        return books;       
    }
    
    public List<Book> findBooksByAuthor(String lastName)
    {
        String jpql = "select b from Book b where b.author.lastName = :lastName";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("lastName", lastName);
        List<Book> books = query.getResultList();
        return books;       
    }
    
    public List<Book> findBooksByTitle(String title)
    {
        String jpql = "select b from Book b where b.title = :title";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        query.setParameter("title", title);
        List<Book> books = query.getResultList();
        return books;       
    }
    
    public Author createAuthor(String firstName, String lastName)
    {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        em.persist(author);
        return author;
    }
    
    public Book createBook(String title, Author author)
    {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        author.getBooks().add(book);
        em.persist(book);
        return book;
    }
    
    public long getNumBooks()
    {
        String jpql = "select count(b) from Book b";
        Long numBooks = (Long) em.createQuery(jpql).getSingleResult();
        return numBooks;
    }

    public long getNumAuthors()
    {
        String jpql = "select count(a) from Author a";
        Long numAuthors = (Long) em.createQuery(jpql).getSingleResult();
        return numAuthors;
    }
}
