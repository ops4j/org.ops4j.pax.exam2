/*
 * Copyright (C) 2011 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.regression.multi.wrap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.regression.pde.HelloService;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

/**
 * This test shows
 * <ul>
 * <li>how to use the {@code wrappedBundle()} option</li>
 * <li>how to set the version of exported packages with {@code wrappedBundle()}.</li>
 * <li>how to use both Mockito and JUnit and avoid a conflict with the Hamcrest library which is embedded in each of the
 * two by default.</li>
 * <li>how to suppress classloader issues caused by CGLIB proxies in Mockito and implicit 
 * boot delegation in Felix</li>
 * 
 * </ul>
 * See PAXEXAM-274.
 * 
 * @author Harald Wellmann
 * 
 */
@RunWith( JUnit4TestRunner.class )
@ExamReactorStrategy( PerMethod.class )
public class MockitoTest
{

    @Configuration
    public Option[] config1()
    {
        return options(
            
            regressionDefaults(),

            // A simple test bundle 
            mavenBundle( "org.ops4j.pax.exam", "regression-pde-bundle", Info.getPaxExamVersion() ),
            
            // Mockito with Hamcrest and Objenesis embedded
            mavenBundle( "org.mockito", "mockito-all", "1.8.5" ),

            // JUnit without Hamcrest
            wrappedBundle( mavenBundle( "junit", "junit-dep", "4.9" ) ).exports( "*;version=4.9" ),

            
            systemProperty( "pax.exam.invoker" ).value( "junit" ),            
            mavenBundle("org.ops4j.pax.exam", "pax-exam-invoker-junit", Info.getPaxExamVersion()),
            /*
             * Felix has implicit boot delegation enabled by default, which causes the following 
             * exception: 
             * 
             * loader constraint violation in interface itable initialization: 
             * when resolving method "org.ops4j.pax.exam.regression.pde.HelloService$$EnhancerByMockitoWithCGLIB$$451e2809.newInstance(Lorg/mockito/cglib/proxy/Callback;)Ljava/lang/Object;" 
             * the class loader (instance of org/mockito/internal/creation/jmock/SearchingClassLoader) of the current class, 
             * org/ops4j/pax/exam/regression/pde/HelloService$$EnhancerByMockitoWithCGLIB$$451e2809, 
             * and the class loader (instance of org/apache/felix/framework/ModuleImpl$ModuleClassLoaderJava5) 
             * for interface org/mockito/cglib/proxy/Factory have different Class objects for the type 
             * org/mockito/cglib/proxy/Callback used in the signature
             * 
             * The bundle classloader of regression-pde-bundle loads org.mockito.cglib.proxy.Factory
             * via boot delegation from the app class loader, which conflicts with the class loaded
             * by the Mockito bundle class loader.
             * 
             * See ModuleImpl.doImplicitBootDelegation() in Felix. 
             */
            frameworkProperty( "felix.bootdelegation.implicit" ).value( "false" )

        );

    }

    @Configuration
    public Option[] config2()
    {
        return options(

            regressionDefaults(),

            // A simple test bundle 
            mavenBundle( "org.ops4j.pax.exam", "regression-pde-bundle", Info.getPaxExamVersion() ),

            // Mockito without Hamcrest and Objenesis
            mavenBundle( "org.mockito", "mockito-core", "1.8.5" ),

            // Hamcrest with a version matching the range expected by Mockito
            mavenBundle( "org.hamcrest", "com.springsource.org.hamcrest.core", "1.1.0" ),

            // Objenesis with a version matching the range expected by Mockito
            wrappedBundle( mavenBundle( "org.objenesis", "objenesis", "1.2" ) ).exports( "*;version=1.2" ),

            // The default JUnit bundle also exports Hamcrest, but with an (incorrect) version of
            // 4.9 which does not match the Mockito import.
            junitBundles(),

            // see config1()
            frameworkProperty( "felix.bootdelegation.implicit" ).value( "false" ) );
    }

    
    /**
     * This test does not produce the boot delegation issue, since the mocked
     * interface is loaded via the system class loader.
     */
    @Test
    @SuppressWarnings( "unchecked" )
    public void createMock()
    {
        List<String> list = mock( List.class );
        when( list.size() ).thenReturn( 0 );
        int size = list.size();
        assertEquals( 0, size );
    }

    /**
     * Here we mock an interface from our test bundle.
     * Set felix.bootdelegation.implicit = true to fail this test.
     */
    @Test
    public void bootDelegation()
    {
        HelloService service = mock( HelloService.class );
        service.getMessage();
    }
}
