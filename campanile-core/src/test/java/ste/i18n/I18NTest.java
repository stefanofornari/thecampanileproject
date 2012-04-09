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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ste
 */
public class I18NTest {
    
    public final static String TEST_PATH             = "target/test";
    public final static String TEST_NAME             = "messages";
    public final static String TEST_EXISTING_KEY     = "This key exists";
    public final static String TEST_NOT_EXISTING_KEY = "This key does not exist";
    public final static String TEST_TRANSLATION      = "Questa chiave esiste";
    
    public final static File   testIT
        = new File(TEST_PATH + '/' + Locale.ITALY + '/' + TEST_NAME);
   
    public I18NTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception { 
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
  
    }
    
    @Before
    public void setUp() throws Exception {
        if (testIT.exists()) {
            testIT.delete();
        }
        
        testIT.getParentFile().mkdirs();
        Properties p = new Properties();
        p.put(TEST_EXISTING_KEY, TEST_TRANSLATION);
        
        p.storeToXML(new FileOutputStream(testIT), "");
    }
    
    @After
    public void tearDown() throws Exception {
        testIT.delete();
    }
    
    private void addEntry() throws Exception {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        
        Properties p = new Properties();
        
        p.loadFromXML(fis = new FileInputStream(testIT));
        fis.close();
        p.put(TEST_NOT_EXISTING_KEY, "traduzione");
        p.storeToXML(fos = new FileOutputStream(testIT), "");
        fos.close(); fos.close();
        Thread.sleep(1000);
        testIT.setLastModified(System.currentTimeMillis()); 
    }

    @Test
    public void testConstructorFail() throws Exception {
        try {
            I18N i18n = new I18N("notexistingfile", Locale.getDefault());
            fail("the file must exist");
        } catch (FileNotFoundException e) {
            //
            // OK
            //
        }
    }
    
    @Test
    public void testConstructorOK() throws Exception {
        new I18N(TEST_PATH, Locale.ITALY);
    }
    
    @Test
    public void testGet() throws Exception {
        I18N i18n = new I18N(TEST_PATH, Locale.ITALY);
        
        assertEquals(TEST_TRANSLATION, i18n.get(TEST_EXISTING_KEY));
        assertEquals(TEST_NOT_EXISTING_KEY, i18n.get(TEST_NOT_EXISTING_KEY));
    }
    
    @Test
    /**
     *  Read the language file only once
     */
    public void testNotReload() throws Exception {
        long millis = testIT.lastModified();
        I18N i18n = new I18N(TEST_PATH, Locale.ITALY);
        assertEquals(TEST_NOT_EXISTING_KEY, i18n.get(TEST_NOT_EXISTING_KEY));
        addEntry();
        testIT.setLastModified(millis); 
        assertEquals(TEST_NOT_EXISTING_KEY, i18n.get(TEST_NOT_EXISTING_KEY));
    }
    
    @Test
    /**
     *  Re-read the language file if modified
     */
    public void testReload() throws Exception {
        I18N i18n = new I18N(TEST_PATH, Locale.ITALY);
        assertEquals(TEST_NOT_EXISTING_KEY, i18n.get(TEST_NOT_EXISTING_KEY));
        addEntry();
        assertFalse(TEST_NOT_EXISTING_KEY.equals(i18n.get(TEST_NOT_EXISTING_KEY)));
    }
}
