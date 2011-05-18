/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.testforge;

import org.ops4j.pax.exam.TestContainerException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

/**
 * Simple pre-build test to be used with Pax Exam.
 *
 * Inspired by Gogos "which bundleid classname" command.
 * It makes sure that a class is (if loadable!) loaded by the same source.
 * Multiple sources for the same class is not bad per-se, but some classes (like LoggerFactory) and stuff should really be loaded from one single source.
 * Otherwise its source of trouble.
 * This is what this test checks.
 * 
 */
public class SingleClassProvider {

    private static final long LOADED_FROM_BOOTCLASSLOADER = -2;
	private static final long LOADED_FROM_OTHER_CLASSLOADER = -3;
	private static final long CLAZZ_NOT_FOUND = -4;
	private static final long UNCHECKED = -1;

	/**
	 * 
	 * @param ctx Injected Bundlecontext
	 * @param clazzName Class to load from all bundles and check sources.
	 * @param optionalSkipSystemBundle optional: set to true if System Bundle (ID=0) should be skipped.
	 * @throws Exception
	 */
	public void probe( BundleContext ctx, String clazzName, Boolean optionalSkipSystemBundle )
        throws Exception
    {
		long commonSource = UNCHECKED;
		String sourceName = "";
    	for (Bundle b : ctx.getBundles() ) {
    		if ((optionalSkipSystemBundle != null) && (optionalSkipSystemBundle) && (b.getBundleId() == 0)) continue;
    		long loaded = findSafe(b,clazzName);
    		if (commonSource == UNCHECKED && loaded != CLAZZ_NOT_FOUND) {
    			commonSource = loaded;
    			sourceName = b.getSymbolicName();
    		}else if (loaded == CLAZZ_NOT_FOUND) {
    			// skip that. Not every bundle needs to be able to load that clazz of cause. This is not our test.
    		}else {
    			if (commonSource != loaded) {
    				throw new TestContainerException("Test failed because class " + clazzName + " has been loaded from at least two different sources: " + map(commonSource,sourceName) +" vs. " + map(loaded,b.getSymbolicName()) + ".");
    			}
    		}
    	}    
    }

	private String map(long loaded, String name) {
		if (loaded == LOADED_FROM_BOOTCLASSLOADER) {
			return "[" + name + " loaded from " + "Bootclassloader]";
		}
		if (loaded == LOADED_FROM_OTHER_CLASSLOADER) {
			return "[" + name + " loaded from " + "Other Classloader]";
		}
		
		return "[" + name + " loaded from " + " Bundle " + loaded + "]";
	}

	private long findSafe(Bundle bundle, String className) {
		 try
	        {
	            Class clazz = bundle.loadClass(className);
	            if (clazz.getClassLoader() == null)
	            {
	                return ( LOADED_FROM_BOOTCLASSLOADER );
	            }
	            else if (clazz.getClassLoader() instanceof BundleReference)
	            {
	                Bundle p = ((BundleReference) clazz.getClassLoader()).getBundle();
	                return p.getBundleId();
	            }
	            else
	            {
	                return (LOADED_FROM_OTHER_CLASSLOADER );
	            }
	        }
	        catch (ClassNotFoundException ex)
	        {
	        	return (CLAZZ_NOT_FOUND);
	        }
	}
}
