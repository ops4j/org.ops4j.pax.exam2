package org.ops4j.pax.exam.acceptance;

import org.ops4j.pax.exam.TestContainer;

public interface TestSubject {

    <T>T createClient(Class<T> endpoint, ClientConfiguration config);

    <T> T createClient(Class<T> endpoint);

    TestContainer getTestContainer();


}
