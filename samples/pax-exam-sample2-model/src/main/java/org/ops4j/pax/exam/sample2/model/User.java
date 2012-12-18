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
package org.ops4j.pax.exam.sample2.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * User entity. Note that {@code USER} is a reserved word in Derby SQL, so we override the default
 * table name.
 * 
 * @author Harald Wellmann
 * 
 */
@Entity
@Table(name = "usr")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String name;
    private String password;

    @ManyToMany
    private List<User> friends = new ArrayList<User>();

    @ManyToMany
    private List<Rating> ratings = new ArrayList<Rating>();

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the friends
     */
    public List<User> getFriends() {
        return friends;
    }

    /**
     * @param friends
     *            the friends to set
     */
    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    /**
     * @return the ratings
     */
    public List<Rating> getRatings() {
        return ratings;
    }

    /**
     * @param ratings
     *            the ratings to set
     */
    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }
}
