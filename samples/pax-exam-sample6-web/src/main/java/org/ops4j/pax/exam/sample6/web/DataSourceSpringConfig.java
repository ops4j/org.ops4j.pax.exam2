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

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.ops4j.pax.exam.sample6.web.DataSourceSpringConfig.EmbeddedDataSourceSpringConfig;
import org.ops4j.pax.exam.sample6.web.DataSourceSpringConfig.JndiDataSourceSpringConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import( { JndiDataSourceSpringConfig.class, EmbeddedDataSourceSpringConfig.class } )
public class DataSourceSpringConfig
{

    @Configuration
    @Profile( "web" )
    public static class JndiDataSourceSpringConfig
    {

        @Resource( mappedName = "jdbc/library" )
        private DataSource dataSource;

        @Bean
        public DataSource dataSource()
        {
            return dataSource;
        }
    }

    @Configuration
    @Profile( "test" )
    public static class EmbeddedDataSourceSpringConfig
    {

        @Bean
        public DataSource dataSource()
        {
            EmbeddedDataSource dataSource = new EmbeddedDataSource();
            dataSource.setDatabaseName( "memory:library" );
            dataSource.setCreateDatabase( "create" );
            return dataSource;
        }
    }
}
