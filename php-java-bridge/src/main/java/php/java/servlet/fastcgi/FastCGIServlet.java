/*-*- mode: Java; tab-width:8 -*-*/
package php.java.servlet.fastcgi;

import java.io.ByteArrayOutputStream;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import php.java.bridge.Util;
import php.java.bridge.http.AbstractChannelName;
import php.java.bridge.http.IContextFactory;
import php.java.bridge.util.Logger;
import php.java.bridge.util.NotImplementedException;
import php.java.fastcgi.Connection;
import php.java.fastcgi.ConnectionException;
import php.java.fastcgi.FCGIHeaderParser;
import php.java.fastcgi.FCGIInputStream;
import php.java.fastcgi.FCGIOutputStream;
import php.java.fastcgi.FCGIUtil;
import php.java.servlet.ContextLoaderListener;
import php.java.servlet.PhpJavaServlet;
import php.java.servlet.ServletContextFactory;
import php.java.servlet.ServletUtil;

/**
 * A CGI Servlet which connects to a FastCGI server. If allowed by the
 * administrator and if a fast cgi binary is installed in the JavaBridge web
 * application or DEFAULT_CGI_LOCATIONS, the bridge can automatically start one
 * FCGI server on the computer. Default is Autostart.
 * <p>
 * The admin may start a FCGI server for all users with the command:<br>
 * <code> cd /tmp<br>
 * REDIRECT_STATUS=200 X_JAVABRIDGE_OVERRIDE_HOSTS="/" PHP_FCGI_CHILDREN="5"
 * PHP_FCGI_MAX_REQUESTS="5000" /usr/bin/php-cgi -b 127.0.0.1:9667<br>
 * </code>
 * </p>
 * <p>
 * When the program <code>/bin/sh</code> does not exist, a program called
 * <code>launcher.exe</code> is called instead:
 * <code>launcher.exe "path_to_php-cgi.exe" 16777216 -b 9667</code>.
 * </p>
 * 
 * @see php.java.bridge.Util#DEFAULT_CGI_LOCATIONS
 */
public class FastCGIServlet extends HttpServlet {
    protected static final String _80 = "80";
    protected static final String _443 = "443";

    private static final long serialVersionUID = 3545800996174312757L;

    protected String documentRoot;
    protected String serverSignature;

    private final class ErrorLogOutputStream extends OutputStream {
	@Override
	public void write(byte[] buf, int s, int n) throws IOException {
	    Logger.logError(new String(buf, s, n));
	}

	@Override
	public void write(int b) throws IOException {
	    throw new NotImplementedException();
	}
    }

    protected static class Environment {
	public IContextFactory ctx;
	public String contextPath;
	public String pathInfo;
	public String servletPath;
	public String queryString;
	public String requestUri;
	public HashMap environment;
	public boolean includedJava;
	public boolean includedDebugger;
	public ArrayList allHeaders;
    }

    protected ServletContext context;
    protected ContextLoaderListener contextLoaderListener;
    protected String serverInfo;

    protected boolean phpRequestURIisUnique; // Patch#3040849
    private ErrorLogOutputStream err = new ErrorLogOutputStream();
    private FCGIServletHeaderParser headerParser = new FCGIServletHeaderParser();

