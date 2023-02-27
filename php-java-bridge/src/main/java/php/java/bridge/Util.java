/*-*- mode: Java; tab-width:8 -*-*/

package php.java.bridge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import php.java.bridge.util.Logger;

/*
 * Copyright (C) 2003-2007 Jost Boekemeier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER(S) OR AUTHOR(S) BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

public final class Util {

    static {
        initGlobals();
    }


    /** Used by the watchdog. After MAX_WAIT (default 1500ms) the ContextRunner times out. Raise this value if you want to debug the bridge.
     * See also system property <code>php.java.bridge.max_wait</code>
     */
    public static int MAX_WAIT;

    /** Used by the launcher.exe for CreateProcess. This should be 16777216 (CREATE_BREAKAWAY_FROM_JOB) in order to let php-cgi.exe create a new job object.
     * Once the main php-cgi process is killed all sub processes (see PHP_FCGI_CHILDREN) go away, too.
     */
    public static String LAUNCHER_FLAGS;

    /** The java/Java.inc code */
    public static Class JAVA_INC;
    /** The java/Java.inc code */
    public static Class PHPDEBUGGER_PHP;
    /** The launcher.sh code */
    public static Class LAUNCHER_UNIX;
    /** The launcher.exe code */
    public static Class LAUNCHER_WINDOWS, LAUNCHER_WINDOWS2, LAUNCHER_WINDOWS3, LAUNCHER_WINDOWS4, LAUNCHER_WINDOWS5, LAUNCHER_WINDOWS6, LAUNCHER_WINDOWS7;
    /** Only for internal use */
    public static final byte HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /** True if /bin/sh exists, false otherwise */
    public static final boolean USE_SH_WRAPPER = new File("/bin/sh").exists();
    
    /** The PHP argument allow_url_include=On, passed to all JSR223 script engines */
    public static final String[] ALLOW_URL_INCLUDE = {"-d", "allow_url_include=On"};

    /** Used to re-direct back to the current VM */
    public static final String X_JAVABRIDGE_OVERRIDE_HOSTS = "X_JAVABRIDGE_OVERRIDE_HOSTS";

    /** The standard Context ID used by the ContextFactory */
    public static final String X_JAVABRIDGE_CONTEXT = "X_JAVABRIDGE_CONTEXT";

    public static final String X_JAVABRIDGE_OVERRIDE_HOSTS_REDIRECT = "X_JAVABRIDGE_OVERRIDE_HOSTS_REDIRECT";

    public static final String X_JAVABRIDGE_REDIRECT = "X_JAVABRIDGE_REDIRECT";

    public static final String X_JAVABRIDGE_INCLUDE = "X_JAVABRIDGE_INCLUDE";

    public static final String X_JAVABRIDGE_INCLUDE_ONLY = "X_JAVABRIDGE_INCLUDE_ONLY";
    
    private Util() {}
    
    private static DateFormat formatter;
    
    /** 
     * The default PHP arguments. Can be passed via -Dphp.java.bridge.php_exec_args=list of urlencoded strings separated by space
     * Default: "-d display_errors=Off -d log_errors=On -d java.persistent_servlet_connections=On"
     */
    public static String[] PHP_ARGS;
    private static String DEFAULT_PHP_ARGS;
    
    /**
     * The default CGI locations: <code>"/usr/bin/php-cgi"</code>, <code>"c:/Program Files/PHP/php-cgi.exe</code>
     */
    public static String DEFAULT_CGI_LOCATIONS[];

    /**
     * ASCII encoding
     */
    public static final String ASCII = "ASCII";

    /**
     * UTF8 encoding
     */
    public static final String UTF8 = "UTF-8";

    /**
     * DEFAULT currently UTF-8, will be changed when most OS support and use UTF-16.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The default buffer size
     */
    public static final int BUF_SIZE = 8192;

    /** Environment entries which should NOT be passed to PHP. For example PHPRC, which is set by some broken PHP installers */
    
    public static List ENVIRONMENT_BLACKLIST;
    /**
     * A map containing environment values not in ENVIRONMENT_BLACKLIST. At least:
     * "PATH", "LD_LIBRARY_PATH", "LD_ASSUME_KERNEL", "USER", "TMP", "TEMP", "HOME", "HOMEPATH", "LANG", "TZ", "OS"
     * They can be set with e.g.: <code>java -DPATH="$PATH" -DHOME="$HOME" -jar JavaBridge.jar</code> or
     * <code>java -DPATH="%PATH%" -jar JavaBridge.jar</code>. 
     */
    public static HashMap COMMON_ENVIRONMENT;

    /**
     * The default extension directories. If one of the directories
     * "/usr/share/java/ext", "/usr/java/packages/lib/ext" contains
     * java libraries, the bridge loads these libraries automatically.
     * Useful if you have non-pure java libraries (=libraries which
     * use the Java Native Interface to load native dll's or shared
     * libraries).
     */
    public static final String DEFAULT_EXT_DIRS[] = { "/usr/share/java/ext", "/usr/java/packages/lib/ext" };
    
    
    /** Set to true if the VM is gcj, false otherwise */
    public static final boolean IS_GNU_JAVA = checkVM();

    /**
     * The name of the extension, usually "JavaBridge" or "MonoBridge"
     */
    public static String EXTENSION_NAME;

    /**
     * The max. number of threads in the thread pool. Default is 20.
     * @see System property <code>php.java.bridge.threads</code>
     */
    public static String THREAD_POOL_MAX_SIZE;
    
    /**
     * The default log level, java.log_level from php.ini
     * overrides. Default is 3, if started via java -jar
     * JavaBridge.jar or 2, if started as a sub-process of Apache/IIS.
     * @see System property <code>php.java.bridge.default_log_level</code>
     */
    public static int DEFAULT_LOG_LEVEL;

    /**
     * Backlog for TCP and unix domain connections.
      */
    public static final int BACKLOG = 20;

    /** Only for internal use */
    public static final Object[] ZERO_ARG = new Object[0];

    /** Only for internal use */
    public static final Class[] ZERO_PARAM = new Class[0];
    
    /** Only for internal use */
    public static final byte[] RN = Util.toBytes("\r\n");

    public static File TMPDIR;
    
    /** The name of the VM, for example "1.4.2@http://java.sun.com/" or "1.4.2@http://gcc.gnu.org/java/".*/
    public static String VM_NAME;
    /**
     * Set to true, if the Java VM has been started with -Dphp.java.bridge.promiscuous=true;
     */
    public static boolean JAVABRIDGE_PROMISCUOUS;

    /**
     * The default log file. Default is stderr, if started as a
     * sub-process of Apache/IIS/Eclipse or <code>EXTENSION_NAME</code>.log,
     * if started via java -jar JavaBridge.jar.
     * @see System property <code>php.java.bridge.default_log_file</code>
     */
    public static String DEFAULT_LOG_FILE;

    static boolean DEFAULT_LOG_FILE_SET;
    
    /** The base directory of the PHP/Java Bridge. Usually /usr/php/modules/ or $HOME  */
    public static String JAVABRIDGE_BASE;

    /** The library directory of the PHP/Java Bridge. Usually /usr/php/modules/lib or $HOME/lib */
    public static String JAVABRIDGE_LIB;
    
    private static String getProperty(Properties p, String key, String defaultValue) {
	String s = null;
	if(p!=null) s = p.getProperty(key);
	if(s==null) s = System.getProperty("php.java.bridge." + String.valueOf(key).toLowerCase());
	if(s==null) s = defaultValue;
	return s;
    }
    /** Only for internal use */
    public static String VERSION;
    /** Only for internal use */
    public static String osArch;
    /** Only for internal use */
    public static String osName;
    /** Only for internal use */
    public static String PHP_EXEC;
    /** Only for internal use */
    public static File HOME_DIR;

    public static String sessionSavePath;

    private static void initGlobals() {
	
	formatter = SimpleDateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG, Locale.ENGLISH);
	formatter.setTimeZone(TimeZone.getTimeZone("GMT"));


	try {
	    JAVA_INC = Class.forName("php.java.bridge.generated.JavaInc");
	} catch (Exception e) {/*ignore*/}
	try {
	    PHPDEBUGGER_PHP = Class.forName("php.java.bridge.generated.PhpDebuggerPHP");
	} catch (Exception e) {/*ignore*/}
	try {
	    LAUNCHER_UNIX = Class.forName("php.java.bridge.generated.LauncherUnix");
	} catch (Exception e) {/*ignore*/}
	try {
	    LAUNCHER_WINDOWS = Class.forName("php.java.bridge.generated.LauncherWindows");
	    LAUNCHER_WINDOWS2 = Class.forName("php.java.bridge.generated.LauncherWindows2");
	    LAUNCHER_WINDOWS3 = Class.forName("php.java.bridge.generated.LauncherWindows3");
	    LAUNCHER_WINDOWS4 = Class.forName("php.java.bridge.generated.LauncherWindows4");
	    LAUNCHER_WINDOWS5 = Class.forName("php.java.bridge.generated.LauncherWindows5");
	    LAUNCHER_WINDOWS6 = Class.forName("php.java.bridge.generated.LauncherWindows6");
	    LAUNCHER_WINDOWS7 = Class.forName("php.java.bridge.generated.LauncherWindows7");
	} catch (Exception e) {/*ignore*/}
	    
    	Properties p = new Properties();
	try {
	    InputStream in = Util.class.getResourceAsStream("global.properties");
	    p.load(in);
	    VERSION = p.getProperty("BACKEND_VERSION");
	} catch (Throwable t) {
	    VERSION = "unknown";
	    //t.printStackTrace();
	};
	ENVIRONMENT_BLACKLIST = getEnvironmentBlacklist(p);
	COMMON_ENVIRONMENT = getCommonEnvironment(ENVIRONMENT_BLACKLIST);
	DEFAULT_CGI_LOCATIONS = new String[] {"/usr/bin/php-cgi", "c:/Program Files/PHP/php-cgi.exe"};
	try {
	    if (!new File(DEFAULT_CGI_LOCATIONS[0]).exists() && !new File(DEFAULT_CGI_LOCATIONS[1]).exists())
		try {
		    File filePath = null;
		    boolean found = false;
		    String path = (String)COMMON_ENVIRONMENT.get("PATH");
		    StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);
		    while(tok.hasMoreTokens()) {
			String s = tok.nextToken();
			if ((filePath = new File(s, "php-cgi.exe")).exists()) { found = true; break; }
			if ((filePath = new File(s, "php-cgi")).exists())     { found = true; break; }
		    }
		    if (!found) found = ((filePath = new File("/usr/php/bin/php-cgi")).exists());
		    if (!found) {
			String programFiles = (String)COMMON_ENVIRONMENT.get("ProgramFiles");
			if (programFiles!=null)
			 found = ((filePath = new File(programFiles+"\\PHP\\php-cgi.exe")).exists());
		    }
		    if (found) 
			DEFAULT_CGI_LOCATIONS = new String[] {filePath.getCanonicalPath(), DEFAULT_CGI_LOCATIONS[0], DEFAULT_CGI_LOCATIONS[1]};
		    
		} catch (Exception e) { /*ignore*/ }
	} catch (Throwable xe) {/*ignore*/}
	
	LAUNCHER_FLAGS = getProperty(p, "php.java.bridge.launcher_flags", "16777216");
	try {
	    MAX_WAIT = Integer.parseInt(getProperty(p, "php.java.bridge.max_wait", "15000"));
	} catch (Exception e) {
	    MAX_WAIT = 15000;
	}
	try {
	    HOME_DIR = new File(System.getProperty("user.home"));
	} catch (Exception e) {
	    HOME_DIR = null;
	}
	try {
	    JAVABRIDGE_BASE = getProperty(p, "php.java.bridge.base",  System.getProperty("user.home"));
	    JAVABRIDGE_LIB =  JAVABRIDGE_BASE + File.separator +"lib";
	} catch (Exception e) {
	    JAVABRIDGE_BASE=".";
	    JAVABRIDGE_LIB=".";
	}
	try {
    	    VM_NAME = "unknown";
	    VM_NAME = System.getProperty("java.version")+"@" + System.getProperty("java.vendor.url");	    
	} catch (Exception e) {/*ignore*/}
    	try {
    	    JAVABRIDGE_PROMISCUOUS = false;
	    JAVABRIDGE_PROMISCUOUS = getProperty(p, "php.java.bridge.promiscuous", "false").toLowerCase().equals("true");	    
	} catch (Exception e) {/*ignore*/}

	try {
	    THREAD_POOL_MAX_SIZE = "20";
	    THREAD_POOL_MAX_SIZE = getProperty(p, "THREADS", "20");
	} catch (Throwable t) {
	    //t.printStackTrace();
	};
	
	// resolve java.io.tmpdir for windows; PHP doesn't like dos short file names like foo~1\bar~2\...
	TMPDIR = new File(System.getProperty("java.io.tmpdir", "/tmp"));
	if (!TMPDIR.exists() || !TMPDIR.isDirectory()) TMPDIR = null;
	sessionSavePath = null;
	if (TMPDIR != null) try {TMPDIR = TMPDIR.getCanonicalFile(); } catch (IOException ex) {/*ignore*/}
	
	if (TMPDIR != null) {
	    sessionSavePath = TMPDIR.getPath();
	}
	DEFAULT_PHP_ARGS = "-d java.session=On -d display_errors=Off -d log_errors=On -d java.persistent_servlet_connections=On";
	    
	try {
	    String str = getProperty(p, "PHP_EXEC_ARGS", DEFAULT_PHP_ARGS);
	    String[] args = str.split(" ");
	    for (int i=0; i<args.length; i++) {
		try {
		    args[i] = java.net.URLDecoder.decode(args[i], UTF8);
		} catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		}
	    }
	    PHP_ARGS = args;
	} catch (Throwable t) {
	    //t.printStackTrace();
	};
	try {
	    EXTENSION_NAME = "JavaBridge";
	    EXTENSION_NAME = getProperty(p, "EXTENSION_DISPLAY_NAME", "JavaBridge");
	} catch (Throwable t) {
	    //t.printStackTrace();
	};
	try {
	    PHP_EXEC = getProperty(p, "PHP_EXEC", null);
	} catch (Throwable t) {
	    //t.printStackTrace();
	}
	try {
	    String s = getProperty(p, "DEFAULT_LOG_LEVEL", "3");
	    DEFAULT_LOG_LEVEL = Integer.parseInt(s);
	    Logger.setLogLevel(Util.DEFAULT_LOG_LEVEL); /* java.log_level in php.ini overrides */
	} catch (Throwable t) {/*ignore*/}
	try {
	    DEFAULT_LOG_FILE_SET = false;
	    DEFAULT_LOG_FILE = getProperty(p, "DEFAULT_LOG_FILE", Util.EXTENSION_NAME+".log");
	    DEFAULT_LOG_FILE_SET = System.getProperty("php.java.bridge.default_log_file") != null;
	} catch (Throwable t) {/*ignore*/}

	String separator = "/-+.,;: ";
	try {
	    String val = System.getProperty("os.arch").toLowerCase();
	    StringTokenizer t = new StringTokenizer(val, separator);
	    osArch = t.nextToken();
	} catch (Throwable t) {/*ignore*/}
	if(osArch==null) osArch="unknown";
	try {
	    String val = System.getProperty("os.name").toLowerCase();
	    StringTokenizer t = new StringTokenizer(val, separator);
	    osName = t.nextToken();
	} catch (Throwable t) {/*ignore*/}
	if(osName==null) osName="unknown";
    }
    
    /**
     * Return the class name
     * @param obj The object
     * @return The class name
     */
    public static String getClassName(Object obj) {
        if(obj==null) return "null";
        Class c = getClass(obj);
        String name = c.getName();
        if(name.startsWith("[")) name = "array_of_"+name.substring(1);
        return name;
    }
    
    /**
     * Return the short class name
     * @param obj The object
     * @return The class name
     */
    public static String getShortClassName(Object obj) {
	String name = getClassName(obj);
	int idx = name.lastIndexOf('.');
	if(idx!=-1) 
	    name = name.substring(idx+1);
	return name;
    }
    /**
     * Return the short class name
     * @param clazz The class
     * @return The class name
     */
    public static String getShortName(Class clazz) {
	String name = clazz.getName();
        if(name.startsWith("[")) name = "array_of_"+name.substring(1);
	int idx = name.lastIndexOf('.');
	if(idx!=-1) 
	    name = name.substring(idx+1);
	return name;
    }
    
    /**
     * Return the class or the object, if obj is already a class.
     * @param obj The object
     * @return Either obj or the class of obj.
     */
    public static Class getClass(Object obj) {
	if(obj==null) return null;
	return obj instanceof Class?(Class)obj:obj.getClass();
    }
    
    /**
     * Append an object to a StringBuffer
     * @param obj The object
     * @param buf The StringBuffer
     */
    public static void appendObject(Object obj, StringBuffer buf) {
	if(obj==null) { buf.append("null"); return; }

    	if(obj instanceof Class) {
	    if(((Class)obj).isInterface()) 
		buf.append("[i:");
	    else
		buf.append("[c:");
    	} else {
	    buf.append("[o:");
	}
        buf.append(getShortClassName(obj));
	buf.append("]:");
	buf.append("\"");
	buf.append(Util.stringValueOf(obj));
	buf.append("\"");
    }
    /**
     * Append a stack trace to buf.
     * @param throwable The throwable object
     * @param trace The trace from PHP
     * @param buf The current buffer.
     */
    public static void appendTrace(Throwable throwable, String trace, StringBuffer buf) {
	    buf.append(" at:\n");
	    StackTraceElement stack[] = throwable.getStackTrace();
	    int top=stack.length;
	    for(int i=0; i<top; i++) {
		buf.append("#-");
		buf.append(top-i);
		buf.append(" ");
		buf.append(stack[i].toString());
		buf.append("\n");
	    }
	    buf.append(trace);
    }
    /**
     * Append a parameter object to a StringBuffer
     * @param obj The object
     * @param buf The StringBuffer
     */
    public static void appendShortObject(Object obj, StringBuffer buf) {
	if(obj==null) { buf.append("null"); return; }

    	if(obj instanceof Class) {
	    if(((Class)obj).isInterface()) 
		buf.append("[i:");
	    else
		buf.append("[c:");
    	} else {
	    buf.append("[o:");
	}
        buf.append(getShortClassName(obj));
	buf.append("]");
    }
    
    /**
     * Append a function parameter to a StringBuffer
     * @param c The parameter 
     * @param buf The StringBuffer
     */
    public static void appendParam(Class c, StringBuffer buf) {
	if(c.isInterface()) 
	    buf.append("(i:");
	else if (c==java.lang.Class.class)
	    buf.append("(c:");
	else
	    buf.append("(o:");
	buf.append(getShortClassName(c));
	buf.append(")");
    }
    
    /**
     * Append a function parameter to a StringBuffer
     * @param obj The parameter object
     * @param buf The StringBuffer
     */
    public static void appendParam(Object obj, StringBuffer buf) {
	if(obj instanceof Class) {
	    Class c = (Class)obj;
	    if(c.isInterface()) 
		buf.append("(i:");
	    else
		buf.append("(c:");
	}
	else
	    buf.append("(o:");
	buf.append(getShortClassName(obj));
	buf.append(")");
    }
    
    /**
     * Return function arguments and their types as a String
     * @param args The args
     * @param params The associated types
     * @return A new string
     */
    public static String argsToString(Object args[], Class[] params) {
	StringBuffer buffer = new StringBuffer("");
	appendArgs(args, params, buffer);
	return buffer.toString();
    }
    
    /**
     * Append function arguments and their types to a StringBuffer
     * @param args The args
     * @param params The associated types
     * @param buf The StringBuffer
     */
    public static void appendArgs(Object args[], Class[] params, StringBuffer buf) {
	if(args!=null) {
	    for(int i=0; i<args.length; i++) {
		if(params!=null) {
		    appendParam(params[i], buf); 
		}
	    	appendShortObject(args[i], buf);
		
		if(i+1<args.length) buf.append(", ");
	    }
	}
    }
    
    /**
     * Locale-independent getBytes(), uses ASCII encoding
     * @param s The String
     * @return The ASCII encoded bytes
     */
    public static byte[] toBytes(String s) {
	try {
	    return s.getBytes(ASCII);
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	    return s.getBytes();
	}
    }
    
    /**
     * Create a string array from a hashtable.
     * @param h The hashtable
     * @return The String
     * @throws NullPointerException
     */
    public static String[] hashToStringArray(Map h) {
	Vector v = new Vector();
	Iterator e = h.keySet().iterator();
	while (e.hasNext()) {
	    String k = e.next().toString();
	    v.add(k + "=" + h.get(k));
	}
	String[] strArr = new String[v.size()];
	v.copyInto(strArr);
	return strArr;
    }


    /**
     * Returns the string "127.0.0.1". If the system property "php.java.bridge.promiscuous" is "true", 
     * the real host address is returned.
     * @return The host address as a string.
     */
    public static String getHostAddress(boolean promiscuous) {
	String addr = "127.0.0.1";
	try {
	    if(JAVABRIDGE_PROMISCUOUS || promiscuous) 
		addr = InetAddress.getLocalHost().getHostAddress();
	} catch (UnknownHostException e) {/*ignore*/}
	return addr;
    }
    /**
     * Checks if the cgi binary buf-&lt;os.arch&gt;-&lt;os.name&gt;.sh or buf-&lt;os.arch&gt;-&lt;os.name&gt;.exe or buf-&lt;os.arch&gt;-&lt;os.name&gt; exists.
     * @param php the php binary or null
     * @return The full name or null.
     */
    public static String[] checkCgiBinary(String php) {
    	File location;
 
    	File phpFile = new File(php);
    	String path = phpFile.getParent();
    	String file = phpFile.getName();
    	
    	StringBuffer buf = canonicalPath(path, file);
	
	if (USE_SH_WRAPPER) {
	    location = new File(buf.toString() + ".sh");
	    if(Logger.getLogLevel()>3) Logger.logDebug("trying: " + location);
	    if(location.exists()) return new String[] {"/bin/sh", location.getAbsolutePath()};
	} else {
	    location = new File(buf.toString() + ".exe");
	    if(Logger.getLogLevel()>3) Logger.logDebug("trying: " + location);
	    if(location.exists()) return new String[] {location.getAbsolutePath()};
	}
	
	location = new File(buf.toString());
	if(Logger.getLogLevel()>3) Logger.logDebug("trying: " + location);
	if(location.exists()) return new String[] {location.getAbsolutePath()};
	
	return null;
    }

    public static StringBuffer canonicalPath(String path, String file) {
	StringBuffer buf = new StringBuffer();
    	if (path != null) {
    	    buf.append(path);
    	    buf.append(File.separatorChar);
    	}
	buf.append(osArch);
	buf.append("-");
	buf.append(osName);
	buf.append(File.separatorChar);
	buf.append(file);
	return buf;
    }

    /**
     * Returns s if s contains "PHP Fatal error:";
     * @param s The error string
     * @return The fatal error or null
     */
    public static String checkError(String s) {
        // Is there a better way to check for a fatal error?
        return (s.startsWith("PHP") && (s.indexOf("error:")>-1)) ? s : null;
    }

    private static List getEnvironmentBlacklist(Properties p) {
	List l = new LinkedList();
	try {
	    String s = getProperty(p, "PHP_ENV_BLACKLIST", "PHPRC");
	    StringTokenizer t = new StringTokenizer(s, " ");
	    while(t.hasMoreTokens()) l.add(t.nextToken());
	} catch (Exception e) {
	    e.printStackTrace();
	    l = new LinkedList ();
	    l.add("PHPRC");
	}
	return l;
    }
    
    private static HashMap getCommonEnvironment(List blacklist) {
	String entries[] = {
		"PATH", "PATH", "LD_LIBRARY_PATH", "LD_ASSUME_KERNEL", "USER", "TMP", "TEMP", "HOME", "HOMEPATH", "LANG", "TZ", "OS"
	};
	HashMap defaultEnv = new HashMap();
	String key, val;
        Method m = null;
        try {m = System.class.getMethod("getenv", new Class[]{String.class});} catch (Exception e) {/*ignore*/}
	for(int i=0; i<entries.length; i++) {
	    val = null;
	    if (m!=null) { 
	      try {
	        val = (String) m.invoke(System.class, (Object[])new String[]{entries[i]});
	      } catch (Exception e) {
		 m = null;
	      }
	    }
	    if(val==null) {
	        try { val = System.getProperty(entries[i]); } catch (Exception e) {/*ignore*/}
	    }
	    if((val!=null) && (!blacklist.contains(entries[i])))
		defaultEnv.put(entries[i], val);
	}
	
	// check for windows SystemRoot, needed for socket operations
	key = val = null;
	if((new File("c:/winnt")).isDirectory()) val="c:\\winnt";
	else if((new File("c:/windows")).isDirectory()) val = "c:\\windows";
	try {
	    String s = System.getenv(key = "SystemRoot"); 
	    if(s!=null) val=s;
        } catch (Throwable t) {/*ignore*/}
        try {
	    String s = System.getProperty(key = "Windows.SystemRoot");
	    if(s!=null) val=s;
        } catch (Throwable t) {/*ignore*/}
	if(val!=null && (!blacklist.contains(key))) defaultEnv.put("SystemRoot", val);

	// add all non-blacklisted environment entries
	try {
	    m = System.class.getMethod("getenv", ZERO_PARAM);
	    Map map = (Map) m.invoke(System.class, ZERO_ARG);
	    for (Iterator ii = map.entrySet().iterator(); ii.hasNext(); ) {
		Entry entry = (Entry) ii.next();
		key = (String) entry.getKey();
		val = (String) entry.getValue();
		
		if (!blacklist.contains(key)) 
		    defaultEnv.put(key, val);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	return defaultEnv;
    }
    /** 
     * This procedure should be used whenever <code>object</code> may be a dynamic proxy: 
     * <code>String.valueOf(object) returns null, if object is a proxy and returns null.</code>
     * 
     * @param object The object or dynamic proxy
     * @return The string representation of object
     */
    public static String stringValueOf(Object object) {
        String s = String.valueOf(object);
        if(s==null) s = String.valueOf(s);
        return s;
    }

    /**
     * Return the time in GMT
     * @param ms the time in milliseconds
     * @return The formatted date string
     */
    public static String formatDateTime(long ms) {
	return formatter.format(new Date(ms));
    }
    static final boolean checkVM() {
	try {
	    return "libgcj".equals(System.getProperty("gnu.classpath.vm.shortname"));
	} catch (Throwable t) {
	    return false;
	}
    }
    /**
     * Return the thread context class loader
     * @return The context class loader
     */
    public static final ClassLoader getContextClassLoader() {
        ClassLoader loader = null;
        try {loader = Thread.currentThread().getContextClassLoader();} catch (SecurityException ex) {/*ignore*/}
	if(loader==null) loader = Util.class.getClassLoader();
        return loader;
    }
    public static final Class classForName(String name) throws ClassNotFoundException {
	return Class.forName(name, true, getContextClassLoader());
    }

    public static String getSimpleRedirectString(String webPath, String socketName, boolean isSecure) {
	try {
	    StringBuffer buf = new StringBuffer();
	    buf.append(socketName);
	    buf.append("/");
	    buf.append(webPath);
	    URI uri = new URI(isSecure?"s:127.0.0.1":"h:127.0.0.1", buf.toString(), null);
	    return (uri.toASCIIString()+".phpjavabridge");
	} catch (URISyntaxException e) {
	    Logger.printStackTrace(e);
	}
	StringBuffer buf = new StringBuffer(isSecure?"s:127.0.0.1":"h:127.0.0.1:");
	buf.append(socketName); 
	buf.append('/');
	buf.append(webPath);
	buf.append(".phpjavabridge");
	return buf.toString();
    }
 }
