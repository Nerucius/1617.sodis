package poker5cardgame.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
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
import poker5cardgame.game.Hand;

public class GameClient {

    private boolean FANCY_GREETING_TIME = true;
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

        GAME_DEBUG("GameClient: Updating with state" + clientGameState + " and data" + clientGameData);

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
                    if (clientGameState.isServerTurn()) {
                        move = this.updateReceive();
                        INFO_SERVER(move.toString());
                    }
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
                    if (clientGameState.isServerTurn()) {
                        move = this.updateReceive();
                        INFO_SERVER(move.toString());
                    }
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
                    if (clientGameState.isServerTurn()) {
                        move = this.updateReceive();
                        INFO_SERVER(move.toString());
                    }
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
                    this.fancyDraw();
                    move = this.updateSend();                       
          
                    // If the cards are not matching the hand cards, the discard method throws an exception.
                    if(move.cDrawn > 0)                   
                        clientGameData.cHand.discard(move.cards);   
                    clientGameData.save(move, false);
                    break;

                case DRAW_SERVER:
                    move = this.updateReceive();
                    clientGameState.setServerTurn(clientGameData.dealer == 1);
                    clientGameData.save(move, true);
                    INFO_SERVER(move.toString());
                    this.fancyDrawServer(move);
                    break;

                case SHOWDOWN:
                    move = this.updateReceive();
                    if(clientGameState.isFold())
                        this.fancyFold();
                    
                    if(!clientGameState.isFold() && clientGameState.isShowTime())
                    {
                        clientGameData.save(move, true);
                        this.fancyShowndown();
                        
                        move.winner = ArtificialIntelligence.manageWinner(clientGameData);
                        clientGameData.save(move, true);
                        this.fancyWinner(move);
                        clientGameState.setShowTime(false);
                    }             

                    if(!clientGameState.isShowTime() && !ArtificialIntelligence.possibleNewRound(clientGameData))
                        move.action = Action.TERMINATE;
                    
                    clientGameState.setFold(false);
                    clientGameState.setShowTime(false);
                    clientGameData.save(move, true);
                    break;

                case QUIT:
                    FANCY_CLIENT("There are not enough chips for the initial bet. The game is finished.\n\n");
                    this.fancyGoodByeGreetings();
                    close();
                    break;
            }

            clientGameState.apply(move.action);

            GAME_DEBUG("GameClient: Processed move " + move);

        } catch (Exception ex) {
            FANCY_CLIENT("ERROR: ", Format.BOLD, Format.RED);
            FANCY_CLIENT(ex.getMessage() + '\n', Format.RED);
            FANCY_CLIENT("Oh! It looks like you did an error... Please try again!\n\n");
            ex.printStackTrace();
            close();
        }
        GAME_DEBUG("GameClient: Updated State: " + getState() + " and data" + clientGameData);
    }

    public Move updateSend() {

        /* Begin: Talking with the client */
        this.fancyMoves();
        /* End: Talking with the client */

        GAME_DEBUG("GameClient: Waiting for next client move...");
        Move next = playerSource.getNextMove();    

        if(!clientGameState.getValidActions().contains(next.action) && next.action != Action.NOOP)
        {
            FANCY_CLIENT("INVALID ACTION.\n", Format.BOLD, Format.RED);
            FANCY_CLIENT("Oh! It looks like you entered an ", Format.RED);
            FANCY_CLIENT("invalid action", Format.UNDERLINE, Format.RED);
            FANCY_CLIENT(". Please try again.\n", Format.RED);
            next.action = Action.NOOP;
        }
        
        if(next.action != Action.NOOP)
            INFO_CLIENT(next.toString());
        
        IOSource.sendMove(next);
        return next;
    }

    public Move updateReceive() {
        Move reply = IOSource.getNextMove();
        if(reply.action == Action.TERMINATE)
            close();

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
        FANCY_CLIENT("Do you want to play?\n\n");
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
        System.out.println("");
    }
     
    private Card[] fancyRememberCards()
    {
        FANCY_CLIENT("Remember your cards: ");
        Card[] cards = new Card[5];
        clientGameData.cHand.getCards().toArray(cards);
        this.fancyCards(cards);
        return cards;
    }
    
    private void fancyDraw()
    {
        Card[] cards = this.fancyRememberCards();
        FANCY_CLIENT("Do you want to discard some cards? Remember the card codes:\n");
        this.fancyCodeCards(cards);
    }
    
    private void fancyDrawServer(Move move)
    {
        if(clientGameData.cDrawn > 0)
        {
            FANCY_CLIENT("These are your new cards: ");
            this.fancyCards(move.cards);
        }
        FANCY_CLIENT("The server discarted " + move.sDrawn + " cards.\n\n");
    }
    
    private void fancyBetting() {
        this.fancyRememberCards();
        FANCY_CLIENT("Oh! You have to take a very difficult decision!\n");
        FANCY_CLIENT("Remember some important informations:\n");
        FANCY_CLIENT("Your actual bet: ");
        FANCY_CLIENT(clientGameData.cBet + "   ", Format.BOLD, Format.BLUE);
        FANCY_CLIENT("\nYour remaining chips: ");
        FANCY_CLIENT(clientGameData.cChips + "\n", Format.BOLD, Format.BLUE);
        FANCY_CLIENT("Your opponents bet: "); 
        FANCY_CLIENT(clientGameData.sBet + "   ", Format.BOLD, Format.PURPLE);
        FANCY_CLIENT("\nYour opponents remaining chips: ");
        FANCY_CLIENT(clientGameData.sChips + "\n\n", Format.BOLD, Format.PURPLE);
        this.FANCY_ADVICES_TIME = false;
    }
    
    private void fancyFold() {
        FANCY_CLIENT("Ops! It looks like you don't have a good hand...\nI wish you good luck for the next round!\n");
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
        FANCY_CLIENT(fancyCards + "\n\n");
    }
    
    private void fancyShowndown()
    {
        //try{
        Card[] cHand = new Card[Hand.SIZE];
        clientGameData.cHand.getCards().toArray(cHand);
        Card[] sHand = new Card[Hand.SIZE];
        clientGameData.sHand.getCards().toArray(sHand);
        FANCY_CLIENT("The game round ended here! Let's see the results...\n");
        FANCY_CLIENT("Your final hand: ");
        this.fancyCards(cHand);
        FANCY_CLIENT("Server's final hand: ");
        this.fancyCards(sHand);
        //} catch(Exception ex){}
        
    }
    private void fancyWinner(Move move) {
        switch (move.winner) {
            case 0:
                FANCY_CLIENT("\n----- YOU LOSE THIS TIME -----\n\n", Format.BOLD, Format.BLUE);
                FANCY_CLIENT("Oh... You lose this time. "
                        + "But for sure you'll do it much better the next time! "
                        + "Do you want to continue playing?\n\n");
                break;
            case 1:
                FANCY_CLIENT_RAINBOW("\n***** CONGRATULATIONS :) YOU WIN THIS TIME *****\n\n");
                break;
            case 2:
                FANCY_CLIENT("\n~~~~~ TIE ~~~~~\n\n", Format.BOLD, Format.GREEN);
                FANCY_CLIENT("How amazing! You both are equal so good!\n\n");
                break;
            default:
                break;
        }
    }   
}
