/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 *
 * @author ste
 */
public class I18N {
    
    private Properties dictionary; 
    
    public I18N(String languageFile, Locale locale) throws IOException {
        File file = new File(languageFile + '-' + locale);
        
        if (!file.exists()) {
            throw new FileNotFoundException(
                      "Language resource " + 
                      new File(languageFile).getAbsolutePath() + 
                      " not found for locale " 
                      + locale
           );
        }
        
        dictionary = new Properties();
        dictionary.loadFromXML(new FileInputStream(file));
        
    }
    
    /**
     * Returns the translation for the given key if it exists, or the key itself
     * if not translations exists
     * 
     * @param key the key of the translation
     * 
     * @return the translation for the given key if it exists, or the key itself
     * if not translations exists
     */
    public String t(final String key) {
        return dictionary.getProperty(key, key);
    }
    
}
