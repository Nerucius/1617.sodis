package poker5cardgame.game;

import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.ai.IntelligentServerAI;
import poker5cardgame.ai.RandomServerAI;
import poker5cardgame.io.Source;
import poker5cardgame.game.GameState.Action;
import static poker5cardgame.Log.*;
import poker5cardgame.io.NetworkSource;

public class ServerGame {

    private static final int INITIAL_BET = 100;
    private static final int INITIAL_SERVER_CHIPS = 10000;
    private static final int INITIAL_CLIENT_CHIPS = 1000;
    
    /**
     * Source to be used for receiving and sending data to another player.
     */
    private Source IOSource;
    /**
     * Source to be used to get the next move on the local game.
     */
    private Source playerSource;
    private GameData serverGameData;
    private GameState serverGameState;
    
    // Selector Variables
    public boolean nextMoveReady = false;
    public boolean isSelector = false;

    /**
     * Create a new Game Instance with the given source. Which is in charge of
     * providing the game with the means of communicating with the exterior
     * world in the language the game understands, Actions.
     *
     * @param IOSource input/output source
     * @param aiType Type of AI (Random / Advanced)
     */
    public ServerGame(Source IOSource, ArtificialIntelligence.Type aiType) {
        this.IOSource = IOSource;

        this.serverGameData = new GameData();
        this.serverGameState = new GameState();

        switch (aiType) {
            case AI_RANDOM:
                playerSource = new RandomServerAI(serverGameData, serverGameState);
                break;
            case AI_INTELLIGENT:
                playerSource = new IntelligentServerAI(serverGameData, serverGameState);
                break;
            default:
                playerSource = new RandomServerAI(serverGameData, serverGameState);
        }

        GAME_DEBUG(serverGameData.cId, "Game: Creating new with AI " + aiType);
    }

    /**
     * Create a new Game. By default this creates a Random AI Game
     *
     * @param IOSource Source to be used for Sending/Receiving from the other
     * player
     */
    public ServerGame(Source IOSource) {
        this(IOSource, ArtificialIntelligence.Type.AI_RANDOM);
    }

    /**
     * Create a new Game, using the given sources to Get and Send Moves
     *
     * @param IOSource Source to be used for Sending/Receiving from the other
     * player
     * @param playerSource Source to be used locally to get Moves
     */
    public ServerGame(Source IOSource, Source playerSource) {
        this.IOSource = IOSource;
        this.playerSource = playerSource;
        this.serverGameData = new GameData();
        this.serverGameState = new GameState();
    }

    public GameState.State getState() {
        return serverGameState.state;
    }

