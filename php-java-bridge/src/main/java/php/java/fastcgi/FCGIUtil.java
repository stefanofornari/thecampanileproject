/*-*- mode: Java; tab-width:8 -*-*/

package php.java.fastcgi;

/**
 * A procedure class which can be used to capture the HTTP header strings.
 * Example:<br>
 * <code>
 * Util.parseBody(buf, natIn, out, new Util.HeaderParser() {protected void parseHeader(String header) {System.out.println(header);}});<br>
 * </code>
 * @author jostb
 * @see FCGIHeaderParser#parseBody(byte[], InputStream, OutputStreamFactory, FCGIHeaderParser)
 */

public class FCGIUtil {

    /**
     * IO buffer size
     */
    public static final int FCGI_BUF_SIZE = 65535;

    /**
     * header length
     */
    public static final int FCGI_HEADER_LEN = 8;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_BEGIN_REQUEST =      1;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_ABORT_REQUEST =      2;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_END_REQUEST   =      3;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_PARAMS        =      4;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_STDIN         =      5;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_STDOUT        =      6;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_STDERR        =      7;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_DATA          =      8;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_GET_VALUES    =      9;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_GET_VALUES_RESULT = 10;
    /**
     * Values for type component of FCGI_Header
     */
    public static final int FCGI_UNKNOWN_TYPE      = 11;
    /**
     * Values for type component of FCGI_Header
     */
    public static final byte[] FCGI_EMPTY_RECORD = new byte[0];
    
    /**
     * Mask for flags component of FCGI_BeginRequestBody
     */
    public static final int FCGI_KEEP_CONN  = 1;

    /**
     * Mask for flags component of FCGI_BeginRequestBody
     */
    public static final int FCGI_END_CONN  = 0;

    /**
     * Values for role component of FCGI_BeginRequestBody
     */
    public static final int FCGI_RESPONDER  = 1;
    /**
     * Values for role component of FCGI_BeginRequestBody
     */
    public static final int FCGI_AUTHORIZER = 2;
    /**
     * Values for role component of FCGI_BeginRequestBody
     */
    public static final int FCGI_FILTER     = 3;

    /**
     * The Fast CGI default port
     */ 
    public static final int FCGI_PORT = 9667;

    /**
     * This controls how many child processes the PHP process spawns.
     * Default is 5. The value should be less than THREAD_POOL_MAX_SIZE
     * @see php.java.bridge.util.Util#THREAD_POOL_MAX_SIZE
     */
    public static final String PHP_FCGI_CONNECTION_POOL_SIZE = "5"; // should be less than Util.THREAD_POOL_MAX_SIZE;

    /**
     * This controls how long the pool waits for a PHP script to terminate.
     * Default is -1, which means: "wait forever".
     */
    public static final String PHP_FCGI_CONNECTION_POOL_TIMEOUT = "-1"; // no timeout

    /**
     * This controls how many requests each child process will handle before
     * exitting. When one process exits, another will be created. Default is 5000.
     */
    public static final String PHP_FCGI_MAX_REQUESTS = "500"; //FIXME set to 5000 before release
    
    
    

}
