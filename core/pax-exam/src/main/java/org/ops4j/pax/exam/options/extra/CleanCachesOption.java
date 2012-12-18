package org.ops4j.pax.exam.options.extra;

import org.ops4j.pax.exam.options.ValueOption;

/**
 * @author Toni Menzel (tonit)
 * @since Mar 7, 2009
 */
public class CleanCachesOption implements ValueOption<Boolean> {

    private static final Boolean DEFAULT_VALUE = Boolean.TRUE;
    private Boolean value;

    public CleanCachesOption() {
        value = DEFAULT_VALUE;
    }

    public CleanCachesOption(Boolean value) {
        this.value = value;
    }

    public CleanCachesOption setValue(Boolean value) {
        this.value = value;
        return this;
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CleanCachesOption other = (CleanCachesOption) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        }
        else if (!value.equals(other.value))
            return false;
        return true;
    }

}
