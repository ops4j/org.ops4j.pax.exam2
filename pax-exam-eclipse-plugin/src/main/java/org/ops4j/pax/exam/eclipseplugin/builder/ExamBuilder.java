package org.ops4j.pax.exam.eclipseplugin.builder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ExamBuilder extends IncrementalProjectBuilder
{

    class SampleDeltaVisitor implements IResourceDeltaVisitor
    {
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
         */
        public boolean visit( IResourceDelta delta ) throws CoreException
        {
            IResource resource = delta.getResource();
            switch (delta.getKind())
            {
            case IResourceDelta.ADDED :
                // handle added resource
                checkXML( resource );
                break;
            case IResourceDelta.REMOVED :
                // handle removed resource
                break;
            case IResourceDelta.CHANGED :
                // handle changed resource
                checkXML( resource );
                break;
            }
            // return true to continue visiting children.
            return true;
        }
    }

    class SampleResourceVisitor implements IResourceVisitor
    {
        public boolean visit( IResource resource )
        {
            checkXML( resource );
            // return true to continue visiting children.
            return true;
        }
    }

    class XMLErrorHandler extends DefaultHandler
    {

        private IFile file;

        public XMLErrorHandler( IFile file )
        {
            this.file = file;
        }

        private void addMarker( SAXParseException e, int severity )
        {
            ExamBuilder.this.addMarker( file, e.getMessage(), e
                    .getLineNumber(), severity );
        }

        public void error( SAXParseException exception ) throws SAXException
        {
            addMarker( exception, IMarker.SEVERITY_ERROR );
        }

        public void fatalError( SAXParseException exception ) throws SAXException
        {
            addMarker( exception, IMarker.SEVERITY_ERROR );
        }

        public void warning( SAXParseException exception ) throws SAXException
        {
            addMarker( exception, IMarker.SEVERITY_WARNING );
        }
    }

    public static final String BUILDER_ID = "org.ops4j.pax.exam.eclipseplugin.examBuilder";

    static final String MARKER_TYPE = "org.ops4j.pax.exam.eclipseplugin.problemMarker";

    private SAXParserFactory parserFactory;

    private void addMarker( IFile file, String message, int lineNumber,
            int severity )
    {
        try
        {
            IMarker marker = file.createMarker( MARKER_TYPE );
            marker.setAttribute( IMarker.MESSAGE, message );
            marker.setAttribute( IMarker.SEVERITY, severity );
            if ( lineNumber == -1 )
            {
                lineNumber = 1;
            }
            marker.setAttribute( IMarker.LINE_NUMBER, lineNumber );
        } catch ( CoreException e )
        {
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
     * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    protected IProject[] build( int kind, Map args, IProgressMonitor monitor )
            throws CoreException
    {
        final IProject project = getProject();
        project.deleteMarkers( MARKER_TYPE, false, IResource.DEPTH_INFINITE );
        IJavaProject p = JavaCore.create( project );
        Set<IClasspathEntry> compete = new HashSet<IClasspathEntry>();
        
        for ( IClasspathEntry s : p.getResolvedClasspath( true ) )
        {
           if (s.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
                IPath path = s.getPath();
                String portable = path.toPortableString();
               
                if ( portable.contains( "pax-exam-container-paxrunner" ) || portable.contains("pax-exam-container-native") )
                {
                   compete.add(s);
                }
           }
        }
        
        if (compete.size() == 0) {
            IMarker m = project.createMarker( MARKER_TYPE );
            m.setAttribute( IMarker.MESSAGE, "No TestContainer implementation found in classpath. You should add one!" );
            m.setAttribute( IMarker.SEVERITY, IMarker.SEVERITY_ERROR );
        }else if (compete.size() > 1) {
            
            for (IClasspathEntry com : compete) {
                IMarker m = project.createMarker( MARKER_TYPE );
                m.setAttribute( IMarker.MESSAGE, "Superfluous Test Container in Classpath: " + com.getPath().toPortableString() );
                m.setAttribute( IMarker.SEVERITY, IMarker.SEVERITY_ERROR );
            }
           
        }

        /**
         * if ( kind == FULL_BUILD )
         * {
         * fullBuild( monitor );
         * } else
         * {
         * IResourceDelta delta = getDelta( getProject() );
         * if ( delta == null )
         * {
         * fullBuild( monitor );
         * } else
         * {
         * incrementalBuild( delta, monitor );
         * }
         * }
         **/
        return null;
    }

    void checkXML( IResource resource )
    {
        if ( resource instanceof IFile && resource.getName().endsWith( ".xml" ) )
        {
            IFile file = ( IFile ) resource;
            deleteMarkers( file );
            XMLErrorHandler reporter = new XMLErrorHandler( file );
            try
            {
                getParser().parse( file.getContents(), reporter );
            } catch ( Exception e1 )
            {
            }
        }
    }

    private void deleteMarkers( IFile file )
    {
        try
        {
            file.deleteMarkers( MARKER_TYPE, false, IResource.DEPTH_ZERO );
        } catch ( CoreException ce )
        {
        }
    }

    protected void fullBuild( final IProgressMonitor monitor )
            throws CoreException
    {
        try
        {
            getProject().accept( new SampleResourceVisitor() );
        } catch ( CoreException e )
        {
        }
    }

    private SAXParser getParser() throws ParserConfigurationException,
            SAXException
    {
        if ( parserFactory == null )
        {
            parserFactory = SAXParserFactory.newInstance();
        }
        return parserFactory.newSAXParser();
    }

    protected void incrementalBuild( IResourceDelta delta,
            IProgressMonitor monitor ) throws CoreException
    {
        // the visitor does the work.
        delta.accept( new SampleDeltaVisitor() );
    }
}
