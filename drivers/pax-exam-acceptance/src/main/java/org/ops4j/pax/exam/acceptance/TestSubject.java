package org.ops4j.pax.exam.acceptance;


public interface TestSubject {

    <T>T createClient(Class<T> endpoint, ClientConfiguration config);

}
