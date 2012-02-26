/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.campanile;

import java.lang.reflect.Method;

/**
 *
 * @author ste
 */
public class Test {
    
    public Test() {
        
    }
    
    public void test(Object o) {
        if (o != null) {
            Item i = (Item)o;
            for (Method m: o.getClass().getMethods()) {
                System.out.println(">>> " + m.toGenericString()); 
            }
            System.out.println(">> " + i.is_photo()); 
        }
    }
    
}
