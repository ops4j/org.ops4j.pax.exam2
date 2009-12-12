package org.ops4j.pax.exam.growl.internal;

import org.apache.commons.logging.Log;
import org.ops4j.pax.exam.growl.GrowlFactory;
import org.ops4j.pax.exam.growl.GrowlService;

/**
 * @author Toni Menzel
 */
public class GrowlLogger implements Log
{

    private Log m_parent;
    private GrowlService m_service;
    public final static int GROWL_DEBUG = 1;
    public final static int GROWL_INFO = 2;
    public final static int GROWL_ERROR = 4;
    public final static int GROWL_FATAL = 8;
    private String m_title;
    private int m_mask;

    public GrowlLogger( Log parent, String title, int delegationMask )
    {
        m_parent = parent;
        m_service = GrowlFactory.getService();
        m_mask = delegationMask;
        m_title = title;
    }

    public void debug( Object o )
    {
        m_parent.debug( o );
        if( isDebugEnabled() && ( m_mask % GROWL_DEBUG == 0 ) )
        {
            m_service.send( m_title, o.toString() );
        }
    }

    public void debug( Object o, Throwable throwable )
    {
        m_parent.debug( o, throwable );
        if( isDebugEnabled() && ( m_mask % GROWL_DEBUG == 0 ) )
        {
            m_service.send( m_title, o.toString() );
        }
    }

    public void error( Object o )
    {
        m_parent.error( o );
        if( isErrorEnabled() && ( m_mask % GROWL_ERROR == 0 ) )
        {
            m_service.send( m_title, o.toString() );
        }
    }

    public void error( Object o, Throwable throwable )
    {
        m_parent.error( o, throwable );
        if( isErrorEnabled() && ( m_mask % GROWL_ERROR == 0 ) )
        {
            m_service.send( m_title, o.toString() );
        }
    }

    public void fatal( Object o )
    {
        m_parent.fatal( o );
        if( isFatalEnabled() && ( m_mask % GROWL_FATAL == 0 ) )
        {
            m_service.send( m_title, o.toString() );
        }
    }

    public void fatal( Object o, Throwable throwable )
    {
        m_parent.fatal( o, throwable );
        if( isFatalEnabled() && ( m_mask % GROWL_FATAL == 0 ) )
        {
            m_service.send( m_title, o.toString() );
        }
    }

    public void info( Object o )
    {
        m_parent.fatal( o );
        if( isInfoEnabled() && ( m_mask % GROWL_INFO == 0 ) )
        {
            m_service.send( m_title, o.toString() );
        }
    }

    public void info( Object o, Throwable throwable )
    {
        m_parent.info( o, throwable );
        if( isInfoEnabled() && ( m_mask % GROWL_INFO == 0 ) )
        {
            m_service.send( m_title, o.toString() );
        }
    }

    public boolean isDebugEnabled()
    {
        return m_parent.isDebugEnabled();
    }

    public boolean isErrorEnabled()
    {
        return m_parent.isErrorEnabled();
    }

    public boolean isFatalEnabled()
    {
        return m_parent.isFatalEnabled();
    }

    public boolean isInfoEnabled()
    {
        return m_parent.isInfoEnabled();
    }

    public boolean isTraceEnabled()
    {
        return m_parent.isTraceEnabled();
    }

    public boolean isWarnEnabled()
    {
        return m_parent.isWarnEnabled();
    }

    public void trace( Object o )
    {
        m_parent.trace( o );
    }

    public void trace( Object o, Throwable throwable )
    {
        m_parent.trace( o, throwable );
    }

    public void warn( Object o )
    {
        m_parent.warn( o );
    }

    public void warn( Object o, Throwable throwable )
    {
        m_parent.warn( o, throwable );
    }

   
}
