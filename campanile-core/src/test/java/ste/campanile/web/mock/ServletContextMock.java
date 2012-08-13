package ste.campanile.web.mock;

import java.util.HashMap;
import javax.servlet.*;

/**
 *
 * @author ste
 */
public class ServletContextMock extends HashMap implements ServletContext  {
    public String requestURI = "";
    
    public Object getAttribute(String name) {
        return get(name);
    }
    
    public void setAttribute(String name, Object value) {
        put(name, value);
    }
}
