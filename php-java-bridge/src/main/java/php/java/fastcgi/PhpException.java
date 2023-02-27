package php.java.fastcgi;

/** A generic PHP exception */
public class PhpException extends Exception {
    private static final long serialVersionUID = 767047598257671018L;
    private String errorString;
    /** 
     * Create a PHP exception 
     * @param errorString the PHP error string 
     */
    public PhpException(String errorString) {
	super(errorString);
	this.errorString = errorString;
    }
    /** 
     * Return the error string
     * @return the PHP error string
     */
    public String getError() {
	return errorString;
    }
}