/*
 * Copyright 2016 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam;

import java.io.Serializable;

/**
 * @author hwellmann
 *
 */
public class TestDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    private String className;
    private String methodName;
    private Integer index;
    private TestFilter filter;

    /**
     *
     */
    public TestDescription(String className) {
        this(className, null);
    }

    public TestDescription(String className, String methodName) {
        this(className, methodName, null);
    }

    public TestDescription(String className, String methodName, Integer index) {
        this(className, methodName, index, null);
    }

    public TestDescription(String className, String methodName, Integer index, TestFilter filter) {
        this.className = nonEmpty(className);
        this.methodName = nonEmpty(methodName);
        this.index = index;
        this.filter = filter;
    }

    private static String nonEmpty(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return the index
     */
    public Integer getIndex() {
        return index;
    }

    public TestFilter getFilter() {
        return this.filter;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(className);
        builder.append(':');
        if (methodName != null) {
            builder.append(methodName);
        }
        builder.append(':');
        if (index != null) {
            builder.append(index);
        }
        builder.append(':');
        if (filter != null) {
            builder.append(filter);
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + ((filter == null) ? 0 : filter.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TestDescription)) {
            return false;
        }
        TestDescription other = (TestDescription) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        }
        else if (!className.equals(other.className)) {
            return false;
        }
        if (index == null) {
            if (other.index != null) {
                return false;
            }
        }
        else if (!index.equals(other.index)) {
            return false;
        }
        if (methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        }
        else if (!methodName.equals(other.methodName)) {
            return false;
        }
        return true;
    }

    public static TestDescription parse(String s) {
        TestFilter filter = null;
        Integer index = null;

        String[] parts = s.split(":");
        switch (parts.length) {
            case 4:
                if (nonEmpty(parts[3]) != null) {
                    filter = TestFilter.parse(parts[3]);
                }
            case 3:
                if (nonEmpty(parts[2]) != null) {
                    index = Integer.parseInt(parts[2]);
                }
            case 2:
                return new TestDescription(parts[0], parts[1], index, filter);
            case 1:
                return new TestDescription(parts[0]);
            default:
                throw new IllegalArgumentException(s);
        }
    }

}
