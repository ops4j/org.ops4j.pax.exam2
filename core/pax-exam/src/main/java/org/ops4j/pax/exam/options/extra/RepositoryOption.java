/*
 * Copyright 2008 Toni Menzel.
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
package org.ops4j.pax.exam.options.extra;

import org.ops4j.pax.exam.Option;

/**
 * Option specifiying a Pax Runner repository.
 *
 * @author Toni Menzel (tonit)
 * @since 0.3.0, December 19, 2008
 */
public interface RepositoryOption
    extends Option
{

    /**
     * Mark repository as allowing snapshots.
     *
     * @return this for fluent api
     */
    public RepositoryOption allowSnapshots();

    /**
     * Mark repository as not allowing releases.
     *
     * @return this for fluent api
     */
    public RepositoryOption disableReleases();

}
