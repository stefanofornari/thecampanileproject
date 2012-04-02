/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.i18n;

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
            I18N i18n = new I18N("notexistingfile.properties", Locale.getDefault());
            fail("the file must exist");
        } catch (FileNotFoundException e) {
            //
            // OK
            //
        }
    }
    
    @Test
    public void testConstructorOK() throws Exception {
        I18N i18n = new I18N("test.properties", Locale.getDefault());
    }
}
