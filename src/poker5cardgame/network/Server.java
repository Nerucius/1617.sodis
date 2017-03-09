package poker5cardgame.network;

/**
 * Base class for all Server classes. Includes the basic contracts
 * such as binding to a port. Starting up the server and listening to new
 * connections. A run loop and a close method.
 * 
 * @author German Dempere
 */
public interface Server extends Runnable {
    
    public void bind(int port);
    
    public void start();    
    
    public void close();
    
}
