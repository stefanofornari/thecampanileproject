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

/**
 * This class represents a photo object.
 *
 * @author ste
 */
public class Photo {
    
    // ------------------------------------------------------------ Private data
    
    /**
     * Unique identifier
     */
    private int id;
    
    /**
     * Rating
     */
    private Star stars;

    // ------------------------------------------------------------ Constructors
    
    /**
     * Creates a Photo object from a unique id
     * 
     * @param id photo's unique identifier
     */
    public Photo(int id) {
        this.id = id;
    }
    
    // ---------------------------------------------------------- Public methods

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the stars
     */
    public Star getStars() {
        return stars;
    }

    /**
     * @param stars the stars to set
     */
    public void setStars(Star stars) {
        this.stars = stars;
    }
    
    public void unstar() {
        this.stars = Star.NONE;
    }
}
