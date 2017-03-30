package poker5cardgame.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import static poker5cardgame.Log.*;

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
            NET_ERROR("Server: Already bound to a port");
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            NET_ERROR("Server: Failed to Open Socket.");
        }
    }

    @Override
    public final void start() {
        if (serverSocket != null)
            new Thread(this).start();
        else
            NET_ERROR("Server: Not bound to any port.");
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
        NET_DEBUG("Server: Started on port " + serverSocket.getLocalPort());
        try {

            // Main loop for accepting new Connections
            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                NET_DEBUG("Server: New Client connected from " + client.getInetAddress());
                new Thread(() -> {
                    handleConnection(client);
                }).start();
            }

        } catch (SocketException e) {
            // Thrown when the server socket is closed();
            NET_ERROR("Server: Closed");

        } catch (IOException e) {
            NET_ERROR("Server: Failed to accept(). Server Terminated");

        } catch (Exception e) {
            NET_ERROR("Server: Unknown exception. Server Terminated");
            e.printStackTrace();

        } finally {
            // Delete ServerSocket
            serverSocket = null;
        }
    }

    public final void close() {
        if (serverSocket == null) {
            NET_ERROR("Server: Can't close server. Not started.");
            return;
        }

        try {
            serverSocket.close();
        } catch (Exception e) {
        }

    }
}
