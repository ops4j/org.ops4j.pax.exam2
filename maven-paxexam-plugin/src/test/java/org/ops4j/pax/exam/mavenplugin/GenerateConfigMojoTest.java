package org.ops4j.pax.exam.mavenplugin;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Toni Menzel (tonit)
 * @since Apr 27, 2009
 */
public class GenerateConfigMojoTest
{

    @Test
    public void testSettingsMatching()
    {
        assertEquals( "@42@bee",
                      new GenerateConfigMojo().getSettingsForArtifact( "cheese:ham@1@nope,foo:bar@42@bee,fuel:oil@1@xx",
                                                                       "foo", "bar"
                      )
        );

        assertEquals( "",
                      new GenerateConfigMojo().getSettingsForArtifact( "cheese:ham@1@nope,foo:bar@42@bee,fuel:oil@1@xx",
                                                                       "xx", "bb"
                      )
        );

        assertEquals( "",
                      new GenerateConfigMojo().getSettingsForArtifact( "cheese:ham@1@nope,foo:bar@42@bee,fuel:oil@1@xx",
                                                                       "cheese", "bb"
                      )
        );

        assertEquals( "@42@bee",
                      new GenerateConfigMojo().getSettingsForArtifact( "cheese:ham@1@nope,foo:bar@42@bee,fuel:oil@1@xx",
                                                                       "foo", "bar"
                      )
        );

        assertEquals( "",
                      new GenerateConfigMojo().getSettingsForArtifact(
                          "cheese:ham@1@nope,foo:bar@42@bee,fuel:oil@1@xx,ping:back",
                          "ping", "back"
                      )
        );
    }
}
