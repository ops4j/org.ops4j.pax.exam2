package org.ops4j.pax.exam.container.externalframework.options;

import java.util.regex.Pattern;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlProvisionOption;

final public class OverrideUrlProvisionOption extends OverrideOption {
    int level = 0;
    String  urlPattern;
    
    @Override
    public Option match(Option option) {
       if (option instanceof UrlProvisionOption) {
           UrlProvisionOption url = (UrlProvisionOption) option;
           if (Pattern.matches(urlPattern, url.getURL())) {
               if (level>0) {
                   url.startLevel(level);
                   return url;
               }
               return null;
           }
       }
        return IGNORE;
    }
    
    public OverrideUrlProvisionOption setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
        return this;
    }
    
    public OverrideUrlProvisionOption setLevel(int level) {
        this.level = level;
        return this;
    }
}