    /**
     * Create a new FastCGI servlet which connects to a PHP FastCGI server using
     * a connection pool.
     * 
     * If the JavaBridge context exists and the JavaBridge context can start a
     * FastCGI server and the current context is configured to connect to a
     * FastCGI server, the current context connects to the JavaBridge context to
     * start the server and then uses this server for all subsequent requests
     * until the server is stopped. When FastCGI is not available (anymore), the
     * parent CGI servlet is used instead.
     * 
     * @param config
     *            The servlet config
     * @throws ServletException
     * @see php.java.bridge.http.FCGIConnectionPool
     * @see #destroy()
     */
    public void init(ServletConfig config) throws ServletException {
	super.init(config);

	context = config.getServletContext();

	String value = (String) config
	        .getInitParameter("php_request_uri_is_unique");
	if ("on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value))
	    phpRequestURIisUnique = true;

	contextLoaderListener = (ContextLoaderListener) context
	        .getAttribute(ContextLoaderListener.CONTEXT_LOADER_LISTENER);
	serverInfo = config.getServletName();
	if (serverInfo == null)
	    serverInfo = "FastCGIServlet";

	documentRoot = ServletUtil.getRealPath(context, "");
	serverSignature = context.getServerInfo();
    }

    public void destroy() {
	super.destroy();
    }

    protected void setupRequestVariables(HttpServletRequest req,
            Environment env) {
	env.allHeaders = new ArrayList();
	env.includedJava = contextLoaderListener.getHelper().isPhpIncludeJava()
	        && PhpJavaServlet.getHeader(Util.X_JAVABRIDGE_INCLUDE,
	                req) == null;
	env.includedDebugger = contextLoaderListener.getHelper()
	        .isPhpIncludeDebugger()
	        && PhpJavaServlet.getHeader(Util.X_JAVABRIDGE_INCLUDE,
	                req) == null;

	env.contextPath = (String) req
	        .getAttribute("javax.servlet.include.context_path");
	if (env.contextPath == null)
	    env.contextPath = req.getContextPath();

	env.pathInfo = (String) req
	        .getAttribute("javax.servlet.include.path_info");
	if (env.pathInfo == null)
	    env.pathInfo = req.getPathInfo();

	env.servletPath = (String) req
	        .getAttribute("javax.servlet.include.servlet_path");
	if (env.servletPath == null)
	    env.servletPath = req.getServletPath();

	env.queryString = (String) req
	        .getAttribute("javax.servlet.include.query_string");
	if (env.queryString == null)
	    env.queryString = req.getQueryString();

	if (phpRequestURIisUnique) { // use target: my.jsp:include||forward
	                             // target.php => REQUEST_URI: target.php
	    env.requestUri = (String) req
	            .getAttribute("javax.servlet.include.request_uri");
	} else { // use source: my.jsp:include||forward target.php =>
	         // REQUEST_URI: my.jsp
	    env.requestUri = (String) req
	            .getAttribute("javax.servlet.forward.request_uri");
	}
	if (env.requestUri == null)
	    env.requestUri = req.getRequestURI();
    }

    /** calculate PATH_INFO, PATH_TRANSLATED and SCRIPT_FILENAME */
    protected void setPathInfo(HttpServletRequest req, HashMap envp,
            Environment env) {

	String pathInfo = env.pathInfo;
	if (pathInfo != null) {
	    envp.put("PATH_INFO", pathInfo);
	    envp.put("PATH_TRANSLATED", documentRoot + pathInfo);
	}
    }

    private void setScriptName(HttpServletRequest activeReq, Environment env,
            boolean hasPostData) {
	HashMap envp = env.environment;
	boolean includeDebugger = env.includedDebugger
	        && "1".equals(
	                getParameter(activeReq, "start_debug", hasPostData))
	        && null != getParameter(activeReq, "debug_port", hasPostData)
	        && null != getParameter(activeReq, "original_url", hasPostData);
	boolean includeJavaInc = env.includedJava;
	if (includeDebugger && includeJavaInc) {
	    envp.put("X_JAVABRIDGE_INCLUDE_ONLY", "@");
	    envp.put("X_JAVABRIDGE_INCLUDE",
	            ServletUtil.getRealPath(context, env.servletPath));
	    envp.put("SCRIPT_FILENAME",
	            ServletUtil.getRealPath(context, "java/PHPDebugger.php"));
	} else if (includeDebugger && !includeJavaInc) {
	    envp.put("X_JAVABRIDGE_INCLUDE",
	            ServletUtil.getRealPath(context, env.servletPath));
	    envp.put("SCRIPT_FILENAME",
	            ServletUtil.getRealPath(context, "java/PHPDebugger.php"));
	} else if (!includeDebugger && includeJavaInc) {
	    envp.put("X_JAVABRIDGE_INCLUDE_ONLY",
	            ServletUtil.getRealPath(context, "java/Java.inc"));
	    envp.put("X_JAVABRIDGE_INCLUDE",
	            ServletUtil.getRealPath(context, env.servletPath));
	    envp.put("SCRIPT_FILENAME",
	            ServletUtil.getRealPath(context, "java/PHPDebugger.php"));
	} else {
	    envp.put("SCRIPT_FILENAME",
	            ServletUtil.getRealPath(context, env.servletPath));
	}

    }

    private String getParameter(HttpServletRequest activeReq, String str,
            boolean postData) {
	return postData ? null : activeReq.getParameter(str);
    }

    protected void setupCGIEnvironment(HttpServletRequest req,
            HttpServletResponse res, Environment env) throws ServletException {
	HashMap envp = new HashMap();

	envp.put("SERVER_SOFTWARE", serverInfo);
	envp.put("SERVER_NAME", ServletUtil.nullsToBlanks(req.getServerName()));
	envp.put("GATEWAY_INTERFACE", "CGI/1.1");
	envp.put("SERVER_PROTOCOL",
	        ServletUtil.nullsToBlanks(req.getProtocol()));
	int port = ServletUtil.getServerPort(req);
	Integer iPort = (port == 0 ? new Integer(-1) : new Integer(port));
	envp.put("SERVER_PORT", iPort.toString());
	envp.put("REQUEST_METHOD", ServletUtil.nullsToBlanks(req.getMethod()));
	envp.put("SCRIPT_NAME", env.contextPath + env.servletPath);
	envp.put("QUERY_STRING", ServletUtil.nullsToBlanks(env.queryString));
	envp.put("REMOTE_HOST", ServletUtil.nullsToBlanks(req.getRemoteHost()));
	envp.put("REMOTE_ADDR", ServletUtil.nullsToBlanks(req.getRemoteAddr()));
	envp.put("AUTH_TYPE", ServletUtil.nullsToBlanks(req.getAuthType()));
	envp.put("REMOTE_USER", ServletUtil.nullsToBlanks(req.getRemoteUser()));
	envp.put("REMOTE_IDENT", ""); // not necessary for full compliance
	envp.put("CONTENT_TYPE",
	        ServletUtil.nullsToBlanks(req.getContentType()));
	setPathInfo(req, envp, env);

	/*
	 * Note CGI spec says CONTENT_LENGTH must be NULL ("") or undefined if
	 * there is no content, so we cannot put 0 or -1 in as per the Servlet
	 * API spec.
	 */
	int contentLength = req.getContentLength();
	String sContentLength = (contentLength <= 0 ? ""
	        : (new Integer(contentLength)).toString());
	envp.put("CONTENT_LENGTH", sContentLength);

	Enumeration headers = req.getHeaderNames();
	String header = null;
	StringBuffer buffer = new StringBuffer();

	while (headers.hasMoreElements()) {
	    header = ((String) headers.nextElement()).toUpperCase();
	    if ("AUTHORIZATION".equalsIgnoreCase(header)
	            || "PROXY_AUTHORIZATION".equalsIgnoreCase(header)) {
		// NOOP per CGI specification section 11.2
	    } else if ("HOST".equalsIgnoreCase(header)) {
		String host = req.getHeader(header);
		int idx = host.indexOf(":");
		if (idx < 0)
		    idx = host.length();
		envp.put("HTTP_" + header.replace('-', '_'),
		        host.substring(0, idx));
	    } else if (header.startsWith("X_")) {
		envp.put(header, req.getHeader(header));
	    } else {
		envp.put("HTTP_" + header.replace('-', '_'),
		        ServletUtil.getHeaders(buffer, req.getHeaders(header)));
	    }
	}

	env.environment = envp;

	env.environment.put("REDIRECT_STATUS", "200");
	env.environment.put("SERVER_SOFTWARE", Util.EXTENSION_NAME);

	String sPort = (String) env.environment.get("SERVER_PORT");
	String standardPort = req.isSecure() ? _443 : _80;
	StringBuffer httpHost = new StringBuffer(
	        (String) env.environment.get("SERVER_NAME"));
	if (!standardPort.equals(sPort)) { // append port only if necessary, see
	                                   // Patch#3040838
	    httpHost.append(":");
	    httpHost.append(sPort);
	}
	env.environment.put("HTTP_HOST", httpHost.toString());

	String remotePort = null;
	try {
	    remotePort = String.valueOf(req.getRemotePort());
	} catch (Throwable t) {
	    remotePort = String.valueOf(t);
	}
	env.environment.put("REMOTE_PORT", remotePort);
	String query = env.queryString;
	if (query != null)
	    env.environment.put("REQUEST_URI",
	            ServletUtil.nullsToBlanks(env.requestUri + "?" + query));
	else
	    env.environment.put("REQUEST_URI",
	            ServletUtil.nullsToBlanks(env.requestUri));

	env.environment.put("SERVER_ADDR", req.getServerName());
	env.environment.put("SERVER_SIGNATURE", serverSignature);
	env.environment.put("DOCUMENT_ROOT", documentRoot);
	if (req.isSecure())
	    env.environment.put("HTTPS", "On");

	/*
	 * send the session context now, otherwise the client has to call
	 * handleRedirectConnection
	 */
	String id = PhpJavaServlet.getHeader(Util.X_JAVABRIDGE_CONTEXT, req);
	if (id == null) {
	    id = (env.ctx = ServletContextFactory.addNew(
	            contextLoaderListener.getContextServer(), this,
	            getServletContext(), req, req, res)).getId();
	    // short path S1: no PUT request
	    AbstractChannelName channelName = contextLoaderListener
	            .getContextServer().getChannelName(env.ctx);
	    if (channelName != null) {
		env.environment.put(Util.X_JAVABRIDGE_REDIRECT,
		        channelName.getName());
		env.ctx.getBridge();
		contextLoaderListener.getContextServer().start(channelName);
	    }
	}
	env.environment.put(Util.X_JAVABRIDGE_CONTEXT, id);
    }

    /**
     * Optimized run method for FastCGI. Makes use of the large FCGI_BUF_SIZE
     * and the specialized in.read(). It is a modified copy of the parseBody.
     * 
     * @throws InterruptedException
     * @see FCGIHeaderParser#parseBody(byte[], InputStream, OutputStream,
     *      FCGIHeaderParser)
     */
    protected void doExecute(HttpServletRequest req, HttpServletResponse res,
            Environment env)
            throws IOException, ServletException, InterruptedException {
	final byte[] buf = new byte[FCGIUtil.FCGI_BUF_SIZE];// headers cannot be
	                                                    // larger than this
	                                                    // value!

	InputStream in = null;
	OutputStream out = null;

	FCGIInputStream natIn = null;
	FCGIOutputStream natOut = null;

	Connection connection = null;
	try {
	    connection = contextLoaderListener.getConnectionPool()
	            .openConnection();

	    natIn = (FCGIInputStream) connection.getInputStream();
	    natOut = (FCGIOutputStream) connection.getOutputStream();
	    natOut.setId(connection.getId());

	    in = req.getInputStream(); // do not close in, otherwise
	                               // requestDispatcher().include() will
	                               // receive
	                               // a closed input stream
	    out = ServletUtil.getServletOutputStream(res);

	    byte[] postData = readPostData(buf, in);
	    boolean hasPostData = postData != null && postData.length > 0;

	    // update the env after fetching the inputstream
	    setScriptName(req, env, hasPostData);

	    // send the FCGI header
	    sendFcgiHeader(env, natOut, connection);

	    // write the post data before reading the response
	    writePostData(natOut, postData, hasPostData);
	    natOut = null;

	    headerParser.setEnv(env);
	    headerParser.setResponse(res);

	    headerParser.parseBody(buf, natIn, out, err);

	    natIn.close();
	    natIn = null;
	} finally {
	    // Destroy physical connection if exception occured,
	    // so that the PHP side doesn't keep unsent data
	    // A more elegant approach would be to use the FCGI ABORT request.
	    if (natIn != null)
		connection.setIsClosed();
	    if (connection != null)
		contextLoaderListener.getConnectionPool()
		        .closeConnection(connection);
	}

    }

    private byte[] readPostData(final byte[] buf, InputStream in)
            throws IOException {
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	int n;
	// write the post data before reading the response
	while ((n = in.read(buf)) != -1) {
	    buffer.write(buf, 0, n);
	}
	return buffer.toByteArray();
    }

    private void writePostData(FCGIOutputStream natOut, final byte[] postData,
            final boolean hasPostData) throws ConnectionException {

	if (hasPostData) {
	    natOut.write(FCGIUtil.FCGI_STDIN, postData, postData.length);
	}
	natOut.write(FCGIUtil.FCGI_STDIN, FCGIUtil.FCGI_EMPTY_RECORD);
	natOut.close();
    }

    private void sendFcgiHeader(Environment env, FCGIOutputStream natOut,
            Connection connection) throws ConnectionException {
	natOut.writeBegin(connection.isLast());
	natOut.writeParams(env.environment);
	natOut.write(FCGIUtil.FCGI_PARAMS, FCGIUtil.FCGI_EMPTY_RECORD);
    }

    private void handleWebSocketRequest(final byte[] buf, final byte[] postData,
            final boolean hasPostData, final InputStream inputStream,
            final FCGIOutputStream natOutputStream) throws ConnectionException {

	if (hasPostData) {
	    natOutputStream.write(FCGIUtil.FCGI_STDIN, postData,
	            postData.length);
	}

	// write the post data while reading the response
	// used by either http/1.1 chunked connections or "WebSockets",
	// see
	// http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-70
	(new Thread() {
	    public void run() {
		try {
		    int n;
		    while ((n = inputStream.read(buf)) != -1) {
			natOutputStream.write(FCGIUtil.FCGI_STDIN, buf, n);
		    }
		    natOutputStream.write(FCGIUtil.FCGI_STDIN,
		            FCGIUtil.FCGI_EMPTY_RECORD);
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
			natOutputStream.close();
		    } catch (IOException e) {
			// ignore
		    }
		}
	    }
	}).start();
    }

    protected Environment getEnvironment() {
	return new Environment();
    }

    protected void execute(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException, InterruptedException {

	Environment env = getEnvironment();
	setupRequestVariables(req, env);
	setupCGIEnvironment(req, res, env);

	try {
	    doExecute(req, res, env);
	} finally {
	    if (env.ctx != null)
		env.ctx.releaseManaged();
	    env.ctx = null;
	}
    }

    protected void handle(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
	try {
	    execute(req, res);
	} catch (InterruptedException e) {
	    /* ignore */}
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
	handle(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
	handle(req, res);
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
	handle(req, res);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
	if (Logger.getLogLevel() > 4) {
	    if (req.getAttribute("javax.servlet.include.request_uri") != null)
		log("doGet (included):" + req
		        .getAttribute("javax.servlet.include.request_uri"));
	    log("doGet:" + req.getRequestURI());
	}
	handle(req, res);
    }
}
