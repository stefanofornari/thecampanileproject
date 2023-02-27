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

import java.io.IOException;

import php.java.bridge.JavaBridge;


/**
 * <p>
 * A bridge pattern which allows us to vary the class loader as run-time.
 * The decision is based on whether we are allowed to use a dynamic
 * classloader or not (cl==null) or security exception at run-time.
 * </p>
 * <p>
 * The SimpleJavaBridgeClassLoader should be used outside of the J2EE environment. 
 * If you need to switch from a servlet to a ContextRunner thread, use the JavaBridgeClassLoader instead.
 * </p>
 * @see php.java.bridge.JavaBridgeClassLoader
 */
public class SimpleJavaBridgeClassLoader {

    protected ClassLoader scl = null;
    
    /**
     * Return the thread context class loader
     * @return The context class loader
     */
    public static final ClassLoader getContextClassLoader() {
        ClassLoader loader = null;
        try {loader = Thread.currentThread().getContextClassLoader();} catch (SecurityException ex) {/*ignore*/}
	if(loader==null) loader = JavaBridge.class.getClassLoader();
        return loader;
    }

    /**
     * Create a new JavaBridgeClassLoader
     * @param xloader The delegate
     */
    public SimpleJavaBridgeClassLoader(ClassLoader xloader) {
	scl = xloader; 
    }
    protected boolean checkCl() {
      	return false;
    }
    private void throwNotAvailableException() {
	throw new SecurityException("java_require() not supported by SimpleJavaBridgeClassLoader. Enable the Pipe- or SocketContextServer and try again.");
    }
	
    /**
     * Set a DynamicJavaBridgeClassLoader.
     * @param loader The dynamic class loader
     * @throws SecurityException 
     */
    protected void setClassLoader(DynamicJavaBridgeClassLoader loader) {
	throwNotAvailableException();
    }
    /**
     * Append the path to the current library path
     * @param path A file or url list, separated by ';' 
     * @param extensionDir Usually ini_get("extension_dir"); 
     * @param searchpath 
     * @param cwd 
     * @throws IOException 
     */
    public void updateJarLibraryPath(String path, String extensionDir, String cwd, String searchpath) throws IOException  {
	throwNotAvailableException();
    }

    /**
     * Only for internal use
     * @return the classloader
     */
    public ClassLoader getClassLoader() {
	return getDefaultClassLoader();
    }
    
    /**
     * Return the default class loader
     * @return The default class loader
     */
    public ClassLoader getDefaultClassLoader() {
	return scl;
    }
    
    /**
     * Reset the loader.
     * This is only called by the API function "java_reset()", used only for testing.
     */
    protected void doReset() {
    }
    /**
     * reset loader to the initial state
     * This is only called by the API function "java_reset()", used only for testing.
     */
    public void reset() {
    }

    /**
     * Load a class.
     * @param name The class, for example java.lang.String
     * @return the class
     * @throws ClassNotFoundException
     */
    public Class forName(String name) throws ClassNotFoundException {
    	return Class.forName(name, false, scl);
    }
    protected void doClear() {
    }
    /**
     * clear caches and the input vectors
     */
    public void clear() {
    }
    
    /** re-initialize for keep alive */
    protected void recycle() {
    }
    /**
     * Switch the current thread context class loader.
     * @throws IllegalStateException A SimpleJavaBridgeClassLoader cannot be used in a web environment.
     */
    public void switcheThreadContextClassLoader() {
	throwNotAvailableException();
    }
}