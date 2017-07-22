package org.ops4j.pax.exam.acceptance;

public class IncludesFilterSpec extends FilterSpec {

    public static IncludesFilterSpec includes(String... includes) {
        return new IncludesFilterSpec(includes);
    }

    IncludesFilterSpec(String[] includes) {
        super(includes,null);
    }

}
