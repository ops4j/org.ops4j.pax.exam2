package org.ops4j.pax.exam.acceptance.junit4;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.acceptance.ClientConfiguration;
import org.ops4j.pax.exam.acceptance.SessionSpec;
import org.ops4j.pax.exam.acceptance.rest.api.RestClient;
import org.ops4j.pax.exam.acceptance.rest.restassured.RestClientImpl;
import org.ops4j.pax.exam.spi.PaxExamRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;


public class OSGiTestSubjectRule implements TestSubjectRule {

    private final Option m_options;

    private final static Map<Class<?>,Class<?>> clientTypes = new HashMap<>();

    private static Logger LOG = LoggerFactory.getLogger(OSGiTestSubjectRule.class);

    static {
        clientTypes.put(RestClient.class, RestClientImpl.class);
    }

    private final SessionSpec sessionSpec;
    private TestContainer container;

    public OSGiTestSubjectRule(Option options, SessionSpec spec) {
        sessionSpec = spec;
        // put into OSGi system to know. This is a very stupid assumption.
        // Need a way to map SessionSpecs to .. ExamOptions in a rather generic way.
        m_options = composite(
            options,
                newConfiguration("org.apache.felix.http")
                .put("org.osgi.service.http.port",spec.getPort())
                .asOption());
    }


    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                ExamSystem system = PaxExamRuntime.createTestSystem(m_options);
                container = PaxExamRuntime.createContainer(system);
                container.start();
                try {
                    base.evaluate();
                } finally {
                    container.stop();
                }
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createClient(Class<T> endpoint, ClientConfiguration config) {
        Class impl = clientTypes.get(endpoint);

        if (impl == null) {
            InvocationHandler handler = new OSGiServiceInvokationHandler(endpoint);
            return (T) Proxy.newProxyInstance(endpoint.getClassLoader(), new Class[]{endpoint}, handler);
        }

        try {
            Constructor con = impl.getDeclaredConstructor(SessionSpec.class, ClientConfiguration.class);
            return (T) con.newInstance(sessionSpec,config);
        } catch (Exception e) {
            throw new RuntimeException("Problem creating client of type " + endpoint.getName(),e);
        }
    }

    @Override
    public <T> T createClient(Class<T> endpoint) {
        return createClient(endpoint, null);
    }

    @Override
    public TestContainer getTestContainer() {
        return container;
    }

    private class OSGiServiceInvokationHandler<T> implements InvocationHandler {
        private final Class<T> type;

        public OSGiServiceInvokationHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args == null) {
                args = new Object[0];
            }
            LOG.info("Callout to " + type.getName() + " method " + method.getName() + " with args: " + args.length);
            return container.remoteCall(type,method.getName(),method.getParameterTypes(),null, RelativeTimeout.TIMEOUT_DEFAULT,args);
        }
    }
}

