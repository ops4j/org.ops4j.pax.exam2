/*
 * Copyright 2009 Toni Menzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.junit.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.runtime.PaxExamRuntime;
import org.ops4j.pax.exam.spi.container.TestContainerFactory;

/**
 * @author Toni Menzel
 * @since Jan 26, 2010
 */
public class ExecutionPolicyOption implements Option
{

    private TestContainerFactory m_select;
    private ReUsePolicy m_reUsePolicy;

    public ExecutionPolicyOption()
    {
        m_reUsePolicy = ReUsePolicy.NEVER; // pax exam 1.x compatibility
        m_select = PaxExamRuntime.getTestContainerFactory();
    }

    public ExecutionPolicyOption testContainer( Class<? extends TestContainerFactory> factory )
    {
        try
        {
            m_select = factory.newInstance();
        } catch( InstantiationException e )
        {
            e.printStackTrace();
        } catch( IllegalAccessException e )
        {
            e.printStackTrace();
        }
        return this;
    }

    public ExecutionPolicyOption testContainer( TestContainerFactory factory )
    {
        m_select = factory;
        return this;
    }

    public TestContainerFactory getTestContainer()
    {
        return m_select;
    }

    public ReUsePolicy reuseContainer()
    {
        return m_reUsePolicy;
    }

    public ExecutionPolicyOption reuseContainer( ReUsePolicy policy )
    {
        m_reUsePolicy = policy;
        return this;
    }
}
