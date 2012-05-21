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

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.ops4j.pax.exam.sample2.model.Movie;
import org.ops4j.pax.exam.sample2.model.Rating;
import org.ops4j.pax.exam.sample2.model.User;
import org.ops4j.pax.exam.sample2.movieimport.MovieDbException;

@Stateless
public class UserService {
    
    @PersistenceContext
    private EntityManager em;
    
    public User findByLogin(String login)
    {
        return em.find( User.class, login );        
    }

    public User authenticate(String login, String password) {
        User user = em.find( User.class, login );
        if (user != null) {
            if (password.equals(user.getPassword())) {
                return user;
            }
        }
        return null;
    }    
    
    public User register(String login, String name, String password) {
        if (findByLogin( login ) != null)
        {
            throw new MovieDbException( "login is already taken"  );
        }
        User user = new User();
        user.setId( login );
        user.setName( name );
        user.setPassword( password );
        em.persist( user );
        return user;
    }    

    public Rating rate(User user, Movie movie, int stars, String comment) {
        Rating rating = new Rating();
        rating.setUser( user );
        rating.setMovie( movie );
        rating.setStars( stars );
        rating.setComment( comment );
        em.persist( rating );
        return rating;
    }

    public void addFriend(User user, String friendLogin)
    {
        User friend = findByLogin( friendLogin );
        if (friend != null && !friend.equals( user )) {
            user.getFriends().add( friend );
        }
    }
    
    public boolean areFriends(User currentUser, User otherUser)
    {
        return currentUser.getFriends().contains( otherUser );
    }
}
