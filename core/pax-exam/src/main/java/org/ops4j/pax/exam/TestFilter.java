/*
 * Copyright 2019 Nikolas Falco
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class to filter test methods.
 *
 * @author Nikolas Falco
 */
public class TestFilter implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final TestFilter ALL_METHODS = new TestFilter("All Methods");

    private final List<String> uniqueIds;
    private final String description;

    public TestFilter(String description) {
        this(description, null);
    }

    public TestFilter(String description, List<String> uniqueIds) {
        this.description = description;
        this.uniqueIds = uniqueIds == null ? new ArrayList<>() : new ArrayList<>(uniqueIds);
    }

    public void addUniqueId(String testUniqueId) {
        this.uniqueIds.add(testUniqueId);
    }

    public List<String> getUniqueIds() {
        return Collections.unmodifiableList(uniqueIds);
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (description != null) {
            builder.append(Base64.getEncoder().encodeToString(description.getBytes()));
        }
        builder.append(';');

        Iterator<String> ids = uniqueIds.iterator();
        while (ids.hasNext()) {
            builder.append(ids.next());
            if (ids.hasNext()) {
                builder.append('#');
            }
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((uniqueIds == null) ? 0 : uniqueIds.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestFilter other = (TestFilter) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (uniqueIds == null) {
            if (other.uniqueIds != null) {
                return false;
            }
        } else if (!uniqueIds.equals(other.uniqueIds)) {
            return false;
        }
        return true;
    }

    public static TestFilter parse(String s) {
        StringTokenizer st = new StringTokenizer(s, ";");
        if (!st.hasMoreTokens()) {
            return new TestFilter(null);
        }

        String description = nonEmpty(st.nextToken());
        if (description != null) {
            description = new String(Base64.getDecoder().decode(description));
        }

        List<String> uniqueIds = Collections.emptyList();
        if (st.hasMoreTokens()) {
            uniqueIds = Arrays.asList(st.nextToken().split("#"));
        }

        if (st.hasMoreTokens()) {
            throw new IllegalArgumentException(s);
        }
        return new TestFilter(description, uniqueIds);
    }

    private static String nonEmpty(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

}
