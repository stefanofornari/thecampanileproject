package php.java.bridge.classloader;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.WeakHashMap;
//* <p>Title: php-java-bridge</p>

import php.java.bridge.http.SessionFactory;
import php.java.bridge.util.Logger;

/**
 * <p>
 * This class implements a class loader, which keeps track of a dynamic list of other delegate URLClassLoaders
 * It is possible to change the list of these Classloaders during execution. The classloaders themselves, as well as
 * their corresponding classes are globally cached.
 * </p>
 * <p>
 * In case of file:// URLs, this classloader also handles reloading of Jar-Files once they are modified.
 * This is possible <b>without restarting the JVM</b>
 * It keeps track of the latest file modification times, and reloads Jar files if neccessary.
 * It is also possible to add an URL pointing to a directory of simple class files.
 * This is slow, though, and only recommended for quick and dirty development use since
 * it will *always* reload
 * </p>
 * <p>Copyright: PHP-License</p>
 * <p>http://sourceforge.net/projects/php-java-bridge</p>
 * @author Kai Londenberg
 * @version 2.06
 */
public class DynamicClassLoader extends SecureClassLoader {

    protected static Map classLoaderCache = Collections.synchronizedMap(new HashMap()); // Global Cache Map of Classpath=>Soft Reference=>URLClassLoaderEntry
    protected static Map parentCacheMap = new WeakHashMap(); // Holds global caches for parent Classloaders
    /**
     * By default minumum file modification check interval is 2 seconds, that should be fast enough :)
     */
    public static long defaultCacheTimeout = 2000;
    /**
     *  By default lazy classpath addition
     */
    public static boolean defaultLazy = true; 
    protected static final String nf = "not found"; // Dummy entry for cache maps if a class or resource can't be found.
    private static int instanceCount = 0;
    private static long debugStart = System.currentTimeMillis();
    private static String ENTRY_SEPARATOR=";";
    
    protected String arrayToString(URL[] array) {
	StringBuffer buf = new StringBuffer();
	for(int i=0; i<array.length; i++) {
	    buf.append(String.valueOf(array[i]));
	    if(i+1!=array.length) buf.append(ENTRY_SEPARATOR);
	}
	return buf.toString();
    }

    protected class URLClassLoaderEntry {
	URLClassLoader cl;
	long lastModified;
	HashMap cache = new HashMap(); // Cache for this URLClassLoader
	public String toString() {
    	    return String.valueOf(arrayToString(cl.getURLs()));
    	}
	protected URLClassLoaderEntry (URLClassLoader cl, long lastModified) {
	    this.cl = cl;
	    this.lastModified = lastModified;
	}
    }

    static void debugMsg(String str) {
	if(Logger.getLogLevel()>5) Logger.logDebug((System.currentTimeMillis()-debugStart)+"::"+str);
    }

    static void clearCache() {
	classLoaderCache.clear();
    }

    /**
     * Invalidates a given classpath, so that the corresponding classloader gets reloaded.
     * @param urls The urls.
     */
    public static void invalidate(URL urls[]) {
	invalidate(getStringFromURLArray(urls));
    }

    /**
     * Invalidates a given classpath, so that the corresponding classloader gets reloaded.
     * This method should be called from PHP to signal that a given codebase has been modified.
     * @param classpath
     */
    public static void invalidate(String classpath) {
	if(Logger.getLogLevel()>5) Logger.logDebug("DynamicClassLoader.invalidate("+classpath+")\n");
	classLoaderCache.remove(classpath);
    }

    final static String getStringFromURLArray(URL urls[]) {
	if (urls.length==0) return "";
	StringBuffer cp = new StringBuffer(urls[0].toExternalForm());
	for (int i=1;i<urls.length;i++) {
	    cp.append(';');
	    cp.append(urls[i].toExternalForm());
	}
	return cp.toString();
    }

    final static URL[] getURLArrayFromString(String cp) throws MalformedURLException {
	StringTokenizer st = new StringTokenizer(cp, ";", false);
	ArrayList urls = new ArrayList();
	while (st.hasMoreTokens()) {
	    String urlStr = st.nextToken();
	    URL u = new URL(urlStr);
	    urls.add(u);
	}
	URL u[] = new URL[urls.size()];
	urls.toArray(u);
	return u;
    }

