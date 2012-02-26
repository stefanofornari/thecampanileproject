/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.campanile;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ste
 */
public class ItemTest implements Item {
    
    public Map<String, Object> properties;
    
    public ItemTest() {
        properties =  new HashMap<String, Object>();
    }

    @Override
    public boolean viewable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean is_photo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean is_album() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item set_data_file(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String url(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String abs_url(String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String file_path() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String file_url(boolean full) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String thumb_path() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasThumb() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String thumb_url(boolean full) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String resize_path() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String resize_url(boolean full) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String relative_path() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String relative_url() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item save() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Item album_cover() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object __get(String column) {
        return properties.get(column);
    }
    
    
}
