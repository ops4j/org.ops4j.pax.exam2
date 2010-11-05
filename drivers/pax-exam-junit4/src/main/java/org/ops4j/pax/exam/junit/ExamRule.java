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
