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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ops4j.pax.exam.sample2.model.Movie;
import org.ops4j.pax.exam.sample2.model.User;
import org.ops4j.pax.exam.sample2.movieimport.MovieDbImportService;

/**
 * @author mh
 * @since 04.03.11
 */
public class DatabasePopulator {

    @Inject
    private UserService userService;

    @Inject
    private MovieService movieService;

    @Inject
    private MovieDbImportService importService;

    public List<Movie> populateDatabase() {

        User micha = userService.register("micha", "Micha", "password");
        userService.register("ollie", "Olliver", "password");
        userService.addFriend(micha, "ollie");

        List<Integer> ids = asList(19995, 194, 600, 601, 602, 603, 604, 605, 606, 607, 608, 609,
            13, 20526, 11, 1893, 1892, 1894, 168, 193, 200, 157, 152, 201, 154, 12155, 58, 285,
            118, 22, 392, 5255, 568, 9800, 497, 101, 120, 121, 122);
        List<Movie> result = new ArrayList<Movie>(ids.size());
        for (Integer id : ids) {
            result.add(importService.importMovie(id));
        }

        final Movie movie = movieService.findById(603);
        userService.rate(micha, movie, 5, "Best of the series");
        return result;
    }
}
