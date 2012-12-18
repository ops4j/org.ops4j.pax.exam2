package org.ops4j.pax.exam.options;

import org.ops4j.pax.exam.Option;

public interface ValueOption<T> extends Option {

    T getValue();
}
