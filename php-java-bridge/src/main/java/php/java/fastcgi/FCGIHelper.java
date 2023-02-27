package php.java.fastcgi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import php.java.bridge.Util;
import php.java.bridge.util.Logger;

public class FCGIHelper {
    protected int phpFcgiConnectionPoolSize;
    protected int phpFcgiConnectionPoolTimeout;

    protected boolean phpTryOtherLocations;
    protected boolean preferSystemPhp;
    protected boolean phpIncludeJava;
    protected boolean phpIncludeDebugger;
    protected int phpFcgiMaxRequests;
    protected boolean promiscuous;
    protected String socketPort;
    protected String cgiDir;
    protected String pearDir;
    protected String webInfDir;

    protected String php;

    
    public FCGIHelper() {
	phpFcgiConnectionPoolSize = Integer.parseInt(System.getProperty("php.java.bridge.php_fcgi_connection_pool_size",FCGIUtil.PHP_FCGI_CONNECTION_POOL_SIZE));
	phpFcgiConnectionPoolTimeout = Integer.parseInt(System.getProperty("php.java.bridge.php_fcgi_connection_pool_timeout", FCGIUtil.PHP_FCGI_CONNECTION_POOL_TIMEOUT));
	phpFcgiMaxRequests = Integer.parseInt(System.getProperty("php.java.bridge.php_fcgi_max_requests",FCGIUtil.PHP_FCGI_MAX_REQUESTS));
	phpTryOtherLocations = Util.PHP_EXEC == null;
	preferSystemPhp = "true".equalsIgnoreCase(System.getProperty("php.java.bridge.prefer_system_php_exec", "true"));
	php = Util.PHP_EXEC == null?"php-cgi":Util.PHP_EXEC;
	phpIncludeJava = "true".equalsIgnoreCase(System.getProperty("php.java.bridge.php_include_java", "true"));
	phpIncludeDebugger = "true".equalsIgnoreCase(System.getProperty("php.java.bridge.php_include_debugger", "false"));
	promiscuous = Util.JAVABRIDGE_PROMISCUOUS;
	cgiDir = null;
	pearDir = webInfDir = null;
	
	socketPort= System.getProperty("php.java.bridge.php_fcgi_external_socket_pool");
    }

    
    public String getSocketPort() {
        return socketPort;
    }

    public boolean isExternalFCGIPool() {
        return socketPort!=null && !"true".equalsIgnoreCase(socketPort);
    }

    public boolean isInternalDefaultPort() {
        return socketPort!=null && "true".equalsIgnoreCase(socketPort);
    }


    public int getPhpFcgiConnectionPoolSize() {
	return phpFcgiConnectionPoolSize;
    }

    public int getPhpFcgiConnectionPoolTimeout() {
	return phpFcgiConnectionPoolTimeout;
    }

    
    public boolean isPhpTryOtherLocations() {
	return phpTryOtherLocations;
    }

    public boolean isPreferSystemPhp() {
	return preferSystemPhp;
    }

    public String getPhp() {
	return php;
    }

    public boolean isPhpIncludeJava() {
	return phpIncludeJava;
    }

    public boolean isPhpIncludeDebugger() {
	return phpIncludeDebugger;
    }

    public int getPhpFcgiMaxRequests() {
	return phpFcgiMaxRequests;
    }

    public boolean isPromiscuous() {
	return promiscuous;
    }
    public void createLauncher(File cgiOsDir) {
	File javaIncFile = new File(cgiOsDir, "launcher.sh");
	if (Util.USE_SH_WRAPPER) {
	    try {
		if (!javaIncFile.exists() || javaIncFile.length()==0) {
		    Field f = Util.LAUNCHER_UNIX.getField("bytes");
		    byte[] buf = (byte[]) f.get(Util.LAUNCHER_UNIX);
		    OutputStream out = new FileOutputStream(javaIncFile);
		    out.write(buf);
		    out.close();
		}
	    } catch (Exception e) {
		Logger.printStackTrace(e);
	    }
	}
	File javaProxyFile = new File(cgiOsDir, "launcher.exe");
	if (!Util.USE_SH_WRAPPER) {
	    try {
		if (!javaProxyFile.exists() || javaProxyFile.length()==0) {
		    OutputStream out = new FileOutputStream(javaProxyFile);
		    for (Class c : new Class[] { Util.LAUNCHER_WINDOWS,
		            Util.LAUNCHER_WINDOWS2, Util.LAUNCHER_WINDOWS3,
		            Util.LAUNCHER_WINDOWS4, Util.LAUNCHER_WINDOWS5,
		            Util.LAUNCHER_WINDOWS6, Util.LAUNCHER_WINDOWS7 }) {
			if (c != null) {
			    Field f = c.getField("bytes");
			    byte[] buf = (byte[]) f.get(c);
			    out.write(buf);
			}
		    }
		    out.close();
		    if (javaProxyFile.length()==0) throw new IllegalStateException("LAUNCHER_WINDOWS class missing");
		}
	    } catch (Exception e) {
		Logger.printStackTrace(e);
	    }
	}
    }


    public String getCgiDir() {
	return cgiDir;
    }


    public void setCgiDir(String cgiDir) {
	this.cgiDir = cgiDir;
    }


    public String getPearDir() {
	return pearDir;
    }


    public void setPearDir(String pearDir) {
	this.pearDir = pearDir;
    }


    public String getWebInfDir() {
	return webInfDir;
    }


    public void setWebInfDir(String webInfDir) {
	this.webInfDir = webInfDir;
    }



}
