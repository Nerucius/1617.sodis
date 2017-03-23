package poker5cardgame.network;

import java.lang.IllegalStateException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.ai.IntelligentClientAI;
import poker5cardgame.ai.RandomClientAI;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.Move;
import poker5cardgame.io.KeyboardSource;
import poker5cardgame.io.NetworkSource;
import poker5cardgame.io.Source;

public class GameClient {
    
    /** The client's source of moves to send. */
    Source playerSource;
    Source IOSource;
    
    /** Create a client With the given AI type */
    public GameClient(ArtificialIntelligence.Type aiType) {
        GameData data = new GameData();
        GameState state = new GameState();
        
        switch (aiType) {
            case AI_RANDOM:
                playerSource = new RandomClientAI(data, state);
                break;
            case AI_INTELLIGENT:
                playerSource = new IntelligentClientAI(data, state);
                break;
        }
    }
    
    /** Create a client that reads new commands from the Server. */
    public GameClient() {
        this(new KeyboardSource());
    }
    
    /** Create a Client with the given source. */
    public GameClient(Source playerSource){
        this.playerSource = playerSource;
    }
    
    public void update(){
        if(!isConnected())
            throw new IllegalStateException("Client not connected");
        
        // Get the next move from the local player
        Move next = playerSource.getNextMove();
        IOSource.sendMove(next);
        
        // Inform the local player of the other player's move
        // This local player might be the Keyboard or the AI
        // in the case of the AI, it will save the Game State
        Move reply = IOSource.getNextMove();
        playerSource.sendMove(reply);
        
    }

    public void connect(String IP, int port) {
        try {
            InetAddress address = InetAddress.getByName(IP);
            Socket sock = new Socket(address, port);
            IOSource = new NetworkSource(sock);
            //inSource = outSource;
            System.err.println("Client: Connected to Server on IP:" + IP + ".");

        } catch (Exception ex) {
            System.err.println("Client: Failed to connect to Server on IP:" + IP + ".");
            ex.printStackTrace();
        }

    }

    public boolean isConnected() {
        boolean isSocketClosed = ((NetworkSource)(IOSource)).getCom().getSocket().isClosed();
        if (IOSource != null && !isSocketClosed){
            return true;
        }
        IOSource = null;
        return false;
    }

    public void close() {
        if (IOSource == null) {
            System.err.println("Client: Not connected. Can't close()");
            return;
        }

        try {
            Socket sock = ((NetworkSource) IOSource).getCom().getSocket();
            sock.close();
            IOSource = null;
            //inSource = null;
        } catch (IOException ex) {
            System.err.println("Client: Error while closing Socket.");
        }
    }

    public Source getSource() {
        return IOSource;
    }

}
