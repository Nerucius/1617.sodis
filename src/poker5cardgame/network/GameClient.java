package poker5cardgame.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import poker5cardgame.io.NetworkSource;
import poker5cardgame.io.Source;

public class GameClient {

    //Source inSource;
    Source outSource;

    public GameClient() {
    }
    
    public void connect(String IP, int port) {
        try {
            InetAddress address = InetAddress.getByName(IP);
            Socket sock = new Socket(address, port);
            outSource = new NetworkSource(sock);
            //inSource = outSource;
            System.err.println("Client: Connected to Server on IP:" + IP + ".");

        } catch (Exception ex) {
            System.err.println("Client: Failed to connect to Server on IP:" + IP + ".");
            ex.printStackTrace();
        }

    }

    public boolean isConnected(){
        // TODO @alex This does not update correctly when the socket is closed (infinite errors)
        return (outSource != null);
    }
    
    public void close() {
        if (outSource == null) {
            System.err.println("Client: Not connected. Can't close()");
            return;
        }

        try {
            Socket sock = ((NetworkSource) outSource).getCom().getSocket();
            sock.close();
            outSource = null;
            //inSource = null;
        } catch (IOException ex) {
            System.err.println("Client: Error while closing Socket.");
        }
    }

    @Deprecated
    public Source getInSource() {
        throw new UnsupportedOperationException();
        //return inSource;
    }

    public Source getSource() {
        return outSource;
    }

}
