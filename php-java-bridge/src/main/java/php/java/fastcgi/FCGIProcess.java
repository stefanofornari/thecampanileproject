package php.java.fastcgi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import php.java.bridge.Util;
import php.java.bridge.util.Logger;

/*
 * Copyright (C) 2017 Jost Bökemeier
 *
 * The PHP/Java Bridge ("the library") is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either
 * version 2, or (at your option) any later version.
 *
 * The library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the PHP/Java Bridge; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA.
 *
 * Linking this file statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */

public class FCGIProcess extends java.lang.Process {

    protected java.lang.Process proc;
    private String[] args;
    private File homeDir;
    private Map env;
    private boolean tryOtherLocations;
    private boolean preferSystemPhp;
    private boolean isOldPhpVersion = false; // php < 5.3
    private boolean includeJava;
    private boolean includeDebugger;
    private String cgiDir;
    private String pearDir;
    private String webInfDir;
    private final Runtime rt = Runtime.getRuntime();

    private FCGIProcess(Builder builder) {
	this.args = builder.args;
	this.homeDir = builder.homeDir;
	this.env = builder.env;
	this.tryOtherLocations = builder.tryOtherLocations;
	this.preferSystemPhp = builder.preferSystemPhp;
	this.isOldPhpVersion = builder.isOldPhpVersion;
	this.includeJava = builder.includeJava;
	this.includeDebugger = builder.includeDebugger;
	this.cgiDir = builder.cgiDir;
	this.pearDir = builder.pearDir;
	this.webInfDir = builder.webInfDir;
    }

    private String getQuoted(String key, String val) {
	if (isOldPhpVersion)
	    return key + val;
	StringBuffer buf = new StringBuffer(key);
	buf.append("'");
	buf.append(val);
	buf.append("'");
	return buf.toString();
    }

    /**
     * Return args + PHP_ARGS
     * 
     * @param args
     *            The prefix
     * @param includeJava
     *            The option php_include_java
     * @param includeDebugger
     *            The option php_include_debugger
     * @param cgiDir
     *            The WEB-INF/cgi directory
     * @param pearDir
     *            The WEB-INF/pear directory
     * @param webInfDir
     *            The WEB-INF directory
     * @return args with PHP_ARGS appended
     */
    private String[] getPhpArgs(String[] args, boolean includeJava,
            boolean includeDebugger, String cgiDir, String pearDir,
            String webInfDir) {
	String[] allArgs = new String[args.length + Util.PHP_ARGS.length
	        + ((Util.sessionSavePath != null) ? 2 : 0)
	        + (includeJava ? 1 : 0) + (includeDebugger ? 1 : 0)
	        + (cgiDir != null ? 2 : 0) + (pearDir != null ? 2 : 0)
	        + (webInfDir != null ? 2 : 0)];
	int i = 0;
	for (i = 0; i < args.length; i++) {
	    allArgs[i] = args[i];
	}
	if (Util.sessionSavePath != null) {
	    allArgs[i++] = "-d";
	    allArgs[i++] = getQuoted("session.save_path=",
	            Util.sessionSavePath);
	}
	if (cgiDir != null) {
	    File extDir = new File(cgiDir, Util.osArch + "-" + Util.osName);
	    try {
		cgiDir = extDir.getCanonicalPath();
	    } catch (IOException e) {
		Logger.printStackTrace(e);
		cgiDir = extDir.getAbsolutePath();
	    }
	    allArgs[i++] = "-d";
	    allArgs[i++] = getQuoted("java.os_arch_dir=", cgiDir);
	}
	if (pearDir != null) {
	    allArgs[i++] = "-d";
	    allArgs[i++] = getQuoted("java.pear_dir=", pearDir);
	}
	if (webInfDir != null) {
	    allArgs[i++] = "-d";
	    allArgs[i++] = getQuoted("java.web_inf_dir=", webInfDir);
	}
	if (includeJava || includeDebugger)
	    allArgs[i++] = "-C"; // don't chdir, we'll do it
	for (int j = 0; j < Util.PHP_ARGS.length; j++) {
	    allArgs[i++] = Util.PHP_ARGS[j];
	}

	return allArgs;
    }

