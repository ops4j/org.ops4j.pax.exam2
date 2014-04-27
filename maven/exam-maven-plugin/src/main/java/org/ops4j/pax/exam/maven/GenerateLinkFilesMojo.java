package org.ops4j.pax.exam.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;

/**
 * @goal generate-link-files
 * @phase generate-test-resources
 * @requiresDependencyResolution test
 */
public class GenerateLinkFilesMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project.artifacts}"
     * @readonly
     * @required
     */
    private Set<Artifact> projectArtifacts;

    /**
     * @parameter default-value="${project.build}"
     * @readonly
     * @required
     */
    private Build build;

    /**
     * @parameter default-value="${project.build.directory}/pax-exam-links"
     */
    private File outputDirectory;

    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        FilterArtifacts filter = new FilterArtifacts();
        filter.addFilter(new ScopeFilter(Artifact.SCOPE_TEST, null));
        filter.addFilter(new TypeFilter("jar", null));
        Set<Artifact> artifacts;
        try {
            artifacts = filter.filter(projectArtifacts);
        }
        catch (ArtifactFilterException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
        // create output directory with link files
        outputDirectory.mkdirs();
        for (Artifact artifact : artifacts) {
            Manifest manifest = getManifest(artifact);
            if (manifest != null) {
                createLinkFile(artifact, manifest);
            }
        }
        // add output directory as test resource directory
        Resource resource = new Resource();
        resource.setDirectory(outputDirectory.toString());
        build.addTestResource(resource);
    }

    private void createLinkFile(Artifact artifact, Manifest manifest) throws MojoExecutionException {
        String symbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
        if (symbolicName != null) {
            int idx = symbolicName.indexOf(';');
            if (idx != -1) {
                symbolicName = symbolicName.substring(0, idx);
            }
            File linkFile = new File(outputDirectory, symbolicName.trim() + ".link");
            try {
                PrintWriter out = new PrintWriter(new FileOutputStream(linkFile), false);
                try {
                    out.write(artifact.getFile().toURI().toString());
                }
                finally {
                    out.close();
                }
            }
            catch (IOException ex) {
                throw new MojoExecutionException("Failed to create " + linkFile, ex);
            }
        }
    }

    private Manifest getManifest(Artifact artifact) {
        Manifest manifest = null;
        try {
            InputStream in = new FileInputStream(artifact.getFile());
            try {
                ZipInputStream zip = new ZipInputStream(in);
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                        manifest = new Manifest(zip);
                        break;
                    }
                }
            }
            finally {
                in.close();
            }
        }
        catch (IOException ex) {
            getLog().error("Unable to read " + artifact.getFile(), ex);
        }
        return manifest;
    }
}
