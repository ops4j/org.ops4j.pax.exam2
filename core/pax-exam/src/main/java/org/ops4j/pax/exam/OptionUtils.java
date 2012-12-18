/*
 * Copyright 2008 Alin Dreghiciu.
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.ops4j.pax.exam.options.CompositeOption;

/**
 * Utility methods related to {@link Option}s.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 08, 2008
 */
public class OptionUtils {

    /**
     * Utility class. Ment to be used via the static factory methods.
     */
    private OptionUtils() {
        // utility class
    }

    /**
     * Expand options to one level by expanding eventual {@link CompositeOption}. During this
     * process null options are eliminated.
     * 
     * @param options
     *            options to be expanded (can be null or an empty array)
     * 
     * @return expanded options (never null). In case that the options to be expanded is null an
     *         empty array is returned
     */
    public static Option[] expand(final Option... options) {
        final List<Option> expanded = new ArrayList<Option>();
        if (options != null) {
            for (Option option : options) {
                if (option != null) {
                    if (option instanceof CompositeOption) {
                        expanded.addAll(Arrays.asList(((CompositeOption) option).getOptions()));
                    }
                    else {
                        expanded.add(option);
                    }
                }
            }
        }
        return expanded.toArray(new Option[expanded.size()]);
    }

    /**
     * Combines two arrays of options in one array containing both provided arrays in order they are
     * provided.
     * 
     * @param options1
     *            array of options (can be null or empty array)
     * @param options2
     *            array of options (can be null or empty array)
     * 
     * @return combined array of options (never null). In case that both arrays are null or empty an
     *         empty array is returned
     */
    public static Option[] combine(final Option[] options1, final Option... options2) {
        int size1 = 0;
        if (options1 != null && options1.length > 0) {
            size1 += options1.length;
        }
        int size2 = 0;
        if (options2 != null && options2.length > 0) {
            size2 += options2.length;
        }
        final Option[] combined = new Option[size1 + size2];
        if (size1 > 0) {
            System.arraycopy(options1, 0, combined, 0, size1);
        }
        if (size2 > 0) {
            System.arraycopy(options2, 0, combined, size1, size2);
        }
        return combined;
    }

    /**
     * Filters the provided options by class returning an array of those option that are instance of
     * the provided class. Before filtering the options are expanded {@link #expand(Option[])}.
     * 
     * @param optionType
     *            class of the desired options
     * @param options
     *            options to be filtered (can be null or empty array)
     * @param <T>
     *            type of desired options
     * 
     * @return array of desired option type (never null). In case that the array of filtered options
     *         is null, empty or there is no option that matches the desired type an empty array is
     *         returned
     */
    @SuppressWarnings("unchecked")
    public static <T extends Option> T[] filter(final Class<T> optionType, final Option... options) {
        final List<T> filtered = new ArrayList<T>();
        for (Option option : expand(options)) {
            if (optionType.isAssignableFrom(option.getClass())) {
                filtered.add((T) option);
            }
        }
        final T[] result = (T[]) Array.newInstance(optionType, filtered.size());
        return filtered.toArray(result);
    }

    /**
     * Removes from the provided options all options that are instance of the provided class,
     * returning the remaining options.
     * 
     * @param optionType
     *            class of the desired options to be removed
     * @param options
     *            options to be filtered (can be null or empty array)
     * 
     * @return array of remaining options (never null) after removing the desired type
     */

    public static Option[] remove(final Class<? extends Option> optionType, final Option... options) {
        final List<Option> filtered = new ArrayList<Option>();
        for (Option option : expand(options)) {
            if (!optionType.isAssignableFrom(option.getClass())) {
                filtered.add(option);
            }
        }
        return filtered.toArray(new Option[filtered.size()]);
    }

}