    protected String[] quoteArgs(String[] s) {
	// quote all args for windows
	if (!Util.USE_SH_WRAPPER)
	    for (int j = 0; j < s.length; j++)
		if (s[j] != null)
		    s[j] = "\"" + s[j] + "\"";
	return s;
    }

    protected boolean testPhp(String[] php, String[] args) {
	String[] s = quoteArgs(getTestArgumentArray(php, args));
	byte[] buf = new byte[Util.BUF_SIZE];
	int c, result, errCode;
	InputStream in = null;
	OutputStream out = null;
	InputStream err = null;

	try {
	    proc = rt.exec(s, hashToStringArray(env), homeDir);
	    in = proc.getInputStream();
	    err = proc.getErrorStream();
	    out = proc.getOutputStream();

	    out.close();
	    out = null;

	    while ((c = err.read(buf)) > 0)
		Logger.logError(new String(buf, 0, c, Util.ASCII));
	    err.close();
	    err = null;

	    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
	    while ((c = in.read(buf)) > 0)
		outBuf.write(buf, 0, c);
	    in.close();
	    in = null;

	    errCode = proc.waitFor();
	    result = proc.exitValue();

	    if (errCode != 0 || result != 0)
		throw new IOException(
		        "php could not be run, returned error code: " + errCode
		                + ", result: " + result);

	    try {
		checkOldPhpVersion(outBuf);
	    } catch (Throwable t) {
		Logger.printStackTrace(t);
	    } finally {
		outBuf.close();
	    }

	} catch (IOException e) {
	    Logger.logFatal("Fatal Error: Failed to start PHP "
	            + java.util.Arrays.asList(s) + ", reason: " + e);
	    return false;
	} catch (InterruptedException e) {
	    return false;
	} finally {
	    try {
		if (in != null)
		    in.close();
	    } catch (Exception e) {
		/* ignore */}
	    try {
		if (out != null)
		    out.close();
	    } catch (Exception e) {
		/* ignore */}
	    try {
		if (err != null)
		    err.close();
	    } catch (Exception e) {
		/* ignore */}
	}
	return true;
    }

    private void checkOldPhpVersion(ByteArrayOutputStream outBuf) {
	String ver = outBuf.toString();

	StringTokenizer tok = new StringTokenizer(ver);
	int n = tok.countTokens();
	if (n < 2)
	    return;

	String[] str = new String[n];
	for (int i = 0; tok.hasMoreTokens(); i++) {
	    str[i] = tok.nextToken();
	}

	tok = new StringTokenizer(str[1], ".");
	n = tok.countTokens();
	if (n < 1)
	    return;

	str = new String[n];
	for (int i = 0; tok.hasMoreTokens(); i++) {
	    str[i] = tok.nextToken();
	}

	int major = Integer.parseInt(str[0]);
	if ((major > 5))
	    return;
	if (major == 5) {
	    if (n < 2)
		return;
	    int minor = Integer.parseInt(str[1]);
	    if (minor > 2)
		return;
	}
	isOldPhpVersion = true;
    }

    protected void runPhp(String[] php, String[] args) throws IOException {
	String[] s = quoteArgs(getArgumentArray(php, args));

	proc = rt.exec(s, hashToStringArray(env), homeDir);
	if (Logger.getLogLevel() > 3)
	    Logger.logDebug("Started " + java.util.Arrays.asList(s));
    }

    protected String[] getTestArgumentArray(String[] php, String[] args) {
	LinkedList buf = new LinkedList();
	buf.addAll(java.util.Arrays.asList(php));
	buf.add("-v");

	return (String[]) buf.toArray(new String[buf.size()]);
    }