    protected int instanceIndex;
    protected HashMap classLoaders; // Map of Classpath=>URLClassLoaderEntries of this DynamicClassLoader (Hard References)
    protected LinkedList classPaths; // List of Classpaths (corresponding to URLClassLoaderEntries) of this DynamicClassLoader
    protected LinkedList urlsToAdd; // List of URLs to add (lazy evaluation)
    protected long cacheTimeout; // Minimum interval to check for file modification dates
    protected boolean lazy; // Lazy Classloader Creation ?
    protected HashMap parentCache; // Fetched globally from parentCacheMap

    private void init() {
	classLoaders = new HashMap(); // Map of Classpath=>URLClassLoaderEntries of this DynamicClassLoader (Hard References)
	classPaths = new LinkedList(); // List of Classpaths (corresponding to URLClassLoaderEntries) of this DynamicClassLoader
	urlsToAdd = new LinkedList(); // List of URLs to add (lazy evaluation)
    }
    /**
     * We create a new copy of the DynamicClassLoader for each request. 
     * This is necessary to avoid problems with the associated cache (see tests.php5/loader_test.php).
     * To avoid problems with some clone implementations (IBM) we use an empty contstructor and copy all fields ourselfs.
     * @param that The new uninitialized instance of the DynamicClassLoader.
     */
    protected void copyInto(DynamicClassLoader that) {
	that.instanceIndex = instanceIndex;
	that.classLoaders = classLoaders;
	that.classPaths = classPaths;
	that.urlsToAdd = urlsToAdd;
	that.cacheTimeout = cacheTimeout;
	that.lazy = lazy;
	that.parentCache = parentCache;
    }
    protected DynamicClassLoader(DynamicClassLoader other) {
	super(other.getParent());
    }
    protected DynamicClassLoader(ClassLoader parent) {
	super(parent);
	init();
	this.cacheTimeout = defaultCacheTimeout;
	this.lazy = defaultLazy;
	this.instanceIndex = instanceCount++;
	// Load global cache for the parent
	synchronized(parentCacheMap) {
	    parentCache = (HashMap)parentCacheMap.get(parent);
	    if (parentCache==null) {
		parentCache = new HashMap();
		parentCacheMap.put(parent, parentCache);
	    }
	}
    }

    protected DynamicClassLoader() {
	super();
	init();
	ClassLoader parent = ClassLoader.getSystemClassLoader();
	this.cacheTimeout = defaultCacheTimeout;
	this.lazy = defaultLazy;
	// Load global cache for the parent
	synchronized(parentCacheMap) {
	    parentCache = (HashMap)parentCacheMap.get(parent);
	    if (parentCache==null) {
		parentCache = new HashMap();
		parentCacheMap.put(parent, parentCache);
	    }
	}
    }

    protected void clearLoader() {
      if(Logger.getLogLevel()>5) Logger.logDebug("DynamicClassLoader("+System.identityHashCode(this)+").clear()\n");
	classLoaders.clear();
	classPaths.clear();
	urlsToAdd.clear();
    }

    protected void setLazy(boolean lazy) {
	this.lazy = lazy;
    }

    protected void setCacheTimeout(long cacheTimeoutMilliseconds) {
	this.cacheTimeout = cacheTimeoutMilliseconds;
    }

    protected void addURLs(URL urls[]) {
	addURLs(getStringFromURLArray(urls), urls, lazy);
    }

    protected void addURLs(URL urls[], boolean lazy) {
	addURLs(getStringFromURLArray(urls), urls, lazy);
    }

    protected void addURLs(String urlClassPath) throws MalformedURLException {
	addURLs(urlClassPath, getURLArrayFromString(urlClassPath), lazy);
    }

    protected void addURLs(String urlClassPath, boolean lazy) throws MalformedURLException {
	addURLs(urlClassPath, getURLArrayFromString(urlClassPath), lazy);
    }

    protected void addURL(URL url, boolean lazy) {
	URL u[] = new URL[] {url};
	addURLs(u, lazy);
    }

    protected void addURL(URL url) {
	URL u[] = new URL[] {url};
	addURLs(u, lazy);
    }

    protected void addURLs(String classPath, URL urls[], boolean lazy) {
	if (lazy) {
	    lazyAddURLs(classPath, urls);
	} else {
	    realAddURLs(classPath, urls);
	}
    }

