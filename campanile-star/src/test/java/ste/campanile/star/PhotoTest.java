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
package ste.campanile.star;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ste
 */
public class PhotoTest {
    
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
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void constructorAndId() {
        Photo p = new Photo(1); Assert.assertEquals(1, p.getId());
        
        p = new Photo(2); Assert.assertEquals(2, p.getId());
    }
    
    @Test
    public void starring() {
        Photo p = new Photo(1);
        
        p.setStars(Star.ONE); Assert.assertEquals(Star.ONE, p.getStars());
        p.setStars(Star.TWO); Assert.assertEquals(Star.TWO, p.getStars());
    }
    
    @Test
    public void unstarring() {
        Photo p = new Photo(1);
        
        p.setStars(Star.ONE);
        
        p.unstar();
        Assert.assertEquals(Star.NONE, p.getStars());
        
        p.setStars(Star.NONE);
        Assert.assertEquals(Star.NONE, p.getStars());
    }
}
