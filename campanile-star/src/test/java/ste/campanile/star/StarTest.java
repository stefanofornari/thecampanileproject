/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.campanile.star;

import junit.framework.Assert;
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
public class StarTest {
    
    public StarTest() {
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
    public void all() {
        Assert.assertEquals(1, Star.ONE.getValue());
        Assert.assertEquals(2, Star.TWO.getValue());
        Assert.assertEquals(0, Star.NONE.getValue());
        
        Assert.assertEquals(Star.ONE, Star.lookup(1));
        
        try {
           Star.lookup(10); 
           Assert.fail("lookup should have thrown an exception");
        } catch (IllegalArgumentException e) {
            //
            // OK
            //
        }    
    }
}