    protected URLClassLoaderEntry realAddURLs(String classPath, URL urls[]) {
        if(Logger.getLogLevel()>5) Logger.logDebug("DynamicClassLoader("+System.identityHashCode(this)+").realAddURLs(\""+classPath+"\","+getStringFromURLArray(urls)+")\n");
	URLClassLoaderEntry entry = getClassPathFromCache(classPath);
	if (entry==null) {
	    entry = createURLClassLoader(classPath, urls);
	} else {
	    long time = System.currentTimeMillis();
	    if (entry.lastModified+cacheTimeout<time) {
		long urlsLastModified = getLastModified(urls);
		if (urlsLastModified>entry.lastModified) {
		    entry = createURLClassLoader(classPath, urls);
		}
	    }
	}
	if (entry!=null) {
	    if (!classLoaders.containsKey(classPath)) { // If already part of our classpath list, don't add duplicate
		classPaths.add(classPath); // Bugfix, how could I miss this one ? (KL)
	    }
	    classLoaders.put(classPath, entry);
	}
	return entry;
    }

    protected void lazyAddURLs(String classPath, URL urls[]) {
        if(Logger.getLogLevel()>5) Logger.logDebug("DynamicClassLoader("+System.identityHashCode(this)+").lazyAddURLs(\""+classPath+"\","+getStringFromURLArray(urls)+")\n");
	Object params[] = new Object[] {classPath, urls};
	urlsToAdd.add(params);
    }

    protected URLClassLoaderEntry addDelayedURLs() {
	if (urlsToAdd.isEmpty()) return null;
	Object params[] = (Object[]) urlsToAdd.getFirst();
	urlsToAdd.removeFirst();
	return realAddURLs((String)params[0], (URL[])params[1]);
    }

    protected long getLastModified(URL urls[]) { // Returns the highest modification date from all URLs
	long lastModified = 0;
	for (int i=0;i<urls.length;i++) {
	    URL u = urls[i];
	    long lm = 0;
	    try {
		if (u.getProtocol().equals("file")) {
		    File f = new File(u.getPath());
		    if (f.isFile()) {
			lm = f.lastModified();
		    } else if (f.isDirectory()) {
			return 0; // Directories and non-file URLs are considered to be never modified, except through a static call to "invalidate"
		    }
		} else {
		    URLConnection conn = u.openConnection();
		    lm = conn.getLastModified();
		}
		if (lm>lastModified) lastModified = lm;
	    } catch (IOException e) {Logger.printStackTrace(e);}
	}
	return lastModified;
    }

