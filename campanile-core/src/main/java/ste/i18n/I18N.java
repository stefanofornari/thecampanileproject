/*
 * The Campanile Project
 * Copyright (C) 2012 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */

package ste.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

/**
 * This class manages internationalization in an easy way. It is designed to be 
 * used in a JSP page simply using a EL expression like:
 * &lt;%=i18n['Hello world']%&gt
 * 
 * Language files are searched in the a file composed as follows:
 * 
 * <code>
 * $baseDir/$locale/messages
 * <code>
 * 
 * Where baseDir and locale are passed to the constructure.
 * 
 * The language file is reloaded only if changed since last read.
 * 
 * @author ste
 */
public class I18N extends HashMap<String, String> {
    
    private File messagesFile;
    private long lastReadMillis;
    
    public I18N(String baseDir, Locale locale) throws IOException {
        messagesFile = new File(baseDir + '/' + locale + "/messages");
        
        if (!messagesFile.exists()) {
            throw new FileNotFoundException(
                      "Language resource " + 
                      new File(baseDir).getAbsolutePath() + 
                      " not found for locale " 
                      + locale
           );
        }
        
        lastReadMillis = -1;
    }
    
    @Override
    public String get(Object key) {
        refresh();
        
        String value = super.get(key);
        
        return (value == null) ? (String)key : value;
    }
    
    // --------------------------------------------------------- Private methods
    
    private void refresh() throws RuntimeException {
        if (messagesFile.lastModified() == lastReadMillis) {
            return;
        }
        
        lastReadMillis = messagesFile.lastModified();
        Properties p = new Properties();
        try {
            p.loadFromXML(new FileInputStream(messagesFile));
        } catch (IOException e) {
            throw new RuntimeException("Error reading the messages file", e);
        }
        for(Object key: p.keySet()) {
            put((String)key, (String)p.get(key));
        }
    }
}
