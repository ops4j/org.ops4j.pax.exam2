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
package org.ops4j.pax.exam;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


/**
 * An instance that drives cross cutting concerns when using Pax Exam.
 * - Provides System Resource Locations,
 * - Cross Cutting default properties,
 * - access to API entry (building probes, getting TestContainerFactory etc.)
 * 
 * @author Toni Menzel ( toni@okidokiteam.com )
 *
 */
public interface ExamSystem {
	
		public  <T extends Option> T[] getOptions( final Class<T> optionType );
		
		
		public ExamSystem fork (Option[] options ) throws IOException;
		
		/**
		 * 
		 * @return the basic directory that Exam should use to look at user-defaults.
		 */
		public File getConfigFolder();
		
		/**
		 * 
		 * Each call to this method might create a new folder that is being cleared on clear().
		 * 
		 * @return the basic directory that Exam should use for all IO write activities.
		 * 
		 */
		public File getTempFolder() ;
		
		
		/**
		 * 
		 * @return a relative indication of how to deal with timeouts.
		 */
		public RelativeTimeout getTimeout();
		
		
		
		public TestProbeBuilder createProbe( Properties p ) throws IOException ;


		public String createID(String purposeText);
		
		/**
		 * 
		 * Clears up resources taken by system (like temporary files)
		 */
		public void clear();
}