    private URLClassLoaderFactory factory = new URLClassLoaderFactory();
    protected static class URLClassLoaderFactory {
	public URLClassLoader createUrlClassLoader(String classPath, URL urls[], ClassLoader parent) {
	    return new URLClassLoader(urls, parent);
	}
    }
    protected void setUrlClassLoaderFactory(URLClassLoaderFactory factory) {
	this.factory = factory;
    }
    /** the reference queue, used to get a notification when a class loader entry has been gc'ed */
    private static final ReferenceQueue TEMP_FILE_QUEUE = new ReferenceQueue();
    /** prevent the soft references to the class loader entries from beeing garbage collected */
    private static final Set DELETE_TEMP_FILE_ACTIONS = Collections.synchronizedSet(new HashSet());
    /** clean up the temp files as soon as their class loader entry doesn't exist anymore */
    private static final class TempFileObserver extends Thread {
	public TempFileObserver(String name) {
	    super(name);
	    setDaemon(true);
	    start();
	}
	public void run() {
            if (Logger.getLogLevel()>5) 
        	    System.out.println ("lifecycle: init observer "+System.identityHashCode(DynamicClassLoader.class));

	    try {
		while (!interrupted()) {
		    DynamicClassLoader.DeleteTempFileAction action = 
			(DynamicClassLoader.DeleteTempFileAction) DynamicClassLoader.TEMP_FILE_QUEUE.remove();
		    action.command();
		    DELETE_TEMP_FILE_ACTIONS.remove(action);
		}
            } catch (InterruptedException e) {
		if (Logger.getLogLevel()>5) 
			System.out.println ("lifecycle: observer got interrupt"+System.identityHashCode(SessionFactory.class));
	    }
            if (Logger.getLogLevel()>5) 
        	    System.out.println ("lifecycle: observer terminating "+System.identityHashCode(DynamicClassLoader.class));
	}
    }
    static final TempFileObserver THE_TEMP_FILE_OBSERVER = new TempFileObserver("JavaBridgeTempFileObserver");
    /** Destroy the temp file observer. Should be called before the
     * class is unloaded (in servlet.destroy() for example). */
    public static final void destroyObserver() {
	THE_TEMP_FILE_OBSERVER.interrupt();
    }
    /** delete all temp files created for this class loader entry */
    private static class DeleteTempFileAction extends SoftReference {
	List handlers;
	public DeleteTempFileAction(Object arg0, ReferenceQueue arg1, List handlers) {
	    super(arg0, arg1);
	    this.handlers = handlers;
	    DELETE_TEMP_FILE_ACTIONS.add(this);
	    if(Logger.getLogLevel()>4) {
		int count = 0, orphaned = 0;
		for(Iterator ii = DELETE_TEMP_FILE_ACTIONS.iterator(); ii.hasNext(); ) {
		    SoftReference val = (SoftReference) ii.next();
		    count++;
		    if(val.get()==null) orphaned++;
		}
		Logger.logDebug("classloader stats: entries: " + count + " orphaned: " + orphaned);
	    }
        }
	public void command() {
	    for(Iterator ii = handlers.iterator(); ii.hasNext(); ) {
		DynamicHttpURLConnectionHandler handler = (DynamicHttpURLConnectionHandler) ii.next();
		handler.deleteTempFile();
	    }
	}	
    }
    /** wrap http urls so that we can check its modification time. The dynamic class loader always fetches
     * the modification time header field and, if the time has changed, fetches the entire jar file again */ 
    private static URL[] rewriteURLs(URL urls[], List handlers) {
	URL[] newUrls = new URL[urls.length];
	for(int i=0; i<urls.length; i++) {
	    URL url = urls[i];
	    String protocol = url.getProtocol();
	    if(!"file".equals(protocol) && !"jar".equals(protocol)) {
		try {
		    DynamicHttpURLConnectionHandler handler = new DynamicHttpURLConnectionHandler();
	            url = new URL("jar", null, -1, url.toExternalForm()+"!/",  handler);
	            handlers.add(handler);
		} catch (MalformedURLException e) {
                    Logger.printStackTrace(e);
                }
	    }
            newUrls[i] = url;
	}
	return newUrls;
    }
    private SoftReference getReference(URLClassLoaderEntry entry, List handlers) {
	if(handlers.isEmpty()) {
	    return new SoftReference(entry);
	} else {
	    return new DeleteTempFileAction(entry, TEMP_FILE_QUEUE, handlers);
	}
    }
    protected URLClassLoaderEntry createURLClassLoader(String classPath, URL urls[]) {
	List handlers = new LinkedList();
	urls = rewriteURLs(urls, handlers);
        if(Logger.getLogLevel()>5) Logger.logDebug("DynamicClassLoader("+System.identityHashCode(this)+").createURLClassLoader(\""+classPath+"\","+getStringFromURLArray(urls)+")\n");
	URLClassLoader cl = factory.createUrlClassLoader(classPath, urls, this.getParent());
	URLClassLoaderEntry entry = new URLClassLoaderEntry(cl, System.currentTimeMillis());
	SoftReference cacheEntry = getReference(entry, handlers);
	classLoaderCache.put(classPath, cacheEntry);
	return entry;
    }

    protected URLClassLoaderEntry getClassPathFromCache(String classPath) {
	Object o = classLoaders.get(classPath);
	if (o==null) {
	    o = classLoaderCache.get(classPath);
	    if (o!=null) {
		o = ((SoftReference)o).get(); // Caching with SoftReferences to avoid OutOfMemoryExceptions
	    }
	}
	return (URLClassLoaderEntry)o;
    }


    protected void addURLClassLoader(String loaderClasspath, URLClassLoader cl, long lastModified) {
	URLClassLoaderEntry entry = (URLClassLoaderEntry)classLoaders.get(loaderClasspath);
	if (entry==null) { // Check for duplicate entry
	    entry = new URLClassLoaderEntry(cl, lastModified);
	    classLoaders.put(loaderClasspath, entry);
	} else { // If neccessary, update
	    if (entry.lastModified<lastModified) {
		entry.cl = cl;
		entry.lastModified = lastModified;
	    }
	}
    }

