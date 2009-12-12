package org.ops4j.pax.exam.quickbuild;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Guice;

/**
 * @author Toni Menzel
 */
public class CreateSnapshotUsage
{

    public final static String REFERENCE_JAR = "/Users/tonit/devel/pax/runner/pax-runner-platform-felix/target/pax-runner-platform-felix-1.2.0-SNAPSHOT.jar";
    public final static String FOLDER_OF_CHANGE = "/Users/tonit/devel/pax/runner/pax-runner-platform-felix/target/classes";
    public final static String SNAPSHOT = "mysnapshot.snapshot";

    public static void main( String... args )
    {
        try
        {
            Injector injector = Guice.createInjector( new DefaultQuickbuildModule() );
            SnapshotBuilder snapshotBuilder = injector.getInstance( Key.get( SnapshotBuilder.class ) );

            FileInputStream referenceJar = new FileInputStream( new File( REFERENCE_JAR ) );
            File folder = new File( FOLDER_OF_CHANGE );

            Snapshot snapshot = snapshotBuilder.take( referenceJar,
                                                      folder
            );

            // save to disk
            File f = new File( SNAPSHOT );

            FileOutputStream fos = new FileOutputStream( f );
            snapshot.write( fos );

            System.out.println( "Snapshot written to " + f.getAbsolutePath() );
            //show me
            Runtime.getRuntime().exec( "mate " + f.getAbsolutePath() );
        } catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
