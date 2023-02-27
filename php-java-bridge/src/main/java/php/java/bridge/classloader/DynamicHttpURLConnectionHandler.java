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
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.jar.JarFile;

import php.java.bridge.util.Logger;

class DynamicHttpURLConnectionHandler extends URLStreamHandler {

    private JarFile jarFile;
    private File baseFile;
    private Map headerFields;

    public DynamicHttpURLConnectionHandler() {
	if(Logger.getLogLevel()>4) Logger.logDebug("tempfile create DynamicHttpURLConnectionHander " + this);
    }
    protected URLConnection openConnection(URL u) throws IOException {
	return new DynamicJarURLConnection(u, this);
    }

    public void deleteTempFile() {
	if(Logger.getLogLevel()>4) Logger.logDebug("classloader tempfile deleted: " + baseFile + " handler: " + this);
	baseFile.delete();
    }
    public Map getHeaderFields() {
	return headerFields;
    }
    public JarFile getTempFile() {
	return jarFile;
    }
    public void setTempFile(JarFile jarFile, File baseFile) {
	if(Logger.getLogLevel()>4) Logger.logDebug("classloader tempfile created: " + baseFile + " handler" + this);
	this.jarFile = jarFile;
	this.baseFile = baseFile;
    }
    public void setHeaderFields(Map headerFields) {
	this.headerFields = headerFields;
    }
}
