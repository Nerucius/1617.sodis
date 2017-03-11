package poker5cardgame.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Abstract Implementation of a MultiThreaded server. Extend and Implement the
 * Handler method and class
 *
 * @author German Dempere
 */
public abstract class MultithreadServer implements Server {

    private ServerSocket serverSocket = null;

    public MultithreadServer() {
    }

    public final void bind(int port) {
        if (serverSocket != null && serverSocket.isBound()) {
            System.err.println("Server: Already bound to a port");
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.err.println("Server: Failed to Open Socket.");
        }
    }

    @Override
    public final void start() {
        if (serverSocket != null)
            new Thread(this).start();
        else
            System.err.println("Server: Not bound to any port.");
    }

    /**
     * Handle a new Connection. This method is automatically encapsulated inside
     * a new Thread.
     *
     * @param sock New client connection.
     */
    public abstract void handleConnection(Socket sock);

    @Override
    public final void run() {
        System.out.println("Server: Started on port " + serverSocket.getLocalPort());
        try {

            // Main loop for accepting new Connections
            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                System.out.println("Server: New Client connected from " + client.getInetAddress());
                new Thread(() -> {
                    handleConnection(client);
                }).start();
            }

        } catch (SocketException e) {
            // Thrown when the server socket is closed();
            System.err.println("Server: Closed");
            
        } catch (IOException e) {
            System.err.println("Server: Failed to accept(). Server Terminated");
            
        } catch (Exception e){
            System.err.println("Server: Unknown exception. Server Terminated");
            e.printStackTrace();   
            
        } finally {            
            // Delete ServerSocket
            serverSocket = null;
        }
    }

    public final void close() {
        if (serverSocket == null) {
            System.err.println("Server: Can't close server. Not started.");
            return;
        }

        try {
            serverSocket.close();
        } catch (Exception e) {
        }

    }
}
