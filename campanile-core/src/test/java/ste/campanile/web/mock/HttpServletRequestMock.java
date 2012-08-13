package ste.campanile.web.mock;

import javax.servlet.ServletContext;
import javax.servlet.http.*;


/**
 *
 * @author ste
 */
public class HttpServletRequestMock implements HttpServletRequest {
    
    public ServletContextMock context;
    
    public HttpServletRequestMock(ServletContextMock context) {
        this.context = context;
    }
    
    @Override
    public String getRequestURI() {
        return context.requestURI;
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

}