    protected String[] getArgumentArray(String[] php, String[] args) {
	String realPath = new File(args[0]).getParent();
	if (realPath == null)
	    realPath = Util.TMPDIR.getAbsolutePath(); // if not in
	                                              // cgi/version-arch/, use
	                                              // tmpdir to create
	                                              // launcher
	LinkedList buf = new LinkedList();
	if (Util.USE_SH_WRAPPER) {
	    buf.add("/bin/sh");
	    buf.add(realPath + File.separator + "launcher.sh");
	    buf.addAll(java.util.Arrays.asList(php));
	    for (int i = 1; i < args.length; i++) {
		buf.add(args[i]);
	    }
	    buf.addAll(java.util.Arrays.asList(Util.ALLOW_URL_INCLUDE));
	} else {
	    buf.add(realPath + File.separator + "launcher.exe");
	    buf.add(Util.LAUNCHER_FLAGS); // CREATE_BREAKAWAY_FROM_JOB
	    buf.addAll(java.util.Arrays.asList(php));
	    for (int i = 1; i < args.length; i++) {
		buf.add(args[i]);
	    }
	    buf.addAll(java.util.Arrays.asList(Util.ALLOW_URL_INCLUDE));
	}
	return (String[]) buf.toArray(new String[buf.size()]);
    }

    protected void start() throws NullPointerException, IOException {
	File location;
	/*
	 * Extract the php executable from args[0] ...
	 */
	String[] php = new String[] { null };
	if (args == null)
	    args = new String[] { null };
	String phpExec = args[0];
	String[] cgiBinary = null;
	if (Util.PHP_EXEC == null) {
	    if (!preferSystemPhp) {
		if (phpExec != null
		        && ((cgiBinary = Util.checkCgiBinary(phpExec)) != null))
		    php = cgiBinary;
		/*
		 * ... resolve it ..
		 */
		if (tryOtherLocations && php[0] == null) {
		    for (int i = 0; i < Util.DEFAULT_CGI_LOCATIONS.length; i++) {
			location = new File(Util.DEFAULT_CGI_LOCATIONS[i]);
			if (location.exists()) {
			    php[0] = location.getAbsolutePath();
			    break;
			}
		    }
		}
	    } else {
		/*
		 * ... resolve it ..
		 */
		if (tryOtherLocations && php[0] == null) {
		    for (int i = 0; i < Util.DEFAULT_CGI_LOCATIONS.length; i++) {
			location = new File(Util.DEFAULT_CGI_LOCATIONS[i]);
			if (location.exists()) {
			    php[0] = location.getAbsolutePath();
			    break;
			}
		    }
		}
		if (phpExec != null && (php[0] == null
		        && (cgiBinary = Util.checkCgiBinary(phpExec)) != null))
		    php = cgiBinary;
	    }
	}
	if (php[0] == null && tryOtherLocations)
	    php[0] = Util.PHP_EXEC;
	if (php[0] == null && phpExec != null && (new File(phpExec).exists()))
	    php[0] = phpExec;

	String workspace;
	if (php[0] == null
	        && (workspace = Util
	                .canonicalPath(new File("WebContent/WEB-INF/cgi")
	                        .getAbsolutePath(), "php-cgi")
	                .toString()) != null
	        && (new File(workspace).exists()
	                || new File(workspace + ".exe").exists()))
	    php[0] = workspace;

	// give up, use standard php-cgi anywhere in the path
	if (php[0] == null)
	    php[0] = new File("php-cgi").getAbsolutePath();

	if (Logger.getLogLevel() > 3)
	    Logger.logDebug(
	            "Using php binary: " + java.util.Arrays.asList(php));

	/*
	 * ... and construct a new argument array for this specific process.
	 */
	if (homeDir != null && cgiBinary == null)
	    homeDir = Util.HOME_DIR; // system PHP executables are always
	                             // executed in the user's HOME dir

	if (homeDir != null && !homeDir.exists())
	    homeDir = null;

	if (testPhp(php, args))
	    runPhp(php, getPhpArgs(args, includeJava, includeDebugger, cgiDir,
	            pearDir, webInfDir));
	else
	    throw new IOException(
	            "PHP not found. Please install php-cgi. PHP test command was: "
	                    + java.util.Arrays.asList(
	                            getTestArgumentArray(php, args))
	                    + " ");
    }

