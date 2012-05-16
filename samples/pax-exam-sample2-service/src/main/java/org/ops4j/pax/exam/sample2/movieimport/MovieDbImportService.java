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
package org.ops4j.pax.exam.sample2.movieimport;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.ops4j.pax.exam.sample2.model.Actor;
import org.ops4j.pax.exam.sample2.model.Director;
import org.ops4j.pax.exam.sample2.model.Movie;
import org.ops4j.pax.exam.sample2.model.Person;
import org.ops4j.pax.exam.sample2.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class MovieDbImportService {

    private static final Logger logger = LoggerFactory.getLogger(MovieDbImportService.class);
    private MovieDbJsonMapper movieDbJsonMapper = new MovieDbJsonMapper();

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private MovieDbApiClient client;

    @Inject
    private MovieDbLocalStorage localStorage;

    public Map<Integer, String> importMovies(Map<Integer, Integer> ranges) {
        final Map<Integer,String> movies=new LinkedHashMap<Integer, String>();
        for (Map.Entry<Integer, Integer> entry : ranges.entrySet()) {
            for (int id = entry.getKey(); id <= entry.getValue(); id++) {
                String result = importMovieFailsafe(id);
                movies.put(id, result);
            }
        }
        return movies;
    }

    private String importMovieFailsafe(Integer id) {
        try {
            Movie movie = doImportMovie(id);
            return movie.getTitle();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public Movie importMovie(Integer movieId) {
        return doImportMovie(movieId);
    }

    private Movie doImportMovie(Integer movieId) {
        logger.info("Importing movie " + movieId);

        Movie movie = new Movie();
        movie.setId( movieId );

        Map<String, ?> data = loadMovieData(movieId);
        if (data.containsKey("not_found")) throw new RuntimeException("Data for Movie "+movieId+" not found.");
        movieDbJsonMapper.mapToMovie(data, movie);
        relatePersonsToMovie(movie, data);
        em.persist( movie );
        return movie;
    }

    private Map<String, ?> loadMovieData(Integer id) {
        String movieId = Integer.toString(id);
        if (localStorage.hasMovie(movieId)) {
            return localStorage.loadMovie(movieId);
        }

        Map<String, ?> data = client.getMovie(movieId);
        localStorage.storeMovie(movieId, data);
        return data;
    }

    private void relatePersonsToMovie(Movie movie, Map<String, ?> data) {
        @SuppressWarnings("unchecked") Collection<Map<String, ?>> cast = (Collection<Map<String, ?>>) data.get("cast");
        for (Map<String, ?> entry : cast) {
            Integer id = (Integer) entry.get("id");
            String jobName = (String) entry.get("job");
            if ("Actor".equals( jobName ))
            {
                Actor actor = new Actor();
                actor.setId(id);
                doImportPerson(actor);
                Role role = new Role();
                role.setActor( actor );
                role.setMovie( movie );
                role.setName( (String) entry.get("character"));
                em.persist( role );
                movie.getRoles().add( role );
            }
            else if ("Director".equals( jobName ))
            {
                Director director = new Director();
                director.setId(id);
                director.getMovies().add(movie);
                doImportPerson(director);
                movie.setDirector( director );
            }
            else
            {
                if (logger.isInfoEnabled()) logger.info("Could not add person with job "+jobName+" "+entry);
                continue;                
            }
        }
    }
    
    private void doImportPerson(Person person) {
        String personId = Integer.toString(person.getId());
        logger.info("Importing person " + personId);
        Map<String, ?> data = loadPersonData(personId);
        if (data.containsKey("not_found")) throw new RuntimeException("Data for Person "+personId+" not found.");
        movieDbJsonMapper.mapToPerson(data, person);
        Person persistentPerson = em.find(Person.class, person.getId());
        if (persistentPerson == null)
        {
            em.persist( person );
        }
    }

    

    private Map<String, ?> loadPersonData(String personId) {
        if (localStorage.hasPerson(personId)) {
            return localStorage.loadPerson(personId);
        }
        Map<String, ?> data = client.getPerson(personId);
        localStorage.storePerson(personId, data);
        return localStorage.loadPerson(personId);
    }
}
