/*
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
package org.ops4j.pax.exam.container.eclipse.impl.repository;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.osgi.internal.framework.FilterImpl;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitProviding;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.VersionRange;

/**
 * Container class that holds requirement information of a unit
 * 
 * @author Christoph Läubrich
 *
 */
public final class Requires extends VersionRangeSerializable
    implements EclipseInstallableUnit.UnitRequirement, Serializable {

    private static final long serialVersionUID = 120291514995052340L;
    private final String namespace;
    private final String name;
    private final String match;
    private final String matchParameters;
    private final boolean optional;
    private final boolean greedy;
    private final String filterString;
    private transient Filter filter;

    public Requires(String namespace, String name, VersionRange versionRange, String match,
        String matchParameters, boolean optional, boolean greedy, String filter) {
        super(versionRange);
        this.namespace = namespace;
        this.name = name;
        this.match = match;
        this.matchParameters = matchParameters;
        this.optional = optional;
        this.greedy = greedy;
        this.filterString = filter;
    }

    @Override
    public boolean matches(Map<String, ?> map) {
        if (filterString != null) {
            if (filter == null) {
                try {
                    filter = FilterImpl.newInstance(filterString);
                }
                catch (InvalidSyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return filter.matches(map);
        }
        return true;
    }

    @Override
    public String toString() {
        return "Requires:" + getID();
    }

    @Override
    public String getID() {
        return namespace + ":" + name + ":" + getVersionRange();
    }

    @Override
    public boolean isGreedy() {
        return greedy;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public boolean matches(UnitProviding providing) {
        // @formatter:off
        //<required match='providedCapabilities.exists(x | x.name == $0 &amp;&amp; x.namespace == $1)' matchParameters='[&apos;org.eclipse.ui.forms&apos;, &apos;org.eclipse.equinox.p2.iu&apos;]' min='0' max='0'/>
        //<required namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.rap.nebula.jface.gridviewer' range='[3.1.2.20161108-1505,3.1.2.20161108-1505]'/>
       
//        <provides size='12'>
//        <provided namespace='org.eclipse.equinox.p2.iu' name='org.eclipse.ui.forms' version='3.7.1.v20161220-1635'/>
//        <provided namespace='osgi.bundle' name='org.eclipse.ui.forms' version='3.7.1.v20161220-1635'/>
//        <provided namespace='java.package' name='org.eclipse.ui.forms' version='0.0.0'/>
//        <provided namespace='java.package' name='org.eclipse.ui.forms.editor' version='0.0.0'/>
//        <provided namespace='java.package' name='org.eclipse.ui.forms.events' version='0.0.0'/>
//        <provided namespace='java.package' name='org.eclipse.ui.forms.widgets' version='0.0.0'/>
//        <provided namespace='java.package' name='org.eclipse.ui.internal.forms' version='0.0.0'/>
//        <provided namespace='java.package' name='org.eclipse.ui.internal.forms.css.dom' version='0.0.0'/>
//        <provided namespace='java.package' name='org.eclipse.ui.internal.forms.css.properties.css2' version='0.0.0'/>
//        <provided namespace='java.package' name='org.eclipse.ui.internal.forms.widgets' version='0.0.0'/>
//        <provided namespace='org.eclipse.equinox.p2.eclipse.type' name='bundle' version='1.0.0'/>
//        <provided namespace='org.eclipse.equinox.p2.localization' name='df_LT' version='1.0.0'/>
//      </provides>
//        
        // @formatter:on       

        if (namespace == null || namespace.isEmpty()) {
            // TODO match required option, not supported yet!
        }
        else if (providing.getNamespace().equals(namespace)) {
            // normal namespace matching...
            if (providing.getName().equals(name)) {
                return getVersionRange().includes(providing.getVersion());
            }
        }
        return false;
    }

}
