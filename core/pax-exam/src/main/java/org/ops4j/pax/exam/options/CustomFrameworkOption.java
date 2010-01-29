/*
 * Copyright 2009 Guillaume Nodet.
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
package org.ops4j.pax.exam.options;

/**
 * Option specifing a custom framework.
 *
 * @author Guillaume Nodet (gnodet@gmail.com)
 * @since 0.7.0
 */
public class CustomFrameworkOption extends FrameworkOption
{

    private String definitionURL;

    private String basePlatform;

    /**
     * Set a custom framework with a definition url, a baseplatform and an identifier to
     * for your testresults.
     *
     * @param definitionURL
     * @param basePlatform  This can be felix, equinox, knopflerfish etc. These are the platforms
     *                      defined as PlatformBuilder for Pax Runner. This property is needed if the platform in
     *                      the definition file isn't based on default.platfom in Pax Runners runner properties.
     * @param name
     */
    public CustomFrameworkOption( String definitionURL, String basePlatform, String name )
    {
        super( basePlatform + "/" + name + "[" + definitionURL + "]" );
        this.definitionURL = definitionURL;
        this.basePlatform = basePlatform;
    }

    public String getDefinitionURL()
    {
        return definitionURL;
    }

    public String getBasePlatform()
    {
        return basePlatform;
    }

}
