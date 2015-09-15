package org.ops4j.pax.exam.options;

import org.ops4j.pax.exam.Option;

/**
 * Option specifying the timeout the ProbeInvoker waits for
 * services to appear.
 * 
 * @author Fabian Bongratz, Konstantin Matuschek (Matuschek.ObjectVision@partner.akdb.de)
 * @since 4.6.0, August 11, 2015
 */

public class ProbeInvokerTimeoutOption implements Option {

    private final long timeout;

    /**
     * Constructor.
     * 
     * @param timeoutInMillis
     *            timeout for ProbeInvoker (must be bigger then zero)
     * 
     * @throws IllegalArgumentException
     *             - If timoutInMillis is &lt;= 0
     */
    public ProbeInvokerTimeoutOption(long timeoutInMillis) {
        if (timeoutInMillis < 0) {
            throw new IllegalArgumentException("Timeout must be zero or greater, found: " + timeoutInMillis);
        }
        this.timeout = timeoutInMillis;
    }

    /**
     * Getter.
     * 
     * @return timeout in Milliseconds (never negative)
     */
    public long getTimeout() {
        return this.timeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder().append(FrameworkStartLevelOption.class.getSimpleName())
            .append("{timeout='").append(timeout).append("\'}").toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)timeout;
        return result;
    }

    // CHECKSTYLE:OFF : generated code    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProbeInvokerTimeoutOption other = (ProbeInvokerTimeoutOption) obj;
        if (timeout != other.timeout)
            return false;
        return true;
    }
    // CHECKSTYLE:ON
}
