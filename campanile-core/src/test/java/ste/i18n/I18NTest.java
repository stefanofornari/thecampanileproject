/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        
        if (testIT.exists()) {
            testIT.delete();
        }
        
        testIT.getParentFile().mkdirs();
        Properties p = new Properties();
        p.put(TEST_EXISTING_KEY, TEST_TRANSLATION);
        
        p.storeToXML(new FileOutputStream(testIT), "");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        testIT.delete();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    private void addEntry() throws Exception {
        Properties p = new Properties();
        
        p.loadFromXML(new FileInputStream(testIT));
        p.put(TEST_NOT_EXISTING_KEY, "traduzione");
        p.storeToXML(new FileOutputStream(testIT), "");
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