    /**
     * Run the game with the next iteration of Moves.
     * 
     * @return true if the update cycle is done.
     */
    public boolean update() {
        boolean done = false;

        Move move = new Move();
        move.action = Action.NOOP;

        try {
            switch (getState()) {
                
                case INIT:
                    move = getValidMove(IOSource);
                    serverGameData.save(move, false);
                    break;

                case START:
                    move = new Move();
                    move.action = Action.ANTE_STAKES;
                    move.chips = INITIAL_BET;
                    move.cStakes = INITIAL_CLIENT_CHIPS;
                    move.sStakes = INITIAL_SERVER_CHIPS;                    
                    IOSource.sendMove(move);
                    serverGameData.save(move, true);
                    break;
                    
                case ACCEPT_ANTE:                    
                    move = getValidMove(IOSource);
                    serverGameData.save(move,false);
                    break;
                
                case QUIT:                  
                    move = new Move();
                    move.action = Action.QUIT;
                    IOSource.sendMove(move);
                    break;

                case PLAY:
                    move = new Move();
                    
                    // Generate the server and client hands
                    serverGameData.deck = new Deck();
                    serverGameData.cHand.draw5FromDeck(serverGameData.deck);
                    serverGameData.sHand.draw5FromDeck(serverGameData.deck);

                    move.action = Action.DEALER_HAND;
                    if (serverGameData.dealer == -1) // We want a random dealer only for the first game round
                        move.dealer = Math.random() > 0.5 ? 1 : 0;
                    else // Then, the dealer alternates in every game round
                        move.dealer = 1 - serverGameData.dealer;
                    move.cards = new Card[Hand.SIZE];
                    serverGameData.cHand.dumpArray(move.cards);
                    
                    serverGameState.setServerTurn(move.dealer == 1);
                    
                    IOSource.sendMove(move);
                    serverGameData.save(move, true);
                    break;
                
                case BETTING:
                    if(serverGameState.isServerTurn())
                    {
                        move = getValidMove(playerSource);
                        IOSource.sendMove(move);
                    }
                    else
                    {
                        move = getValidMove(IOSource);
                        if(move.action == Action.BET && !ArtificialIntelligence.possibleBet(serverGameData, move.chips, false))
                            throw new Exception("Logic Error. Not valid chips value."); 
                    }
                    serverGameData.save(move, serverGameState.isServerTurn());
                    serverGameState.setServerTurn(!serverGameState.isServerTurn());
                    break;

                case BETTING_DEALER:
                    if(serverGameState.isServerTurn())
                    {
                        move = getValidMove(playerSource);
                        IOSource.sendMove(move);
                    }
                    else
                    {
                        move = getValidMove(IOSource);
                        if(move.action == Action.BET && !ArtificialIntelligence.possibleBet(serverGameData, move.chips, false))
                            throw new Exception("Logic Error. Not valid chips value."); 
                    }
                    serverGameData.save(move, serverGameState.isServerTurn());
                    serverGameState.setServerTurn(!serverGameState.isServerTurn());
                    break;

                case COUNTER:
                    if(serverGameState.isServerTurn())
                    {
                        move = getValidMove(playerSource);
                        IOSource.sendMove(move);
                    }
                    else
                    {
                        move = getValidMove(IOSource);     
                        if(move.action == Action.RAISE && !ArtificialIntelligence.possibleRaise(serverGameData, move.chips, false))
                            throw new Exception("Logic Error. Not valid chips value."); 
                        if(move.action == Action.CALL && !ArtificialIntelligence.possibleCall(serverGameData, false))
                            throw new Exception("Logic Error. Not valid chips value.");                     
                    }
                    if(move.action == Action.FOLD)   
                        serverGameState.setFold(true);
                    serverGameData.save(move, serverGameState.isServerTurn());
                    serverGameState.setServerTurn(!serverGameState.isServerTurn());
                    break;

                case DRAW:
                    move = getValidMove(IOSource);                                 
                    // If the cards are not matching the hand cards, the discard method throws an exception.
                    if(move.cDrawn > 0)
                        serverGameData.cHand.discard(move.cards);
                    
                    serverGameData.save(move, false);
                    break;

                case DRAW_SERVER:;
                    move = getValidMove(playerSource);

                    // Complete the server hand with the missing cards
                    if(move.sDrawn > 0)
                        serverGameData.sHand.putNCards(serverGameData.deck, move.sDrawn);

                    // Complete the client hand with the missing cards
                    move.cDrawn = serverGameData.cDrawn;
                    if(move.cDrawn > 0)
                        move.cards = serverGameData.cHand.putNCards(serverGameData.deck, serverGameData.cDrawn);
                    
                    serverGameState.setServerTurn(serverGameData.dealer == 1);
                    
                    serverGameData.save(move, true);
                    IOSource.sendMove(move);
                    break;

                case SHOWDOWN:
                    // Show the cards only if show time (i.e. we are not here because of a fold case)
                    if (!serverGameState.isFold()) {
                        if (!serverGameState.isShowTime()) {
                            throw new Exception("Server can not show the cards now.");
                        }
                        
                        move = new Move();
                        move.action = Action.SHOW;
                        move.cards = new Card[Hand.SIZE];
                        serverGameData.sHand.dumpArray(move.cards); 
                        
                        // Manage winner with handranker
                       move.winner = ArtificialIntelligence.manageWinner(serverGameData);

                        IOSource.sendMove(move);
                        serverGameData.save(move, true);
                    }

                    // Ended the game round, so prepare the next one
                    serverGameState.setFold(false);
                    serverGameState.setShowTime(false);
                    
                    move = new Move();
                    if(!ArtificialIntelligence.possibleNewRound(serverGameData))
                    {
                        move.action = Action.TERMINATE;
                        throw new Exception("Logic Error. Not enough chips for the initial bet. Terminates the communication.");
                    }
                    
                    move.action = Action.STAKES;
                    move.cStakes = serverGameData.cChips;
                    move.sStakes = serverGameData.sChips;   
                    move.winner = serverGameData.winner;
                    serverGameData.save(move, true);
                    IOSource.sendMove(move);
                    break;
            }

            serverGameState.apply(move.action);
            
            GAME_DEBUG(serverGameData.cId, "Processed move " + move);

        } catch (MoveNotReadyException e){
            GAME_DEBUG("Finished Server turn for the Selector Server");
            done = true;
            
        }catch (Exception ex) {
            this.sendErrorMsg(ex.getMessage());
            if (move.action == Action.TERMINATE)
                serverGameState.apply(move.action);
                return true;
        }

        GAME_DEBUG(serverGameData.cId, "Updated State: " + serverGameState + " | Data: " + serverGameData + '\n');
        return done;
    }

    private void sendErrorMsg(String msg) {
        try {
            Move errMove = new Move();
            errMove.action = Action.ERROR;
            errMove.error = msg;
            IOSource.sendMove(errMove);
        } catch (Exception ex) { /* Ignored */ }
    }

    private Move getValidMove(Source src) throws MoveNotReadyException {
        // In the case of the selector server, we can't run the update method 
        // in a while(true) loop, so when we request the next move after the first
        // has been processed, we have to tell the server that we can't update
        // further without new data from the client
        if(isSelector && !nextMoveReady && src instanceof NetworkSource){
            throw new MoveNotReadyException();
        }
        nextMoveReady = false;        
        
        // get the next  move
        Move move = src.getNextMove();

        while (!serverGameState.getValidActions().contains(move.action)) {
            this.sendErrorMsg("Protocol Error. Received move: " + move.action
                    + ". Expecting moves: " + serverGameState.getValidActions());
            move = src.getNextMove();
        }
        // return the valid move
        return move;
    }
    
    private class MoveNotReadyException extends Exception{
        
    }
}
