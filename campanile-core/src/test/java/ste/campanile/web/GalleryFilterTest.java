/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.campanile.web;

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
public class GalleryFilterTest {
    
    public GalleryFilter filter;
    
    public GalleryFilterTest() {
        filter = null;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        filter = new GalleryFilter();
    }
    
    @After
    public void tearDown() {
    }
    
    /** 
     * Per request preparation
     */
    @Test
    public void prepareRequest() {
    }
}
