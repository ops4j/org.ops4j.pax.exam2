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

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.ops4j.pax.exam.sample6.model.Author;

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
public class LibraryClient implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private LibraryServiceNoTx libraryService;

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void fillLibrary() {
        if (libraryService.getNumBooks() != 0) {
            return;
        }

        Author mann = libraryService.createAuthor("Thomas", "Mann");
        Author steinbeck = libraryService.createAuthor("John", "Steinbeck");

        libraryService.createBook("Buddenbrooks", mann);
        libraryService.createBook("East of Eden", steinbeck);
    }

    public long getNumBooks() {
        return libraryService.getNumBooks();
    }
}
