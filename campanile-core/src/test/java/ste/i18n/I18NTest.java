/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.i18n;

import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    
    public final static String TEST_EXISTING_KEY = "This key exists";
    public final static String TEST_NOT_EXISTING_KEY = "This key does not exist";
    public final static String TEST_TRANSLATION = "Questa chiave esiste";
    
    
    public I18NTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testConstructorFail() throws Exception {
        try {
            I18N i18n = new I18N("notexistingfile-en_US", Locale.getDefault());
            fail("the file must exist");
        } catch (FileNotFoundException e) {
            //
            // OK
            //
        }
    }
    
    @Test
    public void testConstructorOK() throws Exception {
        new I18N("src/test/webapp/modules/amodule/test-en_US", Locale.getDefault());
    }
    
    @Test
    public void testTOK() throws Exception {
        I18N i18n = new I18N("src/test/webapp/modules/amodule/test", Locale.ITALY);
        
        assertEquals(TEST_TRANSLATION, i18n.t(TEST_EXISTING_KEY));
        assertEquals(TEST_NOT_EXISTING_KEY, i18n.t(TEST_NOT_EXISTING_KEY));
    }
}