    /**
     *
     * I have decided to override loadClass instead of findClass,
     * so that this method will actually get to re-load
     * classes if necessary. Otherwise, the Java system would call
     * the final method "getLoadedClass(name)", (i.e. use it's own caching) without
     * dynamically re-loading classes if necessary.
     * @param name The class name
     * @return The class
     * @throws ClassNotFoundException 
     */
    public Class loadClass(String name) throws ClassNotFoundException {
	Class result = null;
	if(Logger.getLogLevel()>5) Logger.logDebug("DynamicClassLoader("+System.identityHashCode(this)+").loadClass("+name+")\n");
	if(Logger.getLogLevel()>5) Logger.logDebug("Trying parent\n");
	Object c = null;
	synchronized(parentCache) {
	    c = parentCache.get(name);
	    if (c!=nf) {
		if (c!=null) return (Class)c;
	    }
	}
	Iterator iter = classPaths.iterator();
	URLClassLoaderEntry e = null;
	while (iter.hasNext()) {
	    e = (URLClassLoaderEntry) classLoaders.get(iter.next());
	    if(Logger.getLogLevel()>5) Logger.logDebug("Trying "+(System.identityHashCode(e.cl)+"\n"));
	    synchronized(e.cache) {
		c = e.cache.get(name);
		if (c!=nf) {
		    if (c!=null) return (Class)c;
		    try {
			result = e.cl.loadClass(name);
			e.cache.put(name, result);
			return result;
		    } catch (ClassNotFoundException cnfe) {
			e.cache.put(name, nf);
		    }
		}
	    }
	}
	e = addDelayedURLs();
	while (e!=null) {
	    if(Logger.getLogLevel()>5) Logger.logDebug("Trying "+(System.identityHashCode(e.cl)+"\n"));
	    synchronized(e.cache) {
		c = e.cache.get(name);
		if (c!=nf) {
		    if (c!=null) return (Class)c;
		    try {
			result = e.cl.loadClass(name);
			e.cache.put(name, result);
			return result;
		    } catch (ClassNotFoundException cnfe) {
			e.cache.put(name, nf);
		    }
		}
	    }
	    e = addDelayedURLs();
	}
	try {
	    result = super.loadClass(name);
	    parentCache.put(name, result);
	    return result;
	} catch (ClassNotFoundException cnfe) {
	    parentCache.put(name, nf);
	}
	if (result==null) {
	    throw new ClassNotFoundException("Class "+name+" not found");
	}
	return result;
    }

    // Not cached
    protected Enumeration findResources(String name) throws java.io.IOException {
	Vector result = new Vector();
	Enumeration enumeration = super.findResources(name);
	while (enumeration.hasMoreElements()) {
	    result.add(enumeration.nextElement());
	}
	Iterator iter = classPaths.iterator();
	URLClassLoaderEntry e = null;
	while (iter.hasNext()) {
	    e = (URLClassLoaderEntry) classLoaders.get(iter.next());
	    enumeration = e.cl.findResources(name);
	    while (enumeration.hasMoreElements()) {
		result.add(enumeration.nextElement());
	    }
	}
	e = addDelayedURLs();
	while (e!=null) {
	    enumeration = e.cl.findResources(name);
	    while (enumeration.hasMoreElements()) {
		result.add(enumeration.nextElement());
	    }
	    e = addDelayedURLs();
	}
	return result.elements();
    }

    protected URL findResource(String name)  {
	String cacheName = "@"+name; // definitely different from class-names
	Object c = null;
	synchronized(parentCache) {
	    c = parentCache.get(cacheName);
	    if ((c!=nf) && (c!=null)) return (URL)c;
	}
	Iterator iter = classPaths.iterator();
	URLClassLoaderEntry e = null;
	while (iter.hasNext()) {
	    e = (URLClassLoaderEntry) classLoaders.get(iter.next());
	    synchronized (e.cache) {
		c = e.cache.get(cacheName);
		if ((c!=nf) && (c!=null)) return (URL)c;
		c = e.cl.findResource(name);
		if (c!=null) {
		    e.cache.put(cacheName, c);
		    return (URL)c;
		} else {
		    e.cache.put(cacheName, nf);
		}
	    }
	}
	e = addDelayedURLs();
	while (e!=null) {
	    synchronized (e.cache) {
		c = e.cache.get(cacheName);
		if ((c!=nf) && (c!=null)) return (URL)c;
		c = e.cl.findResource(name);
		if (c!=null) {
		    e.cache.put(cacheName, c);
		    return (URL)c;
		} else {
		    e.cache.put(cacheName, nf);
		}
	    }
	    e = addDelayedURLs();
	}
	c = super.findResource(name);
	if (c!=null) {
	    parentCache.put(cacheName, c);
	    return (URL)c;
	} else {
	    parentCache.put(cacheName, nf);
	}
	return null;
    }
}
