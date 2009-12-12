package org.ops4j.pax.exam.quickbuild;

import java.io.InputStream;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import org.ops4j.pax.exam.quickbuild.internal.DefaultQuickbuild;
import org.ops4j.pax.exam.quickbuild.internal.DefaultSnapshotBuilder;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

/**
 * Default module for using Quickbuild.
 */
public class DefaultQuickbuildModule extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind( Quickbuild.class ).to( DefaultQuickbuild.class );
        bind( SnapshotBuilder.class ).to( DefaultSnapshotBuilder.class );
        bind( new TypeLiteral<Store<InputStream>>()
        {
        }
        ).toInstance( StoreFactory.sharedLocalStore() );
    }
}
