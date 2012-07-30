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
package org.ops4j.pax.exam.sample6.web;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.ejb.HibernatePersistence;
import org.ops4j.pax.exam.sample6.service.LibraryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement( proxyTargetClass = true )
@Import( DataSourceSpringConfig.class )
public class ServiceSpringConfig
{

    @Inject
    private DataSource dataSource;

    @Bean
    public PlatformTransactionManager transactionManager()
    {
        return new JpaTransactionManager( entityManagerFactory() );
    }

    @Bean
    public EntityManagerFactory entityManagerFactory()
    {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource( dataSource );
        bean.setPersistenceProvider( new HibernatePersistence() );
        bean.setPersistenceXmlLocation( "classpath:META-INF/persistence.xml" );
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean
    public LibraryService libraryService()
    {
        return new LibraryService();
    }
}
