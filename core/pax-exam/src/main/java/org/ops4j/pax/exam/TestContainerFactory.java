/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2008-2011 Toni Menzel.
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

/**
 * {@link TestContainer} factory.
 *
 * @author Toni Menzel (toni@okidokiteam.com)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 09, 2008
 */
public interface TestContainerFactory {

    /**
     * Creates a {@link TestContainer}. Depending on the underlying implementation.
     *
     * @param system
     *            System that you want to materialize the containers from
     *
     * @return created test container
     *
     * @throws TestContainerException
     *             fail if incompatible options are being passed in.
     */
    TestContainer create(ExamSystem system);
}
