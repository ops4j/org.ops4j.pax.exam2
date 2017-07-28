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
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.ops4j.pax.exam.container.eclipse.impl.parser.P2ArtifactRepositoryParser;
import org.ops4j.pax.exam.container.eclipse.impl.repository.EclipseClassifiedVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureSource;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the ArtifactRepository of P2
 * 
 * @author Christoph Läubrich
 *
 */
public class P2ArtifactRepository extends BundleAndFeatureSource {

    private static final Logger LOG = LoggerFactory.getLogger(P2ArtifactRepository.class);

    private final P2BundleSource bundleSource = new P2BundleSource();
    private final P2FeatureSource featureSource = new P2FeatureSource();

    private final String name;

    public P2ArtifactRepository(String name, P2RepositoryFile root) throws IOException {
        this.name = name;
        try {
            addFile(root);
        }
        catch (XPathExpressionException | InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    private void addFile(P2RepositoryFile file)
        throws XPathExpressionException, IOException, InvalidSyntaxException {
        if (file.isComposite()) {
            List<P2RepositoryFile> childs = file.getChilds();
            for (P2RepositoryFile child : childs) {
                addFile(child);
            }
        }
        else if (file.isArtifactRepository()) {
            LOG.info("Parse {}@{}...", file.getType(), file.getURL());
            P2ArtifactRepositoryParser parser = new P2ArtifactRepositoryParser(
                file.getIndex().getURL(), file.getRespository());
            String reproName = name + file.getURL();
            bundleSource.addBundles(reproName,
                parser.getArtifacts(EclipseClassifiedVersionedArtifact.CLASSIFIER_BUNDLE));
            featureSource.addFeatures(reproName,
                parser.getArtifacts(EclipseClassifiedVersionedArtifact.CLASSIFIER_FEATURE));
        }
        else {
            LOG.info("Ignore P2RepositoryFile of type {}@{}...", file.getType(), file.getURL());
        }

    }

    @Override
    protected EclipseBundleSource getBundleSource() {
        return bundleSource;
    }

    @Override
    protected EclipseFeatureSource getFeatureSource() {
        return featureSource;
    }

}
