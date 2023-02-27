package php.java.bridge.util;

/** 
 * Convenience daemon thread class
  */
public class Thread extends java.lang.Thread {
    /**Create a new thread */
    public Thread() {
        super();
        initThread();
    }
    /**Create a new thread 
     * @param name */
    public Thread(String name) {
        super(name);
        initThread();
    }
    /**Create a new thread 
     * @param target */
    public Thread(Runnable target) {
        super(target);
        initThread();
    }
    /**Create a new thread 
     * @param group 
     * @param target */
    public Thread(ThreadGroup group, Runnable target) {
        super(group, target);
        initThread();
    }
    /**Create a new thread 
     * @param group 
     * @param name */
    public Thread(ThreadGroup group, String name) {
        super(group, name);
        initThread();
    }
    /**Create a new thread 
     * @param target 
     * @param name */
    public Thread(Runnable target, String name) {
        super(target, name);
        initThread();
    }
    /**Create a new thread 
     * @param group 
     * @param target 
     * @param name */
    public Thread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        initThread();
    }
    /**Create a new thread 
     * @param group 
     * @param target 
     * @param name 
     * @param stackSize */
    public Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
        initThread();
    }
    private void initThread() {
        setDaemon(true);
    }
}