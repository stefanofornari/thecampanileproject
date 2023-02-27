package php.java.servlet.fastcgi;

import javax.servlet.http.HttpServletResponse;

import php.java.fastcgi.FCGIHeaderParser;
import php.java.servlet.fastcgi.FastCGIServlet.Environment;

public class FCGIServletHeaderParser extends FCGIHeaderParser {
    private HttpServletResponse response;
    private Environment env;

    public void addHeader(String key, String val) {
	response.addHeader(key, val);
    }

    public void parseHeader(String line) {
	try {
	    if (line.startsWith("Status")) {
		line = line.substring(line.indexOf(":") + 1).trim();
		int i = line.indexOf(' ');
		if (i > 0)
		    line = line.substring(0, i);

		response.setStatus(Integer.parseInt(line));
	    } else {
		if (!env.allHeaders.contains(line)) {
		    addHeader(line.substring(0, line.indexOf(":")).trim(),
		            line.substring(line.indexOf(":") + 1).trim());
		    env.allHeaders.add(line);
		}
	    }
	} catch (ArrayIndexOutOfBoundsException e) {
	    /* not a valid header */} catch (StringIndexOutOfBoundsException e) {
	    /* not a valid header */}
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public Environment getEnv() {
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }



}
