package poker5cardgame.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import static poker5cardgame.Log.*;
import poker5cardgame.game.Card;

public class GameClient {

    private boolean FANCY_GREETING_TIME = true;
    private boolean FANCY_WINNER_TIME = true;
    private boolean FANCY_ADVICES_TIME = true;

    /**
     * The client's source of moves to send.
     */
    Source playerSource;
    Source IOSource;

    GameData clientGameData;
    GameState clientGameState;

    /**
     * Create a client With the given AI type
     */
    public GameClient(ArtificialIntelligence.Type aiType) {
        clientGameData = new GameData();
        clientGameState = new GameState();

        switch (aiType) {
            case AI_RANDOM:
                playerSource = new RandomClientAI(clientGameData, clientGameState);
                break;
            case AI_INTELLIGENT:
                playerSource = new IntelligentClientAI(clientGameData, clientGameState);
                break;
        }
    }

    /**
     * Create a client that reads new commands from the Server.
     */
    public GameClient() {
        this(new KeyboardSource());
        clientGameData = new GameData();
        clientGameState = new GameState();

    }

    /**
     * Create a Client with the given source.
     */
    public GameClient(Source playerSource) {
        this.playerSource = playerSource;
        clientGameData = new GameData();
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
            switch (getState()) {
                case INIT:
                    if (this.FANCY_GREETING_TIME) {
                        this.fancyWelcomeGreetings();
                    }
                    move = this.updateSend();
                    clientGameData.save(move, false);
                    break;

                case START:
                    move = this.updateReceive();
                    clientGameData.save(move, true);
                    break;

                case ACCEPT_ANTE:
                    if(this.FANCY_ADVICES_TIME)
                        this.fancyAccept();
                    move = this.updateSend();
                    clientGameData.save(move, false);
                    break;

                case PLAY:
                    move = this.updateReceive();
                    clientGameState.setServerTurn(move.dealer == 1);
                    this.fancyDealer(move);
                    this.fancyHand(move);
                    clientGameData.save(move, true);
                    break;

                case BETTING:
                    if(clientGameState.isServerTurn())
                        move = this.updateReceive();

                    else 
                    {
                        if(this.FANCY_ADVICES_TIME)
                            this.fancyBetting();
                        
                        move = this.updateSend();                    
                        if(move.action == Action.BET && !ArtificialIntelligence.possibleBet(clientGameData, move.chips, false))
                            throw new Exception("Logic Error. Not valid chips value.");        
                    }
                    clientGameData.save(move, clientGameState.isServerTurn());
                    if(move.action != Action.NOOP && move.action != Action.ERROR)
                        clientGameState.setServerTurn(!clientGameState.isServerTurn());      
                    break;

                case BETTING_DEALER:
                    if(clientGameState.isServerTurn())
                        move = this.updateReceive();

                    else 
                    {
                        if(this.FANCY_ADVICES_TIME)
                            this.fancyBetting();
                        
                        move = this.updateSend();                    
                        if(move.action == Action.BET && !ArtificialIntelligence.possibleBet(clientGameData, move.chips, false))
                            throw new Exception("Logic Error. Not valid chips value.");        
                    }
                    clientGameData.save(move, clientGameState.isServerTurn());
                    if(move.action != Action.NOOP && move.action != Action.ERROR)
                        clientGameState.setServerTurn(!clientGameState.isServerTurn());   
                    break;

                case COUNTER:
                    if(clientGameState.isServerTurn())
                        move = this.updateReceive();
                    else 
                    {
                        if(this.FANCY_ADVICES_TIME)
                            this.fancyBetting();
                        
                        move = this.updateSend();                    
                        if(move.action == Action.RAISE && !ArtificialIntelligence.possibleRaise(clientGameData, move.chips, false))
                            throw new Exception("Logic Error. Not valid chips value.");
                        if(move.action == Action.CALL && !ArtificialIntelligence.possibleCall(clientGameData, false))
                            throw new Exception("Logic Error. Not valid chips value.");                           
                    }
                    if(move.action == Action.FOLD)
                        clientGameState.setFold(true);
                    clientGameData.save(move, clientGameState.isServerTurn());
                    if(move.action != Action.NOOP && move.action != Action.ERROR)
                        clientGameState.setServerTurn(!clientGameState.isServerTurn());
                    break;

                case DRAW:
                    //this.fancyDraw();
                    move = this.updateSend();                       
          
                    // If the cards are not matching the hand cards, the discard method throws an exception.
                    if(move.cDrawn > 0)                   
                        clientGameData.cHand.discard(move.cards);                      
                    clientGameData.save(move, false);
                    break;

                case DRAW_SERVER:
                    move = this.updateReceive();
                    clientGameState.setServerTurn(clientGameData.dealer == 1);
                    //this.fancyDrawServer(move);
                    clientGameData.save(move, true);
                    break;

                case SHOWDOWN:
                    move = this.updateReceive();
                    System.out.println("----[DEBUG] SHOWNDOWN client move: " + move);
                    
                    if(!clientGameState.isFold() && clientGameState.isShowTime())
                    {
                        try{clientGameData.sHand.putCards(move.cards);}catch(Exception e){}
                    
                        // Manage winner with handranker
                        clientGameData.sHand.generateRankerInformation();
                        clientGameData.cHand.generateRankerInformation();
                        if (clientGameData.sHand.wins(clientGameData.cHand))
                            move.winner = 0;
                        else if(clientGameData.cHand.wins(clientGameData.sHand))
                            move.winner = 1;
                        else
                            move.winner = 2;                  

                        System.out.println("Entra aqui i el winner es = " + move.winner + move.action);
                        this.fancyWinner(move);
                        clientGameState.setShowTime(false);
                    }             
                    clientGameState.setFold(false);

                    if(!ArtificialIntelligence.possibleNewRound(clientGameData))
                        move.action = Action.TERMINATE;
                    
                    clientGameData.save(move, true);
                    break;

                case QUIT:
                    this.fancyGoodByeGreetings();
                    close();
                    break;
            }

            clientGameState.apply(move.action);

            GAME_DEBUG("GameClient: Processed move " + move);
            if(move.action == Action.NOOP) GAME_DEBUG("\n\n\nNOOOOOOOOOOOOOOOOOOOOOOOOOOP\n\n\n");

        } catch (Exception e) {
            GAME_DEBUG("GameClient: Exception");
            e.printStackTrace();
            System.out.println("LIADA PARDA: " + e.getMessage());
        }
        GAME_DEBUG("GameClient: Updated State: " + getState());
    }

    public Move updateSend() {
        System.out.println("\nClient Data: " + clientGameData + clientGameState +"\n");

        /* Begin: Talking with the client */
        this.fancyMoves();
        /* End: Talking with the client */

        GAME_DEBUG("GameClient: Waiting for next client move...");
        Move next = playerSource.getNextMove();
        
        if(!clientGameState.getValidActions().contains(next.action))
            next.action = Action.NOOP;
        
        IOSource.sendMove(next);
        return next;
    }

    public Move updateReceive() {
        Move reply = IOSource.getNextMove();
        if(reply.action != Action.ERROR)
            this.FANCY_ADVICES_TIME = true;  

        GAME_DEBUG("GameClient: Received Move: " + reply);
        playerSource.sendMove(reply);
        return reply;
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
        if (IOSource == null) {
            return false;
        }

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

    public GameState.State getState() {
        return clientGameState.state;
    }

    private void fancyMoves() {
        String fancyStr = "";
        List<Action> validActions = clientGameState.getValidActions();
        String[] array = new String[validActions.size()];
        for (int i = 0; i < validActions.size(); i++) {
            array[i] = validActions.get(i).name();
        }

        String fancyMoves = String.join(", ", array);

        FANCY_CLIENT("Your next move possibilities are: ");
        FANCY_CLIENT(fancyMoves + "\n", Format.GREEN);
    }
    
    private void fancyWelcomeGreetings() {
        FANCY_CLIENT("\nHI FRIEND!\nWELCOME TO THE ", Format.BOLD);
        FANCY_CLIENT_RAINBOW("PRETTIEST");
        FANCY_CLIENT(" POKER GAME!\n\n", Format.BOLD);
        this.FANCY_GREETING_TIME = false;
    }
    
    private void fancyGoodByeGreetings() {
        FANCY_CLIENT("\nGOODBYE FRIEND!\nI HOPE YOU ENJOYED THE ", Format.BOLD);
        FANCY_CLIENT_RAINBOW("PRETTIEST");
        FANCY_CLIENT(" POKER GAME!\nWISH TO SEE YOU SOON!\n\n", Format.BOLD);
        this.FANCY_GREETING_TIME = false;
    }
    
    private void fancyAccept() {
        FANCY_CLIENT("These are the game conditions:\n");
        FANCY_CLIENT("Your initial chips: ");
        FANCY_CLIENT(clientGameData.cChips + "\n", Format.BOLD, Format.BLUE);
        FANCY_CLIENT("Your opponents initial chips: ");
        FANCY_CLIENT(clientGameData.sChips + "\n", Format.BOLD, Format.PURPLE);
        FANCY_CLIENT("The required initial bet to play: ");
        FANCY_CLIENT(clientGameData.initialBet + "\n", Format.BOLD);
        FANCY_CLIENT("Do you want to play?\n");
        this.FANCY_ADVICES_TIME = false;
    }

    private void fancyDealer(Move move) {
        if (clientGameState.isServerTurn()) {
            FANCY_CLIENT("You are the game dealer, so the server begins the betting round.\n");
        } else {
            FANCY_CLIENT("The server is the game dealer, so you begin the betting round.\n");
        }
    }

    private void fancyHand(Move move)
    {
        FANCY_CLIENT("These are your cards: ");
        this.fancyCards(move.cards);
    }
    
    private void fancyDraw()
    {
        FANCY_CLIENT("Remember your cards: ");
        Card[] cards = new Card[5];
                            //gameData.sHand.getCards().toArray(sMove.cards);
                            clientGameData.cHand.getCards().toArray(cards);
        //clientGameData.cHand.dumpArray(cards);
        this.fancyCards(cards);
        FANCY_CLIENT("Do you want to discard some cards? Remember the card codes:\n");
        this.fancyCodeCards(cards);
    }
    
    private void fancyDrawServer(Move move)
    {
        FANCY_CLIENT("These are your new cards: ");
        this.fancyCards(move.cards);
        FANCY_CLIENT("The server discarted " + move.sDrawn + " cards.\n");
    }
    
    private void fancyBetting() {
        FANCY_CLIENT("Oh! You have to take a very difficult decision!\n");
        FANCY_CLIENT("Remember some important informations:\n");
        FANCY_CLIENT("Your actual bet: ");
        FANCY_CLIENT(clientGameData.cBet + "   ", Format.BOLD, Format.BLUE);
        FANCY_CLIENT("Your remaining chips: ");
        FANCY_CLIENT(clientGameData.cChips + "\n", Format.BOLD, Format.BLUE);
        FANCY_CLIENT("Your opponents bet: "); // TODO aixo ho sap el client?
        FANCY_CLIENT(clientGameData.sBet + "   ", Format.BOLD, Format.PURPLE);
        FANCY_CLIENT("Your opponents remaining chips: ");
        FANCY_CLIENT(clientGameData.sChips + "\n", Format.BOLD, Format.PURPLE);
        this.FANCY_ADVICES_TIME = false;
    }

    private void fancyCards(Card... cards) {        
        String fancyCards = "";
        for(Card card: cards)        
            fancyCards += card.getRankCode() + Format.getCodeFromName(card.getSuit().name()) + " ";
        FANCY_CLIENT(fancyCards + "\n");
    }
    
    private void fancyCodeCards(Card... cards) {        
        String fancyCards = "";
        for(Card card: cards)        
            fancyCards += card.getRankCode() + Format.getCodeFromName(card.getSuit().name()) + " (code = " +card.getCode() + ") ";
        FANCY_CLIENT(fancyCards + "\n");
    }
    
    private void fancyWinner(Move move) {
        switch (move.winner) {
            case 0:
                FANCY_CLIENT("SNIF SNIF SNIF\n", Format.CYAN);
                FANCY_CLIENT("Oh... You lose this time. "
                        + "But for sure you'll do it much better the next time! "
                        + "Do you want to continue playing?\n");
                break;
            case 1:
                FANCY_CLIENT_RAINBOW("*****************************\n");
                FANCY_CLIENT_RAINBOW("****** CONGRATULATIONS ******\n");
                FANCY_CLIENT_RAINBOW("***** YOU WIN THIS TIME *****\n");
                FANCY_CLIENT_RAINBOW("*****************************\n");
                break;
            case 2:
                FANCY_CLIENT_RAINBOW("***************TIE**************\n");
                break;
            default:
                break;
        }
        this.FANCY_WINNER_TIME = false;
    }   
}
