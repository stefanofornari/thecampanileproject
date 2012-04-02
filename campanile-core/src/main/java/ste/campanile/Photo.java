/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.campanile;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 *
 * @author ste
 */
public class Photo {
    private final String METHOD_DISPLAY_NAME = "display_name";
    
    public final int RATE_UNRATED = -1;
    
    private Item item;
    private int rating;
    
    // ------------------------------------------------------------ Constructors
    
    public Photo(Item item) {
        this.item = item;
        this.rating = RATE_UNRATED;  // not rated
    }
    
    // ---------------------------------------------------------- Public methods
    
    /**
     * 
     * @return the owner's display name or null if it cannot be found
     */
    public String getOwner() {
        Proxy o = (Proxy)item.__get(Item.OWNER_FIELD);
        
        if (o == null) {
            return null;
        }
        
        String displayName = null;
        try {
            InvocationHandler invoker = Proxy.getInvocationHandler(o);
            Method m = User.class.getMethod(METHOD_DISPLAY_NAME);
            displayName = (String)invoker.invoke(o, m, new Object[0]);
        } catch (Throwable e) {
            // Nothing we can do about it..
            e.printStackTrace();
        }
        
        return displayName;
    }

    // ------------------------------------------------------- Protected methods
    /**
     * 
     * @return the inner item
     */
    public Item getItem() {
        return item;
    }

    /**
     * @return the rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * @param rating the rating to set
     */
    public void setRating(int rating) {
        this.rating = rating;
    }
    
    /**
     * 
     * @return true if the photo has been rated, false otherwise
     */
    public boolean isRated() {
        return (rating != RATE_UNRATED);
    }
}
