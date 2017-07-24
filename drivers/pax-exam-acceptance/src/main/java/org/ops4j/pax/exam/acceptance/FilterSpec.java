package org.ops4j.pax.exam.acceptance;

import lombok.Data;

@Data
public class FilterSpec {

    public FilterSpec(String[] includes, String[] excudes) {
        this.includes = includes;
        this.excudes = excudes;
    }

    public FilterSpec() {
    }

    private String[] includes;

    private String[] excudes;
}
