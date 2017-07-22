package org.ops4j.pax.exam.acceptance;

public class ExcludesFilterSpec extends FilterSpec {

    public static ExcludesFilterSpec excludes(String... excludes) {
        return new ExcludesFilterSpec(excludes);
    }

    public ExcludesFilterSpec(String[] excludes ) {
        super(null,excludes);
    }
}
