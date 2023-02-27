/*-*- mode: Java; tab-width:8 -*-*/
package php.java.bridge.classloader;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import php.java.bridge.Util;
import php.java.bridge.util.Logger;

/**
 * This class works around a problem in the URLClassLoader implementation, which fetches remote 
 * jar files and caches them forever. This behaviour makes the DynamicClassLoader unusable.
 * 
 * This implementation fetches the jar file and caches them until the associated URL is
 * finalized.
 * 
 * @author jostb
 *
 */
class DynamicJarURLConnection extends JarURLConnection {

    private DynamicHttpURLConnectionHandler handler;
    
    protected DynamicJarURLConnection(URL u, DynamicHttpURLConnectionHandler handler) throws MalformedURLException {
	super(u);
	this.handler = handler;
	if(Logger.getLogLevel()>4) Logger.logDebug("tempfile create DynamicJarURLConnection for " + handler);	
    }
    public void connect() throws IOException
    {
	if(!connected) {
	    if(Logger.getLogLevel()>4) Logger.logDebug("tempfile open connection for " + handler);
	    jarFileURLConnection = getJarFileURL().openConnection();
	    jarFileURLConnection.connect();
	    connected = true;
	}
    }

    private Map headerFields;
    
    public Map getHeaderFields() {
	if(this.headerFields!=null) return this.headerFields;
	if((this.headerFields = this.handler.getHeaderFields())!=null) return this.headerFields;
	try {
	    if(!connected) connect();
	    if(Logger.getLogLevel()>4) Logger.logDebug("tempfile getHeaderFields for " + handler);
	    this.headerFields = new HashMap();
	    Map headerFields = jarFileURLConnection.getHeaderFields();
	    StringBuffer b = new StringBuffer();
	    for(Iterator ii = headerFields.entrySet().iterator(); ii.hasNext(); ) {
		Entry e = (Entry) ii.next();
		Object key = e.getKey();
		if(key==null) continue;
		Object value = e.getValue();
		if(value==null) continue;
		List list= (List)value;
		Iterator ii1 = list.iterator();
		if(ii1.hasNext()) {
		    b.append(ii1.next());
		}
		while(ii1.hasNext()) {
		    b.append(", ");
		    b.append(ii1.next());
		}
		String k = (String)key;
		k = k.toLowerCase();
		this.headerFields.put(k, b.toString());
		b.setLength(0);
	    }
	    this.handler.setHeaderFields(this.headerFields);
	    return this.headerFields;
	} catch (IOException e) {
	    Logger.printStackTrace(e);
	    throw new RuntimeException(e);
	}
    }
 
    public String getHeaderField(String key) {
	String val = (String) getHeaderFields().get(key);
	return val;
    }
    private JarFile jarFile;
    public JarFile getJarFile() throws IOException {
	if(this.jarFile!=null) return this.jarFile;
	if((this.jarFile = this.handler.getTempFile())!=null) return this.jarFile;
	if(!connected) connect();
	if(Logger.getLogLevel()>4) Logger.logDebug("tempfile getJarFile for " + handler);
	InputStream is = jarFileURLConnection.getInputStream();
	byte[] buf = new byte[Util.BUF_SIZE];
	File f = File.createTempFile("pjbcache", "jar");
	f.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(f);
	int len = 0;
	while((len = is.read(buf)) != -1)  fos.write(buf, 0, len);
        fos.close();
	JarFile zipFile = new JarFile(f, true, ZipFile.OPEN_READ);
	this.handler.setTempFile(zipFile, f);
	return this.jarFile = zipFile;
    }
    private JarEntry entry;
    public InputStream getInputStream()  throws IOException {
	if(entry!=null) return getJarFile().getInputStream(entry);
	if(!connected) connect();
	entry = getJarEntry();
	long size = entry.getSize();
	if(size>Integer.MAX_VALUE) throw new IOException("zip file too large");
	int len = (int) size;
	getHeaderFields().put("content-length", String.valueOf(len));
	return getJarFile().getInputStream(entry);
    }
    /**
     * Use the original value.
     * @return The last modified time
     */
    public long getLastModified() {
	long lastModified = 0;
	try {
	    if(!connected) connect();
	    if(Logger.getLogLevel()>4) Logger.logDebug("tempfile getLastModified for " + handler);
	    lastModified = jarFileURLConnection.getLastModified();
	} catch (IOException e) {
	    Logger.printStackTrace(e);
	}
	return lastModified;
    }
}
