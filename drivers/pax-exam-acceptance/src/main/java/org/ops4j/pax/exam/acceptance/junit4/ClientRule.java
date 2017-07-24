package org.ops4j.pax.exam.acceptance.junit4;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.acceptance.ClientConfiguration;

public class ClientRule<T> implements MethodRule {

    private final TestSubjectRule subject;
    private final Class<T> client;
    private final ClientConfiguration config;

    public ClientRule (TestSubjectRule subject, Class<T> client, ClientConfiguration configuration) {
        this.subject = subject;
        this.client = client;
        this.config = configuration;
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        return subject.apply(base,method,target);
    }

    public T client() {
        return  subject.createClient(client,config);
    }

}
