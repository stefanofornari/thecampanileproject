/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.campanile;

/**
 *
 * @author ste
 */
public interface User {
    
    /**
     * @see ORM::delete()
     */
    public void delete(String id);
    
    /**
     * Return the best version of the user's name.  Either their specified full name, or fall back
     * to the user name.
     * @return string
     */
    public String display_name();
}
