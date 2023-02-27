/*-*- mode: Java; tab-width:8 -*-*/

package php.java.servlet;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import php.java.bridge.Util;
import php.java.bridge.http.ContextServer;
import php.java.bridge.util.ILogger;
import php.java.bridge.util.Logger;
import php.java.bridge.util.ThreadPool;
import php.java.fastcgi.ConnectionException;
import php.java.fastcgi.Continuation;
import php.java.fastcgi.FCGIConnectionPool;
import php.java.fastcgi.FCGIHeaderParser;
import php.java.fastcgi.FCGIProcessException;
import php.java.fastcgi.FCGIProxy;

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
/**
 * Register the PHP/Java Bridge when the web context starts. Used by
 * java_context()->onShutdown(proc). The WEB-INF/web.xml contains a listener
 * attribute: <blockquote> <code>
 * &lt;listener&gt;
 * &nbsp;&nbsp;&lt;listener-class&gt;php.java.servlet.ContextLoaderListener&lt;/listener-class&gt;
 *&lt;/listener&gt;
 * </code> </blockquote>
 */
public class ContextLoaderListener
        implements javax.servlet.ServletContextListener {
    private LinkedList closeables = new LinkedList();

    protected ServletContext context;

    private FCGIServletHelper helper = new FCGIServletHelper();

    protected boolean promiscuous = true;
    protected ContextLoaderListener listener;

    private ContextServer contextServer; // shared with FastCGIServlet

    private ThreadPool fcgiThreadPool;

    /**
     * The key used to store the ContextLoaderListener in the servlet context
     */
    public static final String CONTEXT_LOADER_LISTENER = ContextLoaderListener.class
            .getName() + ".ROOT";

    /**
     * Only for internal use
     * 
     * @param ctx
     *            The servlet context
     */
    public void destroyCloseables(ServletContext ctx) {
	List list = closeables;
	if (list == null)
	    return;

	try {
	    for (Iterator ii = list.iterator(); ii.hasNext();) {
		Object c = ii.next();
		try {
		    Method close = c.getClass().getMethod("close",
		            Util.ZERO_PARAM);
		    close.setAccessible(true);
		    close.invoke(c, Util.ZERO_ARG);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	} catch (Throwable t) {
	    t.printStackTrace();
	} finally {
	    closeables.clear();
	}
    }

    /** {@inheritDoc} */
    public void contextDestroyed(ServletContextEvent event) {
	ServletContext ctx = event.getServletContext();
	try {
	    destroyCloseables(ctx);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	if (contextServer != null)
	    contextServer.destroy();
	synchronized (globalCtxLock) {
	    if (fcgiConnectionPool != null)
		fcgiConnectionPool.destroy();
	    fcgiConnectionPool = null;
	}
	if (fcgiThreadPool!=null) {
	    fcgiThreadPool.destroy();
	    fcgiThreadPool = null;
	}
    }

    /** {@inheritDoc} */
    public void contextInitialized(ServletContextEvent event) {
	final ServletContext ctx = event.getServletContext();
	Logger.setLogger(new ILogger() {

	    @Override
	    public void printStackTrace(Throwable t) {
		ctx.log(String.valueOf(t), t);
	    }

	    @Override
	    public void log(int level, String msg) {
		switch (level) {
		case ILogger.FATAL:
		    ctx.log("fatal:" + msg);
		    break;
		case ILogger.ERROR:
		    ctx.log("error:" + msg);
		    break;
		case ILogger.INFO:
		    ctx.log("info:" + msg);
		    break;
		case ILogger.DEBUG:
		    ctx.log("debug:" + msg);
		    break;
		default:
		    ctx.log(msg);
		}
	    }

	    @Override
	    public void warn(String msg) {
		ctx.log("WARNING: " + msg);
	    }
	});

	ctx.setAttribute(CONTEXT_LOADER_LISTENER, this);
	this.context = ctx;
	helper.init(context);

	String servletContextName = ServletUtil.getRealPath(context, "");
	if (servletContextName == null)
	    servletContextName = "";
	contextServer = new ContextServer(servletContextName, promiscuous);

	fcgiThreadPool = new ThreadPool("JavaBridgeServletScriptEngineProxy", helper.getPhpFcgiConnectionPoolSize());
    }

    public ThreadPool getThreadPool() {
	return fcgiThreadPool;
    }

    public List getCloseables() {
	return closeables;
    }

    public static ContextLoaderListener getContextLoaderListener(
            ServletContext ctx) {
	return (ContextLoaderListener) ctx
	        .getAttribute(ContextLoaderListener.CONTEXT_LOADER_LISTENER);
    }

    public ContextServer getContextServer() {
	return contextServer;
    }

    private final Object globalCtxLock = new Object();
    private FCGIConnectionPool fcgiConnectionPool = null;

    public FCGIConnectionPool getConnectionPool()
            throws FCGIProcessException, ConnectionException {
	
	synchronized (globalCtxLock) {
	    if (fcgiConnectionPool != null)
		return fcgiConnectionPool;
	    String[] args = new String[] { helper.getPhp() };
	    HashMap env = new HashMap();
	    env.put("REDIRECT_STATUS", "200");
	    return getConnectionPool(args, env);
	}
    }

    private FCGIConnectionPool getConnectionPool(String[] args, Map env)
            throws FCGIProcessException, ConnectionException {
	
	synchronized (globalCtxLock) {
	    if (fcgiConnectionPool != null)
		return fcgiConnectionPool;
	    return fcgiConnectionPool = FCGIConnectionPool
	            .createConnectionPool(args, env, helper);

	}
    }

    public Continuation createContinuation(String[] args, Map env,
            OutputStream out, OutputStream err, FCGIHeaderParser headerParser)
            throws FCGIProcessException, ConnectionException {

	return new FCGIProxy(env, out, err, headerParser, args == null
	        ? fcgiConnectionPool : getConnectionPool(args, env));
    }

    public FCGIServletHelper getHelper() {
	return helper;
    }
}
