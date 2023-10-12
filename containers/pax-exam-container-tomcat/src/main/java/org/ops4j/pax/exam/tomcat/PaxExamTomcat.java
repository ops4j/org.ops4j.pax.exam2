/*
 * Copyright 2014 Harald Wellmann
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
package org.ops4j.pax.exam.tomcat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.ops4j.pax.exam.TestContainerException;


public class PaxExamTomcat extends Tomcat {
    

    private static final String[] CONTEXT_XML = { 
        "src/test/resources/META-INF/context.xml",
        "src/main/webapp/META-INF/context.xml" 
    };

    @Override
    public Context addWebapp(Host host, String contextPath, String docBase) {

        Context ctx = new StandardContext();
        ctx.setPath(contextPath);
        ctx.setDocBase(docBase);

        ctx.addLifecycleListener(new DefaultWebXmlListener());
        URL configFile = getWebappConfigFile(docBase, contextPath);
        ctx.setConfigFile(configFile);

        ContextConfig ctxCfg = new TomcatContextConfig();
        ctx.addLifecycleListener(ctxCfg);
        
        if (host == null) {
            getHost().addChild(ctx);
        } 
        else {
            host.addChild(ctx);
        }

        return ctx;
    }
    
    @Override
    protected URL getWebappConfigFile(String path, String contextName) {
        URL configFile = super.getWebappConfigFile(path, contextName);
        if (configFile == null) {
            for (String fileName : CONTEXT_XML) {
                File contextXml = new File(fileName);
                if (contextXml.exists()) {
                    try {
                        configFile = contextXml.toURI().toURL();
                        break;
                    }
                    catch (MalformedURLException exc) {
                        throw new TestContainerException(exc);
                    }
                }
            }
            
        }
        return configFile;
    }   
}
