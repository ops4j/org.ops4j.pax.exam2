/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.exam.regression.deltaspike;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.test.core.api.exclude.AlwaysActiveBean;
import org.apache.deltaspike.test.core.api.exclude.CustomExpressionBasedBean;
import org.apache.deltaspike.test.core.api.exclude.CustomExpressionBasedNoBean;
import org.apache.deltaspike.test.core.api.exclude.IntegrationTestBean;
import org.apache.deltaspike.test.core.api.exclude.IntegrationTestDbBean;
import org.apache.deltaspike.test.core.api.exclude.NoBean;
import org.apache.deltaspike.test.core.api.exclude.ProdDbBean;
import org.apache.deltaspike.test.core.api.exclude.StdBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;

/**
 * Integration tests for {@link org.apache.deltaspike.core.api.exclude.annotation.Exclude}.
 * <p>
 * The original DeltaSpike test was based on JBoss Arquillian. Here we use Pax Exam, which does not
 * require any {@code @Configuration} or {@code @Deployment}.
 * <p>
 * All we need is an exam.properties resource on the classpath setting {@code pax.exam.system = cdi}.
 */
@RunWith( PaxExam.class )
public class ExcludeIntegrationTest
{

    /**
     * check project-stage to ensure the correct config for the other tests in this class
     */
    @Test
    public void checkProjectStage()
    {
        Assert.assertEquals( ProjectStage.IntegrationTest, ProjectStageProducer.getInstance()
            .getProjectStage() );
    }

    /**
     * check if this package is included at all
     */
    @Test
    public void simpleCheckOfBeansInPackage()
    {
        AlwaysActiveBean testBean =
            BeanProvider.getContextualReference( AlwaysActiveBean.class, true );

        Assert.assertNotNull( testBean );
    }

    /**
     * bean is excluded in any case
     */
    @Test
    public void excludeWithoutCondition()
    {
        NoBean noBean = BeanProvider.getContextualReference( NoBean.class, true );

        Assert.assertNull( noBean );
    }

    /**
     * bean excluded in case of project-stage integration-test
     */
    @Test
    public void excludeInCaseOfProjectStageIntegrationTest()
    {
        StdBean stdBean = BeanProvider.getContextualReference( StdBean.class, true );

        Assert.assertNull( stdBean );
    }

    /**
     * bean included in case of project-stage integration-test
     */
    @Test
    public void includedInCaseOfProjectStageIntegrationTest()
    {
        IntegrationTestBean integrationTestBean =
            BeanProvider.getContextualReference( IntegrationTestBean.class, true );

        Assert.assertNotNull( integrationTestBean );
    }

    /**
     * beans de-/activated via expressions
     */
    @Test
    public void excludedIfExpressionMatch()
    {
        ProdDbBean prodDbBean = BeanProvider.getContextualReference( ProdDbBean.class, true );

        Assert.assertNull( prodDbBean );

        IntegrationTestDbBean integrationTestDbBean =
            BeanProvider.getContextualReference( IntegrationTestDbBean.class, true );

        Assert.assertNotNull( integrationTestDbBean );
    }

    /**
     * bean excluded based on a custom expression syntax
     */
    @Test
    public void excludedBasedOnCustomExpressionSyntax()
    {
        CustomExpressionBasedNoBean noBean =
            BeanProvider.getContextualReference( CustomExpressionBasedNoBean.class, true );

        Assert.assertNull( noBean );
    }

    /**
     * bean included based on a custom expression syntax
     */
    @Test
    public void includedBasedOnCustomExpressionSyntax()
    {
        CustomExpressionBasedBean bean =
            BeanProvider.getContextualReference( CustomExpressionBasedBean.class, true );

        Assert.assertNotNull( bean );
    }
}
