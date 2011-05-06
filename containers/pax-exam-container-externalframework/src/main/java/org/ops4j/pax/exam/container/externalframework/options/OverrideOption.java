package org.ops4j.pax.exam.container.externalframework.options;


import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.url;

import java.util.regex.Pattern;

import org.ops4j.pax.exam.Option;
/**
 * For overriden or remove some option (default option)
 * @author chomats
 *
 */
public class OverrideOption implements Option {
   
    private static class CST implements Option {};
    
    public final static Option IGNORE = new CST();
    
    public OverrideOption() {}
    
    /**
     * 
     * @param option an option
     * @return IGNORE if this option is ignored by this match and can be keep
     *         null if this option must be removed
     *         a new version if this option must be overridden.
     */
    public Option match(Option option) {
        return IGNORE;
    }
    
    public static OverrideOption overrideRBC(int level) {
        return new OverrideUrlProvisionOption().setUrlPattern(Pattern.quote(
                "link:classpath:META-INF/links/org.ops4j.pax.exam.rbc.link")).setLevel(level);
    }
    
    public static OverrideOption overrideExtenderService(int level) {
        return new OverrideUrlProvisionOption().setUrlPattern(Pattern.quote(
                "link:classpath:META-INF/links/org.ops4j.pax.extender.service.link")).setLevel(level);
    }
   
    public static OverrideOption overrideOsgiCompendium(int level) {
        return new OverrideUrlProvisionOption().setUrlPattern(Pattern.quote(
                "link:classpath:META-INF/links/org.osgi.compendium.link")).setLevel(level);
    }
    
    public static OverrideOption overridePaxLoggingApi(int level) {
        return new OverrideUrlProvisionOption().setUrlPattern(Pattern.quote(
                "link:classpath:META-INF/links/org.ops4j.pax.logging.api.link")).setLevel(level);
    }
}
