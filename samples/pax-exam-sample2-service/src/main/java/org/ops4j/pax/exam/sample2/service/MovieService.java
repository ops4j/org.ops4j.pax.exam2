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
package org.ops4j.pax.exam.sample2.service;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.ops4j.pax.exam.sample2.model.Movie;

@Stateless
public class MovieService {

    @PersistenceContext
    private EntityManager em;

    public Movie findById(int id) {
        Movie movie = em.find(Movie.class, id);
        movie.getActors();
        movie.getRoles();
        movie.getRatings();
        return movie;
    }

    public List<Movie> findByTitleLike(String substring, int limit, int offset) {
        String jpql = "select m from Movie m where m.title like :substring order by m.title";
        TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
        query.setParameter("substring", "%" + substring + "%");

        List<Movie> movies = query.setFirstResult(offset).setMaxResults(limit).getResultList();
        return movies;
    }
}
