/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.i18n;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

/**
 *
 * @author ste
 */
public class I18N {
    
    public I18N(String languageFile, Locale locale) throws IOException {
        throw new FileNotFoundException(
                  "Language resource " + 
                  new File(languageFile).getAbsolutePath() + 
                  " not found for locale " 
                  + locale
       );
    }
    
}
