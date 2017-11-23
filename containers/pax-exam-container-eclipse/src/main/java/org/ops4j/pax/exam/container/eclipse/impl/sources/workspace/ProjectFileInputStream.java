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
package org.ops4j.pax.exam.container.eclipse.impl.sources.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;

/**
 * 
 * An {@link FileInputStream} that carry information about the coresponding project context and
 * workspace folder use for downstream items tha need to parse additional infos from those
 * 
 * @author Christoph Läubrich
 *
 */
public class ProjectFileInputStream extends FileInputStream {

    private ProjectParser context;
    private File workspaceFolder;

    public ProjectFileInputStream(String name, ProjectParser context, File workspaceFolder)
        throws FileNotFoundException {
        super(new File(context.getProjectFolder(), name));
        this.context = context;
        this.workspaceFolder = workspaceFolder;
    }

    public ProjectParser getProject() {
        return context;
    }

    public File getWorkspaceFolder() {
        return workspaceFolder;
    }

}
