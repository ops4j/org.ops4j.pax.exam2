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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a persistence context just for testing.
 * 
 * @author Harald Wellmann
 * 
 */
public class PersistenceContextProvider {

    private static Logger log = LoggerFactory.getLogger(PersistenceContextProvider.class);

    /**
     * Use a producer method so that CDI will find this EntityManager.
     * 
     * @param emf
     *            entity manager factory
     * @return entity manager
     */
    @Produces
    @RequestScoped
    public EntityManager getEntityManager(EntityManagerFactory emf) {
        log.debug("producing EntityManager");
        return emf.createEntityManager();
    }

    @Produces
    @ApplicationScoped
    public EntityManagerFactory getEntityManagerFactory() {
        log.debug("producing EntityManagerFactory");
        Map<String, String> props = new HashMap<String, String>();
        props.put("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        props.put("javax.persistence.jdbc.url", "jdbc:derby:target/libraryDb;create=true");
        props.put("hibernate.hbm2ddl.auto", "create");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("library", props);
        return emf;
    }

    public void closeEntityManager(@Disposes EntityManager em) {
        log.debug("disposing EntityManager");
        em.close();
    }

    public void closeEntityManager(@Disposes EntityManagerFactory emf) {
        log.debug("disposing EntityManagerFactory");
        emf.close();
    }

}
