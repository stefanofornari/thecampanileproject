/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.campanile;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
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
public class PhotoTest {
    private ItemTest itemTest = null;
    private Photo    photo    = null;
    
    public static final String OWNER_VALUE  = "John Doe";
    public static final int    RATING_VALUE = 3         ;
    
    public PhotoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        itemTest = new ItemTest();
        
        itemTest.properties.put(
            ItemTest.OWNER_FIELD, 
            Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {User.class}, new HandlerMock())
        );
        
        photo = new Photo(itemTest);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() throws Exception {
        Photo p = new Photo(itemTest);
        
        assertSame(itemTest, getItem(p));
    }
    
    @Test
    public void testOwnerNull() throws Exception {
        Photo p = new Photo(itemTest);
        
        itemTest.properties.put(Item.OWNER_FIELD, null);
        assertNull(p.getOwner());
    }
    
    @Test
    public void testOwner() throws Exception {
        assertEquals(OWNER_VALUE, photo.getOwner());
    }
    
    @Test
    public void testRating() throws Exception {
        photo.setRating(RATING_VALUE);
        
        assertEquals(RATING_VALUE, photo.getRating());
    }
    
    @Test
    public void testIsRated() throws Exception {
        assertFalse(photo.isRated());
        photo.setRating(RATING_VALUE);
        assertTrue(photo.isRated());     
    }
    
    // --------------------------------------------------------- Private methods
    
    private Item getItem(Photo p) throws Exception {
        Method m = p.getClass().getMethod("getItem");
        return (Item)m.invoke(p);
    }
    
    // ----------------------------------------------------------- Inner classes
    
    public class HandlerMock implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] os) throws Throwable {
            return OWNER_VALUE;
        }
    }
}
