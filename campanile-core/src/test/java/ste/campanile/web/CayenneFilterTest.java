/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.campanile.web;


import org.apache.cayenne.ObjectContext;
import javax.servlet.ServletContext;
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
public class CayenneFilterTest {
    
    private CayenneFilter filter;
    private ServletContext servletContext;
    
    public CayenneFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        filter = new CayenneFilter();
        servletContext = new ServletContext();
    }
    
    @After
    public void tearDown() {
    }
    
    /**
     * Test of doFilter method, of class CayenneFilter.
     */
    @Test
    public void testDoFilter() throws Exception {
        ObjectContext context = 
            (ObjectContext)servletContext.getAttribute(CayenneFilter.ATTRIBUTE_CAYENNE_CONTEXT);
        
        assertNull(context);
        
        filter.doFilter(null, null, null);
        
        context = (ObjectContext)servletContext.getAttribute(CayenneFilter.ATTRIBUTE_CAYENNE_CONTEXT);
        
        assertNotNull(context);
    }

    /**
     * Test of destroy method, of class CayenneFilter.
     */
    @Test
    public void testDestroy() {
        System.out.println("destroy");
        CayenneFilter instance = new CayenneFilter();
        instance.destroy();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
