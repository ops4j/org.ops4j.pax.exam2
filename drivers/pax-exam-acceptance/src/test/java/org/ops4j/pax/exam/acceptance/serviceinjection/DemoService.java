package org.ops4j.pax.exam.acceptance.serviceinjection;

import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class DemoService implements IDemoService {
    public Integer sum(Integer a,Integer b) {
        return a+b;
    }
}
