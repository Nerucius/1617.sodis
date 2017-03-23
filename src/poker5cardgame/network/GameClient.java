package poker5cardgame.network;

import java.lang.IllegalStateException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static poker5cardgame.Log.GAME_DEBUG;
import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.ai.IntelligentClientAI;
import poker5cardgame.ai.RandomClientAI;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;
import poker5cardgame.io.KeyboardSource;
import poker5cardgame.io.NetworkSource;
import poker5cardgame.io.Source;
import static poker5cardgame.Log.GAME_DEBUG;

public class GameClient {

    /**
     * The client's source of moves to send.
     */
    Source playerSource;
    Source IOSource;

    GameState clientGameState; // TODO added to control if send or receive moves

    /**
     * Create a client With the given AI type
     */
    public GameClient(ArtificialIntelligence.Type aiType) {
        GameData gameData = new GameData();
        clientGameState = new GameState();

        switch (aiType) {
            case AI_RANDOM:
                playerSource = new RandomClientAI(gameData, clientGameState);
                break;
            case AI_INTELLIGENT:
                playerSource = new IntelligentClientAI(gameData, clientGameState);
                break;
        }
    }

    /**
     * Create a client that reads new commands from the Server.
     */
    public GameClient() {
        this(new KeyboardSource());
        clientGameState = new GameState();
    }

    /**
     * Create a Client with the given source.
     */
    public GameClient(Source playerSource) {
        this.playerSource = playerSource;
        clientGameState = new GameState();
    }

    public void update() {
        if (!isConnected()) {
            throw new IllegalStateException("Client not connected");
        }

        try {
            Thread.sleep(000);
        } catch (InterruptedException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        GAME_DEBUG("GameClient: Updating with state" + clientGameState);

        Move move = new Move();
        move.action = Action.NOOP;

        try {
            switch (clientGameState.state) {
                case INIT:
                    move = this.updateSend();
                    break;

                case START:
                    move = this.updateReceive();
                    break;

                case ACCEPT_ANTE:
                    move = this.updateSend();
                    break;

                case PLAY:
                    move = this.updateReceive();
                    clientGameState.setServerTurn(move.dealer == 1);
                    break;

                case BETTING:
                    if (clientGameState.isServerTurn()) {
                        move = this.updateReceive();
                    } else {
                        move = this.updateSend();
                    }
                    clientGameState.setServerTurn(!clientGameState.isServerTurn());
                    break;

                case BETTING_DEALER:
                    if (clientGameState.isServerTurn()) {
                        move = this.updateReceive();
                    } else {
                        move = this.updateSend();
                    }
                    clientGameState.setServerTurn(!clientGameState.isServerTurn());
                    break;

                case COUNTER:
                    if (clientGameState.isServerTurn()) {
                        move = this.updateReceive();
                    } else {
                        move = this.updateSend();
                    }
                    clientGameState.setServerTurn(!clientGameState.isServerTurn());
                    break;

                case DRAW:
                    move = this.updateSend();
                    break;

                case DRAW_SERVER:
                    move = this.updateReceive();
                    break;

                case SHOWDOWN:
                    move = this.updateReceive();
                    break;     
                    
                case QUIT:
                    close(); // TODO com tanco la connexio?
                    break;
            }
            
            clientGameState.apply(move.action);
            GAME_DEBUG("GameClient: Processed move " + move);
            
        } catch (Exception e) {
            GAME_DEBUG("GameClient: Exception");
            
            //this.sendErrorMsg(e.getMessage());
        }
        GAME_DEBUG("GameClient: Updated State: " + clientGameState.state);
    }

    public Move updateSend() {
        
        GAME_DEBUG("GameClient: Waiting for next client move...");
        //Move next = this.getValidClientMove(playerSource);
        Move next = playerSource.getNextMove();
        IOSource.sendMove(next);
        return next;
    }

    public Move updateReceive() {
      
        //Move reply = this.getValidServerMove(IOSource);
        Move reply = IOSource.getNextMove();
        GAME_DEBUG("GameClient: Received Move: " + reply);
        playerSource.sendMove(reply);
        return reply;
    }

    private void sendErrorMsg(String msg) {
        try {
            Move errMove = new Move();
            errMove.action = Action.ERROR;
            errMove.error = msg;
            IOSource.sendMove(errMove);
        } catch (Exception e) {
            /* Ignored */ }
    }

    private Move getValidClientMove(Source src) {

        Move move = src.getNextMove();
        while (!clientGameState.getValidActions().contains(move.action)) {
            // send client error message
            this.sendErrorMsg("Protocol Error. Received move: " + move.action 
                    + ". Expecting moves: " + clientGameState.getValidActions());
            
            // get the error response of the server
            //this.updateReceive();
            
            GAME_DEBUG("GameClient: Waiting for next client move...");
            move = src.getNextMove();
        }
        return move;
    }
    
    private Move getValidServerMove(Source src) throws Exception {
        Move move = src.getNextMove();
        if(move.action.equals(Action.ERROR))
            throw new Exception();
        return move;
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
        if(IOSource == null)
            return false;
        
        boolean isSocketClosed = ((NetworkSource) (IOSource)).getCom().getSocket().isClosed();
        if (IOSource != null && !isSocketClosed) {
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
