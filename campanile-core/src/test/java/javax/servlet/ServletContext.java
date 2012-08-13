package javax.servlet;

import java.util.HashMap;

/**
 *
 * @author ste
 */
public interface ServletContext {
    public Object getAttribute(String name);
    
    public void setAttribute(String name, Object value);
}
