/*
 * Copyright 2011 Harald Wellmann
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

package org.ops4j.pax.exam.sample4.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.ops4j.pax.exam.sample4.model.Author;
import org.ops4j.pax.exam.sample4.model.Book;

public class LibraryService implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private EntityManager em;
    
    public void fillLibrary()
    {
        if (getNumBooks() != 0)
            return;
        
        Author mann = createAuthor("Thomas", "Mann");
        Author steinbeck = createAuthor("John", "Steinbeck");
        
        createBook("Buddenbrooks", mann);
        createBook("East of Eden", steinbeck);
    }
    
    public List<Book> findBooks()
    {
        em.getTransaction().begin();
        String jpql = "select b from Book b";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);
        List<Book> books = query.getResultList();
        em.getTransaction().commit();
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
        em.getTransaction().begin();
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        em.persist(author);
        em.flush();
        em.getTransaction().commit();
        return author;
    }
    
    public Book createBook(String title, Author author)
    {
        em.getTransaction().begin();
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        author.getBooks().add(book);
        em.persist(book);
        em.flush();
        em.getTransaction().commit();
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
