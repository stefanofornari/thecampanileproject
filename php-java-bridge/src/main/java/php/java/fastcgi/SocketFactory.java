/*-*- mode: Java; tab-width:8 -*-*/

package php.java.fastcgi;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

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

class SocketFactory extends FCGIFactory {
    public static final String LOCAL_HOST = "127.0.0.1";
    private int port;

    private  ServerSocket fcgiTestSocket = null;
    private  int fcgiTestPort;

    public SocketFactory (String[] args, Map env, CloseableConnection fcgiConnectionPool, FCGIHelper helper) {
	super(args, env, fcgiConnectionPool, helper);
    }
    @Override
    public void test(String errorMessage) throws FCGIProcessException, ConnectionException {
        Socket testSocket;
	try {
	    testSocket = new Socket(InetAddress.getByName(getName()), port);
	    testSocket.close();
	} catch (IOException e) {
	    if (fcgiProcessStartException != null) {
		throw new FCGIProcessException(fcgiProcessStartException);
	    }
	    throw new ConnectionException(errorMessage, e);
	}
    }
    /**
     * Create a new socket and connect
     * it to the given host/port
     * @param host The host, for example 127.0.0.1
     * @param port The port, for example 9667
     * @return The socket
     * @throws UnknownHostException
     * @throws ConnectionException
     */
    private Socket doConnect(String host, int port) throws FCGIProcessException {
        Socket s = null;
	try {
            s = new Socket(InetAddress.getByName(host), port);
	} catch (IOException e) {
	    throw new FCGIProcessException(e);
	}
	try {
	    s.setTcpNoDelay(true);
	} catch (SocketException e) {
	    Logger.printStackTrace(e);
	}
	return s;
    }
    @Override
    public Connection connect() throws FCGIProcessException {
	Socket s = doConnect(getName(), getPort());
	return new SocketConnection(helper.getPhpFcgiMaxRequests(), s); 	
    }
    @Override
    protected void waitForDaemon() throws UnknownHostException, InterruptedException {
	int count = 15;
	InetAddress addr = InetAddress.getByName(LOCAL_HOST);
	if(Logger.getLogLevel()>3) Logger.logDebug("Waiting for PHP FastCGI daemon");
	while(count-->0) {
	    try {
		Socket s = new Socket(addr, getPort());
		s.close();
		break;
	    } catch (IOException e) {/*ignore*/}
	    Thread.sleep(100);
	}
	if(count==-1) Logger.logError("Timeout waiting for PHP FastCGI daemon");
	if(Logger.getLogLevel()>3) Logger.logDebug("done waiting for PHP FastCGI daemon");
    }
	    
    /* Start a fast CGI Server process on this computer. Switched off per default. */
    @Override
    protected FCGIProcess doBind() throws IOException {
	if(proc!=null) return null;
	StringBuffer buf = new StringBuffer((Util.JAVABRIDGE_PROMISCUOUS || promiscuous) ? "" : LOCAL_HOST); // bind to all available or loopback only
	buf.append(':');
	buf.append(String.valueOf(getPort()));
	String port = buf.toString();
	        
	// Set override hosts so that php does not try to start a VM.
	// The value itself doesn't matter, we'll pass the real value
	// via the (HTTP_)X_JAVABRIDGE_OVERRIDE_HOSTS header field
	// later.
	String[] args = new String[this.args.length+2];
	args[0]=this.args[0];
	args[1]="-b";
	args[2]=port;
	System.arraycopy(this.args, 1, args, 3, this.args.length-1);
	proc = createFCGIProcess(args, env);
	proc.start();
	return (FCGIProcess)proc;
    }
    protected int getPort() {
	return port;
    }
    protected String getName() {
	return LOCAL_HOST;
    }
    @Override
    public String getFcgiStartCommand(String base, int php_fcgi_max_requests) {
	String msg=
	    (base==null ? "" : "cd " + base + File.separator + Util.osArch + "-" + Util.osName+ "\n") + 
	    "REDIRECT_STATUS=200 " +
	    "X_JAVABRIDGE_OVERRIDE_HOSTS=\"/\" " +
	    "PHP_FCGI_CHILDREN=\"5\" " +
	    "PHP_FCGI_MAX_REQUESTS=\""+php_fcgi_max_requests+"\" php-cgi -b 127.0.0.1:" +
	    getPort()+"\n\n";
	return msg;
    }
    @Override
    protected void bind() throws InterruptedException, IOException {
	if(fcgiTestSocket!=null) { fcgiTestSocket.close(); fcgiTestSocket=null; }// replace the allocated socket# with the real fcgi server
	super.bind();
    }
    @Override	
    public void findFreePort(boolean select) {
	fcgiTestPort=FCGIUtil.FCGI_PORT; 
	fcgiTestSocket=null;
	for(int i=FCGIUtil.FCGI_PORT+1; select && (i<FCGIUtil.FCGI_PORT+100); i++) {
	    try {
		ServerSocket s = new ServerSocket(i, Util.BACKLOG, InetAddress.getByName(LOCAL_HOST));
		fcgiTestPort = i;
		fcgiTestSocket = s;
		break;
	    } catch (IOException e) {/*ignore*/}
	}
    }
    @Override
    public void setDefaultPort() {
	port = Integer.parseInt(helper.getSocketPort());
    }
    @Override
    protected void setDynamicPort() {
	port = fcgiTestPort;
    }
    @Override
    public void destroy() {
	super.destroy();
	if(fcgiTestSocket!=null) try { fcgiTestSocket.close(); fcgiTestSocket=null;} catch (Exception e) {/*ignore*/}
    }	  
    /** 
     * Return the channel name 
     * @return the channel name
     * 
     */
    @Override
    public String toString() {
	return "ChannelName@127.0.0.1:" + port;
    }
 }