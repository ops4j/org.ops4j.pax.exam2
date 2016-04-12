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

    /**
     *
     */
    public TestDescription(String className) {
        this(className, null, null);
    }

    public TestDescription(String className, String methodName) {
        this(className, methodName, null);
    }

    public TestDescription(String className, String methodName, Integer index) {
        this.className = className;
        this.methodName = methodName;
        this.index = index;
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



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
        TestDescription other = (TestDescription) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        }
        else if (!className.equals(other.className))
            return false;
        if (index == null) {
            if (other.index != null)
                return false;
        }
        else if (!index.equals(other.index))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        }
        else if (!methodName.equals(other.methodName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(className);
        if (methodName != null) {
            builder.append(':');
            builder.append(methodName);
        }
        if (index != null) {
            builder.append(':');
            builder.append(index);
        }
        return builder.toString();
    }

    public static TestDescription parse(String s) {
        String[] parts = s.split(":");
        switch (parts.length) {
            case 1:
                return new TestDescription(parts[0]);
            case 2:
                return new TestDescription(parts[0], parts[1]);
            case 3:
                return new TestDescription(parts[0], parts[1], Integer.parseInt(parts[2]));
            default:
                throw new IllegalArgumentException(s);
        }
    }
}
