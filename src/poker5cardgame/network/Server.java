package poker5cardgame.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import poker5cardgame.game.Game;
import poker5cardgame.io.NetworkSource;

public class Server implements Runnable {

    ServerSocket serverSocket = null;

    public Server() {

    }

    public void bind(int port) {
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

    public void start() {
        if (serverSocket != null)
            new Thread(this).start();
        else
            System.err.println("Server: Not bound to any port.");
    }

    /**
     * Main method to run while
     */
    @Override
    public void run() {
        System.out.println("Server: Started on port " + serverSocket.getLocalPort());
        try {

            // Main loop for accepting new Connections
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Server: New Client connected from " + client.getInetAddress());
                new Thread(new HandlerRunnable(client)).start();
            }

        } catch (SocketException e) {
            // Thrown when the server socket is closed();
            System.err.println("Server: Closed");
        } catch (IOException e) {
            System.err.println("Server: Failed to accept(). Server Terminated");
        }
    }

    public void close() {
        if (serverSocket == null) {
            System.err.println("Server: Can't close server. Not started.");
            return;
        }

        try {
            serverSocket.close();
            serverSocket = null;
        } catch (Exception e) {
        }

    }

    private class HandlerRunnable implements Runnable {

        NetworkSource source;

        public HandlerRunnable(Socket sock) {
            source = new NetworkSource(sock);
        }

        @Override
        public void run() {

            // Main Server <-> Client Loop
            while (true) {

                Game.Move m = source.getNextMove();
                if (m.action == Game.Action.TERMINATE) {
                    System.out.println("Server: Received TERMINATE packet.");
                    break;
                }
                System.out.println(m.toString());

            }

        }
    }
}
