/*
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
package org.ops4j.pax.exam.spi.reactors;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeBuilder;

public class ReactorManagerTest {
   /**
    * 
    * Verifies that simulating beforeSuite/afterSuite based on beforeClass/afterClass
    * calls is consistent when using PerSuite and PerClass reactors together.
    *
    */
   @Test
   public void testBeforeAfterSuiteSimulation() {
      ReactorManager reactorManagerSpy = Mockito.spy(ReactorManager.getInstance());
      List<TestContainer> containers = new ArrayList<TestContainer>();
      List<TestProbeBuilder> probes = new ArrayList<TestProbeBuilder>();
      Object testClassInstance = new Object();
      Class<?> testClass = testClassInstance.getClass();

      /* Assume the ReactorManager always has test classes left. */
      Mockito.doReturn(true).when(reactorManagerSpy).hasMoreTestClasses();

      /* PerClass reactor. */
      EagerSingleStagedReactor perClassReactor = new EagerSingleStagedReactor(containers, probes);
      /* Calling beforeClass() on one reactor shouldn't affect beforeSuite() simulation on other ones. */
      reactorManagerSpy.beforeClass(perClassReactor, testClassInstance);
      reactorManagerSpy.afterClass(perClassReactor, testClass);

      /* PerSuite reactor. */
      PerSuiteStagedReactor perSuiteReactor = Mockito.spy(new PerSuiteStagedReactor(containers, probes));

      reactorManagerSpy.beforeClass(perSuiteReactor, testClassInstance);
      /* There was no explicit beforeSuite() one should have been simulated. */
      Mockito.verify(perSuiteReactor, Mockito.times(1)).beforeSuite();

      reactorManagerSpy.afterClass(perSuiteReactor, testClass);
      /* There are still test classes left don't simulate afterSuite() yet. */
      Mockito.verify(perSuiteReactor, Mockito.times(0)).afterSuite();

      reactorManagerSpy.beforeClass(perSuiteReactor, testClassInstance);
      /* beforeSuite() should not be simulated a second time. */
      Mockito.verify(perSuiteReactor, Mockito.times(1)).beforeSuite();

      reactorManagerSpy.afterClass(perSuiteReactor, testClass);
      /* There are still test classes left don't simulate afterSuite() yet. */
      Mockito.verify(perSuiteReactor, Mockito.times(0)).afterSuite();

      reactorManagerSpy.beforeClass(perSuiteReactor, testClassInstance);
      /* beforeSuite() should not be simulated a second time. */
      Mockito.verify(perSuiteReactor, Mockito.times(1)).beforeSuite();
      /* This is the last test class.*/
      Mockito.doReturn(false).when(reactorManagerSpy).hasMoreTestClasses();
      reactorManagerSpy.afterClass(perSuiteReactor, testClass);
      /* Simulate afterSuite() after the last test class. */
      Mockito.verify(perSuiteReactor, Mockito.times(1)).afterSuite();
   }
}