    /**
     * Check for a PHP fatal error and throw a PHP exception if necessary.
     * 
     * @throws PhpException
     */
    public void checkError() throws PhpException {
    }

    /** {@inheritDoc} */
    public OutputStream getOutputStream() {
	return proc.getOutputStream();
    }

    /** {@inheritDoc} */
    public InputStream getInputStream() {
	return proc.getInputStream();
    }

    /** {@inheritDoc} */
    public InputStream getErrorStream() {
	return proc.getErrorStream();
    }

    /** {@inheritDoc} */
    public int waitFor() throws InterruptedException {
	return proc.waitFor();
    }

    /** {@inheritDoc} */
    public int exitValue() {
	return proc.exitValue();
    }

    /** {@inheritDoc} */
    public void destroy() {
	proc.destroy();
    }

    /**
     * Create a string array from a hashtable.
     * 
     * @param h
     *            The hashtable
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

    public static Builder builder() {
	return new Builder();
    }

    public static final class Builder {
	private String[] args = Util.PHP_ARGS;
	private File homeDir = new File(Util.JAVABRIDGE_BASE).getAbsoluteFile();
	private Map env = new HashMap(Util.COMMON_ENVIRONMENT);
	private boolean tryOtherLocations = true;
	private boolean preferSystemPhp = true;
	private boolean isOldPhpVersion = false; // php < 5.3
	private boolean includeJava = false;
	private boolean includeDebugger = false;
	private String cgiDir = null;
	private String pearDir = null;
	private String webInfDir = null;

	public Builder() {
	}

	public Builder withArgs(String[] args) {
	    this.args = args;
	    return this;
	}

	public Builder withHomeDir(File homeDir) {
	    this.homeDir = homeDir;
	    return this;
	}

	public Builder withEnv(Map env) {
	    this.env = env;
	    return this;
	}

	public Builder withTryOtherLocations(boolean tryOtherLocations) {
	    this.tryOtherLocations = tryOtherLocations;
	    return this;
	}

	public Builder withPreferSystemPhp(boolean preferSystemPhp) {
	    this.preferSystemPhp = preferSystemPhp;
	    return this;
	}

	public Builder withIsOldPhpVersion(boolean isOldPhpVersion) {
	    this.isOldPhpVersion = isOldPhpVersion;
	    return this;
	}

	public Builder withIncludeJava(boolean includeJava) {
	    this.includeJava = includeJava;
	    return this;
	}

	public Builder withIncludeDebugger(boolean includeDebugger) {
	    this.includeDebugger = includeDebugger;
	    return this;
	}

	public Builder withCgiDir(String cgiDir) {
	    this.cgiDir = cgiDir;
	    return this;
	}

	public Builder withPearDir(String pearDir) {
	    this.pearDir = pearDir;
	    return this;
	}

	public Builder withWebInfDir(String webInfDir) {
	    this.webInfDir = webInfDir;
	    return this;
	}

	public Builder withHelper(FCGIHelper helper) {
	    tryOtherLocations = helper.isPhpTryOtherLocations();
	    preferSystemPhp = helper.isPreferSystemPhp();
	    isOldPhpVersion = false;
	    includeJava = helper.isPhpIncludeJava();
	    includeDebugger = helper.isPhpIncludeDebugger();
	    cgiDir = helper.getCgiDir();
	    pearDir = helper.getPearDir();
	    webInfDir = helper.getWebInfDir();
	    return this;
	}

	public FCGIProcess build() {
	    return new FCGIProcess(this);
	}
    }

}