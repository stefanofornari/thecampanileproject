/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ste.campanile;

/**
 *
 * @author ste
 */
public interface Item {
    
    public static final String OWNER_FIELD = "owner";
    
    
    public boolean viewable();
    public boolean is_photo();
    public boolean is_album();
    public void delete();
    public Item set_data_file(String path);
    public String url(String query);
    public String abs_url(String query);
    public String file_path();
    public String file_url(boolean full);
    public String thumb_path();
    public boolean hasThumb();
    public String thumb_url(boolean full);
    public String resize_path();
    public String resize_url(boolean full);
    public String relative_path();
    public String relative_url();
    public Item save();
    public Item album_cover();
    public Object __get(String column);
    
}