/*
 * Copyright (C) 2010 Okidokiteam
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
package com.okidokiteam.exxam.test;

import org.osgi.framework.BundleContext;

import static org.easymock.EasyMock.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: tonit
 * Date: Aug 5, 2010
 * Time: 3:11:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class Probe
{

    public void withoutBC()
    {
        System.out.println( "++++ PEAK ++++" );
    }

    public void withBC( BundleContext ctx )
    {
        System.out.println( "++++ PEAK ++++" );
        // assertThat( ctx, is( notNull() ) );
        assertTrue( ctx != null );
    }
}
