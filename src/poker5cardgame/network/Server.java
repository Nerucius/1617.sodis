package poker5cardgame.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private int port;
    private boolean isRunning;
    private Thread mainThread;
    private ServerSocket socket;
    private List<Connection> clients;

    public Server() {
        clients = new ArrayList<>();
    }

    public void start() {
        mainThread = new Thread(() -> {
            try {
                socket = new ServerSocket(port);
                isRunning = true;
                while (true) {
                    Socket newSocket = socket.accept();
                    // TODO Process new Connection here
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        // TODO Disconnect all clients, stop client threads
    }

}
