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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.exam.sample2.model.Movie;
import org.ops4j.pax.exam.sample2.model.Person;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MovieDbJsonMapper {

    public void mapToMovie(Map<String, ?> data, Movie movie) {
        try {
            movie.setTitle((String) data.get("name"));
            movie.setLanguage((String) data.get("language"));
            movie.setImdbId((String) data.get("imdb_id"));
            movie.setTagline((String) data.get("tagline"));
            movie.setDescription(limit((String) data.get("overview"), 500));
            // movie.setReleaseDate(toDate(data, "released", "yyyy-MM-dd"));
            movie.setRuntime((Integer) data.get("runtime"));
            movie.setHomepage((String) data.get("homepage"));
            Object trailer = data.get("trailer");
            if (trailer != null) {
                String trailerUrl = (String) trailer;
                movie.setTrailer(trailerUrl);
                String youtubeId = getYoutubeId(trailerUrl);
                if (youtubeId != null) {
                    movie.setYoutubeId(youtubeId);
                }
            }
            // movie.setGenre(extractFirst(data, "genres", "name"));
            // movie.setStudio(extractFirst(data,"studios", "name"));
            // movie.setVersion((Integer)data.get("version"));
            // movie.setLastModified(toDate(data,"last_modified_at","yyyy-MM-dd HH:mm:ss"));
            movie.setImageUrl(selectImageUrl((List<Map>) data.get("posters"), "poster", "mid"));
        }
        catch (Exception e) {
            throw new MovieDbException("Failed to map json for movie", e);
        }
    }

    public String getYoutubeId(String trailerUrl) {
        if (trailerUrl == null || !trailerUrl.contains("youtu"))
            return null;
        String[] parts = trailerUrl.split("[=/]");
        int numberOfParts = parts.length;
        return numberOfParts > 0 ? parts[numberOfParts - 1] : null;
    }

    private String selectImageUrl(List<Map> data, final String type, final String size) {
        if (data == null)
            return null;
        for (Map entry : data) {
            Map image = (Map) entry.get("image");
            if (image.get("type").equals(type) && image.get("size").equals(size))
                return (String) image.get("url");
        }
        return null;
    }

    @SuppressWarnings("unused")
    private String extractFirst(Map data, String field, String property) {
        List<Map> inner = (List<Map>) data.get(field);
        if (inner == null || inner.isEmpty())
            return null;
        return (String) inner.get(0).get(property);
    }

    private Date toDate(Map data, String field, final String pattern) throws ParseException {
        try {
            String dateString = (String) data.get(field);
            if (dateString == null || dateString.isEmpty())
                return null;
            return new SimpleDateFormat(pattern).parse(dateString);
        }
        catch (Exception e) {
            return null;
        }
    }

    public void mapToPerson(Map<String, ?> data, Person person) {
        try {
            person.setName((String) data.get("name"));
            person.setBirthday(toDate(data, "birthday", "yyyy-MM-dd"));
            String birthplace = (String) data.get("birthplace");
            if (birthplace != null) {
                person.setBirthplace(birthplace);
            }
            String biography = (String) data.get("biography");
            person.setBiography(limit(biography, 500));
            // person.setVersion((Integer) data.get("version"));
            String imageUrl = selectImageUrl((List<Map>) data.get("profile"), "profile", "profile");
            if (imageUrl != null) {
                person.setProfileImageUrl(imageUrl);
            }
            person.setLastModified(toDate(data, "last_modified_at", "yyyy-MM-dd HH:mm:ss"));
        }
        catch (Exception e) {
            throw new MovieDbException("Failed to map json for person", e);
        }
    }

    private String limit(String text, int limit) {
        if (text == null || text.length() < limit)
            return text;
        return text.substring(0, limit);
    }

    // public Roles mapToRole(String roleString) {
    // if (roleString.equals("Actor")) {
    // return Roles.ACTS_IN;
    // }
    // if (roleString.equals("Director")) {
    // return Roles.DIRECTED;
    // }
    // return null;
    // }
}
