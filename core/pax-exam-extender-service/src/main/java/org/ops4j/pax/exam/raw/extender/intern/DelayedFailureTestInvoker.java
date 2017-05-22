/*
 * Copyright 2014 Ion Savin.
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
package org.ops4j.pax.exam.raw.extender.intern;

import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;

/**
 * This test invoker is an in-place replacement for an actual
 * invoker for which the initialization failed. It will re-throw
 * the cause of the initialization failure when called.
 */
public class DelayedFailureTestInvoker implements ProbeInvoker {
   private final Throwable cause;
   private String message;

   public DelayedFailureTestInvoker(String message, Throwable cause) {
      this.message = message;
      this.cause = cause;
   }

   @Override
   public void call(Object... args) {
      throw new TestContainerException(message, cause);
   }
}
