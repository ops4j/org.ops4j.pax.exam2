package org.ops4j.pax.exam.quickbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import static junit.framework.Assert.*;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;

/**
 * Having a snapshot that has been produced for example as part of a regular maven run,
 * using this to do an actual quickbuild is simple.
 */
public class UpdateWithQuickbuildUsage
{

    public static void main( String... args )
    {
        try
        {
            Injector injector = Guice.createInjector( new DefaultQuickbuildModule() );

            SnapshotBuilder snapshotBuilder = injector.getInstance( Key.get( SnapshotBuilder.class ) );
            Quickbuild build = injector.getInstance( Key.get( Quickbuild.class ) );

            Snapshot snapshot = snapshotBuilder.load( new FileInputStream( CreateSnapshotUsage.SNAPSHOT ) );

            // get updated thing:
            InputStream result = build.update( snapshot, new File( CreateSnapshotUsage.FOLDER_OF_CHANGE ) );
            assertNotNull( result );

            // Done. Now we just save the build to make it visible on disk..
            Store<InputStream> store = injector.getInstance( injector.findBindingsByType( new TypeLiteral<Store<InputStream>>()
            {
            }
            ).get( 0 ).getKey()
            );
            Handle handle = store.store( result );

            System.out.println( "Result has been written to " + store.getLocation( handle ).toASCIIString() );
        } catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
