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

package org.ops4j.pax.exam.regression.cdi.library;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Provides a persistence context just for testing.
 *
 * @author hwellmann
 *
 */
@Dependent
public class PersistenceContextProvider {

    protected EntityManager em;

    /**
     * Use a producer method so that CDI will find this EntityManager.
     *
     * @return
     * @throws IOException
     */
    @Produces
    public EntityManager getEntityManager() throws IOException {
        if (em == null) {
            createEntityManager();
        }
        return em;
    }

    private void createEntityManager() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        props.put("javax.persistence.jdbc.url", "jdbc:derby:target/jeeunitDb;create=true");
        props.put("javax.persistence.jdbc.user", "jeeunit");
        props.put("javax.persistence.jdbc.password", "jeeunit");
        props.put("hibernate.hbm2ddl.auto", "create");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("library", props);
        em = emf.createEntityManager();
    }
}
