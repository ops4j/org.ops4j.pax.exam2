package org.ops4j.pax.exam.acceptance.junit4;

import org.junit.rules.MethodRule;
import org.ops4j.pax.exam.acceptance.TestSubject;
import org.osgi.service.http.HttpService;

public interface TestSubjectRule extends MethodRule, TestSubject {

}
