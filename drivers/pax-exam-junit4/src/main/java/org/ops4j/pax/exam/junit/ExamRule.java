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
package org.ops4j.pax.exam.junit;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class ExamRule implements MethodRule {

	public ExamRule(Object test) {
		String name = test.getClass().getName();
		System.out.println("NEW Exam Context for " + name);
		// we can stage all regression containers upfront here !
		
		

	}
	public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				
				try {
					String name = target.getClass().getName();
					System.out.println("Method is " + method.getName() + " , target is " + name + ",base is " + base);
					//base.evaluate();
				} finally {
					//
				}
			}
		};
	}

}
