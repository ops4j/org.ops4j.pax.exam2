/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.exam.container.externalframework;

import java.util.Arrays;

import static org.ops4j.pax.exam.container.externalframework.options.ExternalFrameworkOptions.*;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.waitForFrameworkStartupFor;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.TestContainerStartTimeoutOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.ops4j.pax.exam.container.externalframework.options.KarafFrameworkConfigurationOption;

/**
 * Before running this test, you must delete all directory like target/test-karaf-home-*
 * @author Stephane Chomat
 *
 */

@RunWith(JUnit4TestRunner.class)
public class KarafConfigurationTest  {
	
	@Inject
	protected BundleContext bundleContext;

    public static final Object[] SERVICES = {
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=history)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=display)(osgi.command.scope=log))", "org.apache.karaf.shell.log", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=change-port)(osgi.command.scope=admin))", "org.apache.karaf.admin.command", 1,
"(&(objectClass=org.osgi.service.url.URLStreamHandlerService)(url.handler.protocol=blueprint))", "org.apache.karaf.deployer.blueprint", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=start)(osgi.command.scope=admin))", "org.apache.karaf.admin.command", 1,
"(&(objectClass=org.osgi.service.url.URLStreamHandlerService)(url.handler.protocol=feature))", "org.apache.karaf.deployer.features", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=resolve)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=stop)(osgi.command.scope=admin))", "org.apache.karaf.admin.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=list)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=sleep)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.shell.packages)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.shell.packages", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=show-tree)(osgi.command.scope=dev))", "org.apache.karaf.shell.dev", 1,
"(&(objectClass=org.osgi.service.command.CommandProcessor))", "org.apache.karaf.shell.console", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=list)(osgi.command.scope=admin))", "org.apache.karaf.admin.command", 1,
"(&(objectClass=org.apache.aries.blueprint.NamespaceHandler)(osgi.service.blueprint.namespace=http://karaf.apache.org/xmlns/shell/v1.0.0))", "org.apache.karaf.shell.console", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=clear)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=set)(osgi.command.scope=log))", "org.apache.karaf.shell.log", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=info)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=tac)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.features.command)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.features.management)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.features.management", 1,
"(&(objectClass=org.osgi.service.cm.ConfigurationListener)(osgi.service.blueprint.compname=configCompleter))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=sort)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.osgi.service.cm.ConfigurationAdmin)(service.description=Configuration Admin Service Specification 1.2 Implementation)(service.pid=org.apache.felix.cm.ConfigurationAdmin)(service.vendor=Apache Software Foundation))", "org.apache.felix.configadmin", 1,
"(&(objectClass=org.apache.felix.fileinstall.ArtifactListener)(objectClass=org.apache.felix.fileinstall.ArtifactUrlTransformer)(osgi.service.blueprint.compname=blueprintDeploymentListener))", "org.apache.karaf.deployer.blueprint", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=dynamic-import)(osgi.command.scope=dev))", "org.apache.karaf.shell.dev", 1,
"(&(objectClass=org.apache.felix.fileinstall.ArtifactListener)(objectClass=org.apache.felix.fileinstall.ArtifactUrlTransformer)(osgi.service.blueprint.compname=springDeploymentListener))", "org.apache.karaf.deployer.spring", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=install)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=display-exception)(osgi.command.scope=log))", "org.apache.karaf.shell.log", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.jaas.config)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.jaas.config", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=bundle-level)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=connect)(osgi.command.scope=admin))", "org.apache.karaf.admin.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=exec)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.osgi.service.threadio.ThreadIO))", "org.apache.karaf.shell.console", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.shell.console)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.shell.console", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=print-stack-traces)(osgi.command.scope=dev))", "org.apache.karaf.shell.dev", 1,
"(&(objectClass=org.osgi.service.startlevel.StartLevel))", "org.apache.felix.framework", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=get)(osgi.command.scope=log))", "org.apache.karaf.shell.log", 1,
"(&(objectClass=org.apache.aries.blueprint.NamespaceHandler)(osgi.service.blueprint.namespace=http://karaf.apache.org/xmlns/jaas/v1.0.0))", "org.apache.karaf.jaas.config", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=imports)(osgi.command.scope=packages))", "org.apache.karaf.shell.packages", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=install)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=cat)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.deployer.blueprint)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.deployer.blueprint", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=restart)(osgi.command.scope=dev))", "org.apache.karaf.shell.dev", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=if)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.features.core)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.features.core", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=help)(osgi.command.scope=*))", "org.apache.karaf.shell.console", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=info)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=each)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=list)(osgi.command.scope=config))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=addUrl)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=new)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.osgi.service.url.URLStreamHandlerService)(url.handler.protocol=wrap))", "org.ops4j.pax.url.wrap", 1,
"(&(objectClass=org.osgi.service.url.URLStreamHandlerService)(url.handler.protocol=mvn))", "org.ops4j.pax.url.mvn", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=update)(osgi.command.scope=config))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.management)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.management", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=removeRepository)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.apache.karaf.jaas.config.JaasRealm)(org.apache.karaf.jaas.module=karaf))", "org.apache.karaf.jaas.modules", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=echo)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=start-level)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.aries.blueprint.NamespaceHandler)(osgi.service.blueprint.namespace=http://aries.apache.org/blueprint/xmlns/blueprint-ext/v1.0.0))", "org.apache.aries.blueprint", 1,
"(&(objectClass=org.osgi.service.log.LogService)(objectClass=org.knopflerfish.service.log.LogService)(objectClass=org.ops4j.pax.logging.PaxLoggingService)(objectClass=org.osgi.service.cm.ManagedService)(service.pid=org.ops4j.pax.logging))", "org.ops4j.pax.logging.pax-logging-service", 1,
"(&(objectClass=org.osgi.service.url.URLStreamHandlerService)(url.handler.protocol=spring))", "org.apache.karaf.deployer.spring", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=listRepositories)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.deployer.spring)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.deployer.spring", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=update)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.shell.dev)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.shell.dev", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=create)(osgi.command.scope=admin))", "org.apache.karaf.admin.command", 1,
"(&(objectClass=org.apache.felix.cm.PersistenceManager)(service.description=Platform Filesystem Persistence Manager)(service.pid=org.apache.felix.cm.file.FilePersistenceManager)(service.ranking=-2147483648)(service.vendor=Apache Software Foundation))", "org.apache.felix.configadmin", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=destroy)(osgi.command.scope=admin))", "org.apache.karaf.admin.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=proplist)(osgi.command.scope=config))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=edit)(osgi.command.scope=config))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=refreshUrl)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=propappend)(osgi.command.scope=config))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.apache.karaf.features.FeaturesListener))", "org.apache.karaf.features.management", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.deployer.features)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.deployer.features", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=uninstall)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=propset)(osgi.command.scope=config))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.shell.log)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.shell.log", 1,
"(&(objectClass=org.apache.karaf.admin.AdminService)(osgi.service.blueprint.compname=adminService))", "org.apache.karaf.admin.core", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintListener)(osgi.service.blueprint.compname=blueprintListener))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.ops4j.pax.logging.spi.PaxAppender)(org.ops4j.pax.logging.appender.name=VmLogAppender)(osgi.service.blueprint.compname=vmLogAppender))", "org.apache.karaf.shell.log", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=listUrl)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.shell.commands)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.osgi.service.cm.ManagedServiceFactory)(service.pid=org.apache.felix.fileinstall))", "org.apache.felix.fileinstall", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=list)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.ops4j.pax.exam.junit.extender.CallableTestMethod)(org.ops4j.pax.exam.test.class=org.apache.karaf.testing.KarafConfigurationTest)(org.ops4j.pax.exam.test.method=testAutoStart))", "pax-exam-probe", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.admin.management)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.admin.management", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.shell.config)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.apache.karaf.jaas.config.KeystoreManager)(osgi.service.blueprint.compname=keystoreManager))", "org.apache.karaf.jaas.config", 1,
"(&(objectClass=org.apache.aries.blueprint.ParserService))", "org.apache.aries.blueprint", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=refresh)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.aries.blueprint.NamespaceHandler)(osgi.service.blueprint.namespace=http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.0.0))", "org.apache.aries.blueprint", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.jaas.modules)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.jaas.modules", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=removeUrl)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=cancel)(osgi.command.scope=config))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=grep)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.osgi.service.packageadmin.PackageAdmin))", "org.apache.felix.framework", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=ls)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.felix.fileinstall.ArtifactListener)(objectClass=org.apache.felix.fileinstall.ArtifactUrlTransformer)(objectClass=org.osgi.framework.BundleListener)(objectClass=java.util.EventListener)(osgi.service.blueprint.compname=featureDeploymentListener))", "org.apache.karaf.deployer.features", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=uninstall)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.karaf.features.FeaturesService)(osgi.service.blueprint.compname=featuresService))", "org.apache.karaf.features.core", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=exports)(osgi.command.scope=packages))", "org.apache.karaf.shell.packages", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.aries.blueprint)(osgi.blueprint.container.version=0.2.0.incubating))", "org.apache.aries.blueprint", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=propdel)(osgi.command.scope=config))", "org.apache.karaf.shell.config", 1,
"(&(objectClass=org.osgi.service.log.LogReaderService))", "org.ops4j.pax.logging.pax-logging-service", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=stop)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=restart)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=framework)(osgi.command.scope=dev))", "org.apache.karaf.shell.dev", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.admin.core)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.admin.core", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=printf)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=javax.management.MBeanServer)(osgi.service.blueprint.compname=mbeanServer))", "org.apache.karaf.management", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.admin.command)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.admin.command", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintContainer)(osgi.blueprint.container.symbolicname=org.apache.karaf.shell.osgi)(osgi.blueprint.container.version=2.1.4.alpha-chomats))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=java)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=listVersions)(osgi.command.scope=features))", "org.apache.karaf.features.command", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=start)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=headers)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.osgi.service.url.URLStreamHandlerService)(url.handler.protocol=jardir))", "org.apache.felix.fileinstall", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=logout)(osgi.command.scope=shell))", "org.apache.karaf.shell.commands", 1,
"(&(objectClass=org.apache.karaf.shell.console.CompletableFunction)(objectClass=org.osgi.service.command.Function)(osgi.command.function=shutdown)(osgi.command.scope=osgi))", "org.apache.karaf.shell.osgi", 1,
"(&(objectClass=org.osgi.service.blueprint.container.BlueprintListener))", "org.apache.aries.jmx.blueprint", 2,
"(&(name=basic)(objectClass=org.apache.karaf.jaas.modules.EncryptionService)(service.ranking=-1))", "org.apache.karaf.jaas.modules", 1,
};
    
	@Configuration
    public static Option[] configuration() throws Exception{
		MavenArtifactUrlReference url = new MavenArtifactUrlReference().groupId("org.apache.karaf").artifactId("apache-karaf").type("tar.gz").versionAsInProject();
		return options(
        		new KarafFrameworkConfigurationOption("start2").
        			addConfiguration(
        					url,
        					"apache-karaf-"+getVersion(url)).
        			home().
                	systemBundleId(31).
        			defaultConf(),
               
        		// wait for ever
        		systemProperty("karaf.restart.clean").value("true"),
        		new TestContainerStartTimeoutOption(Long.MAX_VALUE),
                //vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5008" ),
            		 
             waitForFrameworkStartupFor(60000)
        );
    }
	
	
	/** 
	 * Start two bundles at level one and two.
	 * At level one start mvn handler service and at level 2 use mvn handler service.
	 * @throws Exception cannot start karaf.
	 */
	@Test
    public void testAutoStart() throws Exception {
		Thread.sleep(1000);
		Bundle[] bundles = bundleContext.getBundles();
		Assert.assertTrue(bundles.length>=3);
        int i = 1;
        for (Bundle b : bundles) {
			System.out.println(""+(i++)+": "+b+" - "+b.getLocation());
		}
        for (Bundle b : bundles) {
			Assert.assertTrue(b.getSymbolicName()+" not activate", b.getState() == b.ACTIVE);
		}
        Assert.assertEquals(32+6, bundles.length);
        ServiceReference[] services = bundleContext.getAllServiceReferences(null, null);
        i = 1;
        for (ServiceReference s : services) {
            System.out.println(""+(i++)+": "+toString(s.getProperty(Constants.OBJECTCLASS))+" - "+s.getBundle());
        }
        for (ServiceReference s : services) {
            StringBuilder sb = new StringBuilder();
            sb.append("(&");
            String[] keys = s.getPropertyKeys();
            for (String k : keys) {
                add(sb, k,s.getProperty(k));
            }
            sb.append(")");
            System.out.println("\""+sb.toString()+"\", \""+s.getBundle().getSymbolicName()+"\", ");
            try {
                bundleContext.getAllServiceReferences(null, sb.toString());
           } catch (Throwable e) {
               e.printStackTrace();
           }
        }
        ServiceReference[] result;
        for (int j = 0; j < SERVICES.length; ) {
            String filter = (String) SERVICES[j++];
            String sn = (String) SERVICES[j++];
            int nb = (Integer) SERVICES[j++];
            System.out.println(filter);
            result = bundleContext.getAllServiceReferences(null, filter);
            Assert.assertNotNull(result);
            Assert.assertEquals(nb, result.length);
            //Assert.assertEquals(sn, result[0]);
        }
	}

    private String toString(Object property) {
        if (property instanceof String[]) {
            String[] objClass = (String[]) property;
            if (objClass.length == 1)
                return objClass[0];
            return Arrays.asList(objClass).toString();
        }
        if (property instanceof Object[]) {
            Object[] objClass = (Object[]) property;
            return Arrays.asList(objClass).toString();
        }
        if (property == null)
            return "null";
        return property.toString();
    }

    private void add(StringBuilder sb, String k, Object property) {
        if ("service.id".equals(k))
            return;
        if (property instanceof String[]) {
            String[] objClass = (String[]) property;
            for (String s : objClass) {
                sb.append("(").append(k).append("=").append(s).append(")");
            }
            return;
        }
        if (property instanceof Object[]) {
            Object[] objClass = (Object[]) property;
            for (Object s : objClass) {
                sb.append("(").append(k).append("=").append(s).append(")");
            }

        }
        if (property == null)
            return ;
         sb.append("(").append(k).append("=").append(property).append(")");
    }
}


