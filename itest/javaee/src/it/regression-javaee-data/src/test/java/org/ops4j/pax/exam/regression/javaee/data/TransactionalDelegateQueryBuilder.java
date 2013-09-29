/*
 * Copyright 2013 Harald Wellmann
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
package org.ops4j.pax.exam.regression.javaee.data;

import javax.enterprise.inject.Specializes;

import org.apache.deltaspike.data.impl.builder.DelegateQueryBuilder;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocation;
import org.apache.deltaspike.jpa.api.transaction.Transactional;


@QueryInvocation(MethodType.DELEGATE)
@Specializes
@Transactional
public class TransactionalDelegateQueryBuilder extends DelegateQueryBuilder {
    
    @Override
    public Object execute(CdiQueryInvocationContext context) {
        return super.execute(context);
    }
}
