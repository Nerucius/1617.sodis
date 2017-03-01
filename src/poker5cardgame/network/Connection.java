package poker5cardgame.network;

import java.net.Socket;

public class Connection {

    int port;
    Socket socket;
    ConnectionListener listener = null;

    public Connection(Socket sock) {
        this.socket = sock;
    }

    public void sendTCP(Network.Command packet) {
        // TODO Send packet over the net
    }

    public void registerListener(ConnectionListener listener) {
        this.listener = listener;
    }

    // TODO Link with Listener to recieve packets
}
