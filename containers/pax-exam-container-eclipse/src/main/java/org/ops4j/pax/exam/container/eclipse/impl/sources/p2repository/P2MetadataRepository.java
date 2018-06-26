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
package org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPathException;

import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2MetadataRepositoryParser;
import org.ops4j.pax.exam.container.eclipse.impl.repository.Unit;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseUnitSource;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the Metadata repository of P2
 * 
 * @author Christoph Läubrich
 *
 */
public class P2MetadataRepository extends AbstractEclipseUnitSource<P2Unit> {

    private static final Logger LOG = LoggerFactory.getLogger(P2ArtifactRepository.class);

    private final P2ArtifactRepository artifactRepository;

    private final String name;

    private final long lastModified;

    public P2MetadataRepository(String name, P2RepositoryFile root,
        P2ArtifactRepository artifactRepository) throws IOException {
        this.name = name;
        this.artifactRepository = artifactRepository;
        try {
            lastModified = addFile(root);
        }
        catch (XPathException | InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    private long addFile(P2RepositoryFile file)
        throws IOException, InvalidSyntaxException, XPathException {
        if (file.isComposite()) {
            long lastm = -1;
            List<P2RepositoryFile> childs = file.getChilds();
            for (P2RepositoryFile child : childs) {
                lastm = Math.max(lastm, addFile(child));
            }
            return lastm;
        }
        else if (file.isMetadataRepository()) {
            String reproName = name + file.getURL();
            LOG.info("Parse {}@{}...", file.getType(), file.getURL());
            P2MetadataRepositoryParser parser = new P2MetadataRepositoryParser(
                file.getRespository());
            LOG.info("... {} units parsed.", parser.getCount());
            Collection<Unit> units = parser.getUnits();
            for (Unit unit : units) {
                add(new ArtifactInfo<P2Unit>(unit, new P2Unit(unit, reproName)));
            }
            return file.getLastModified();
        }
        else {
            LOG.info("Ignore P2RepositoryFile of type {}@{}...", file.getType(), file.getURL());
            return -1;
        }

    }

    @Override
    protected EclipseInstallableUnit getArtifact(ArtifactInfo<P2Unit> info) throws IOException {
        return new P2EclipseInstallableUnit(info.getContext(), artifactRepository, this);
    }

    public long getLastModified() {
        return lastModified;
    }

}
