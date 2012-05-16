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

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class MovieDbApiClient {

    private final String baseUrl = "http://api.themoviedb.org/";
    private final String apiKey;
    protected final ObjectMapper mapper;

    public MovieDbApiClient() {
        this("926d2a79e82920b62f03b1cb57e532e6");
    }
    
    
    public MovieDbApiClient(String apiKey) {
        this.apiKey = apiKey;
        mapper = new ObjectMapper();
    }

    public Map<String, ?> getMovie(String id) {
        return loadJsonData(id, buildMovieUrl(id));
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> loadJsonData(String id, String url) {
        try {
            List<?> value = mapper.readValue(new URL(url), List.class);
            if (value.isEmpty() || value.get(0).equals("Nothing found.")) return Collections.singletonMap("not_found",System.currentTimeMillis());
            return (Map<String, ?>) value.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data from " + url, e);
        }
    }

    private String buildMovieUrl(String movieId) {
        return String.format("%s2.1/Movie.getInfo/en/json/%s/%s", baseUrl, apiKey, movieId);
    }

    public Map<String, ?> getPerson(String id) {
        return loadJsonData(id, buildPersonUrl(id));
    }

    private String buildPersonUrl(String personId) {
        return String.format("%s2.1/Person.getInfo/en/json/%s/%s", baseUrl, apiKey, personId);
    }
}
