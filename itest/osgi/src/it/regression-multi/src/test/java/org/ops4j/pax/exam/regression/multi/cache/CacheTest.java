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
package org.ops4j.pax.exam.regression.multi.cache;
import static  org.ops4j.pax.exam.regression.multi.RegressionConfiguration.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assume.*;
import static org.junit.Assert.*;
import java.io.File;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class CacheTest
{

    private static File workDir;
    
    static 
    {
        workDir = new File(System.getProperty( "java.io.tmpdir" ), "pax-exam-cache-test");
        workDir.mkdir();        
    }
    
    public static File getWorkDir()
    {
        return workDir;
    }

    @Test
    public void cleanCachesFalse() 
    {
        assumeThat(isFelix(), is(true));

        JUnitCore junit = new JUnitCore();
        Result result = junit.run( CacheTestCleanFalse.class );
        assertThat(result.getFailureCount(), is(equalTo(0)));
        
        File bundlesDir = new File(workDir, "bundles");
        assertThat(bundlesDir.exists(), is(true));
    }

    @Test
    public void cleanCachesTrue() 
    {
        assumeThat(isFelix(), is(true));

        JUnitCore junit = new JUnitCore();
        Result result = junit.run( CacheTestCleanTrue.class );
        assertThat(result.getFailureCount(), is(equalTo(0)));
        
        File bundlesDir = new File(workDir, "bundles");
        assertThat(bundlesDir.exists(), is(false));
    }

    @Test
    public void keepCaches() 
    {
        assumeThat(isFelix(), is(true));

        JUnitCore junit = new JUnitCore();
        Result result = junit.run( CacheTestKeep.class );
        assertThat(result.getFailureCount(), is(equalTo(0)));
        
        File bundlesDir = new File(workDir, "bundles");
        assertThat(bundlesDir.exists(), is(true));
    }
}
