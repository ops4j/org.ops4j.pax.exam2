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
package org.ops4j.pax.exam.regression.web.spring;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.ops4j.pax.exam.sample6.service.LibraryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class TestSpringConfig
{
    @Bean
    public DataSource dataSource()
    {
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName( "memory:library" );
        dataSource.setCreateDatabase( "create" );
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager()
    {
        return new JpaTransactionManager( entityManagerFactory() );
    }

    @Bean
    public EntityManagerFactory entityManagerFactory()
    {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource( dataSource() );
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
