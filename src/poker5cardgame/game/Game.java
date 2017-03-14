package poker5cardgame.game;

import java.util.Arrays;
import poker5cardgame.io.Source;
import java.util.HashMap;
import java.util.Map;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.GameState.State;

/**
 * Finite State Machine for the Game State
 */
public class Game {

    private Source source;
    
    private GameData gameData;
    private GameState gameState;

    /**
     * Create a new Game Instance with the given source. Which is in charge of
     * providing the game with the means of communicating with the exterior
     * world in the language the game understands, Actions.
     *
     * @param source
     */
    public Game(Source source) {
        this.source = source;
        this.gameData = new GameData();
    }

   

    /**
     * Run the game with the next iteration of commands
     */
    public void update() {
        // TODO @sonia Acabar la logica de tots els estats i comprovar que seguim el protocol
        // TODO @sonia metode pq cmove sigui valid 

        Move sMove, cMove;
        System.out.println("[DEBUG Game] " + gameData);

        switch (getState()) {

            case INIT:
                // Turn: CLIENT
                // Expected move: START
                
                cMove = this.getClientValidMove(gameState.state);
                gameState.apply(cMove.action);
                break;

            case START:
                // Turn: SERVER
                // Move to send: ANTE_STAKES
                // Parameters: CHIPS (minimum bet), CHIPS (client chips), CHIPS (server chips)
                
                sMove = new Move();
                sMove.action = Action.ANTE_STAKES;
                
                sMove.chips = gameData.minBet;     // ANTE parameter
                sMove.cStakes = gameData.cChips;   // STAKES parameter
                sMove.sStakes = gameData.sChips;   // STAKES parameter
                
                source.sendMove(sMove);
                gameState.apply(sMove.action);
                break;

            case ACCEPT_ANTE:
                // Turn: CLIENT
                // Expected move: ANTE_OK or QUIT
                
                cMove = this.getClientValidMove(gameState.state);
                gameState.apply(cMove.action);
                break;

            case PLAY:
                // Turn: SERVER
                // Move to send: DEALER_HAND
                // Parameters: '0' (dealer = server) or '1' (dealer = client), client hand
               
                // the game is accepted, so set the minimum bet as the bet of each player
                gameData.cBet = gameData.minBet;
                gameData.sBet = gameData.minBet;
                
                sMove = new Move();
                sMove.action = Action.DEALER_HAND;
                
                // choose the dealer randomly (0: server; 1: client)
                sMove.dealer = Math.random() > 0.5 ? 1 : 0;
                gameState.setServerTurn(sMove.dealer == 1); // the non dealer has the next turn
                
                // generate the server and the client hands
                gameData.deck.generate();                
                gameData.cHand.generate(gameData.deck);
                gameData.sHand.generate(gameData.deck);
                sMove.cards = new Card[Hand.SIZE];
                gameData.cHand.getCards().toArray(sMove.cards);

                source.sendMove(sMove);
                gameState.apply(sMove.action);                
                break;

            case BETTING:                
                // Turn: non dealer = SERVER
                // Move to send: BET or PASS
                // Parameters: CHIPS (if bet) or none (if PASS)     
                if (gameState.isServerTurn()) 
                {
                    System.out.println("[DEBUG GAME] Entra a BETTING server");
                    // TODO @sonia RandomIA(= no IA) and IntelligentIA
                    // sMove = ia.getMoveForGame(Game g)
                    // NOW implemented with PASS example
                    
                    sMove = new Move();
                    sMove.action = Action.PASS;
                                    
                    source.sendMove(sMove);                                        
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(sMove.action);
                }
                // Turn: non dealer = CLIENT
                // Expected move: BET or PASS
                else 
                {
                    System.out.println("[DEBUG GAME] Entra a BETTING client");

                    cMove = this.getClientValidMove(getState());
                    System.out.println("[DEBUG GAME] despres de getvalid move a betting");

                    this.manageBetAndRaise(cMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(cMove.action);

                }
                break;

            case BETTING_DEALER:
                // TODO same as BETTING? considero que ara mateix si pq no hi ha IA           
    
                // Turn: dealer = SERVER
                // Move to send: BET or PASS
                // Parameters: CHIPS (if bet) or none (if PASS)    
                if (gameState.isServerTurn()) 
                {
                    // TODO @sonia RandomIA(= no IA) and IntelligentIA
                    // sMove = ia.getMoveForGame(Game g)
                    // NOW implemented with PASS example
                    
                    sMove = new Move();
                    sMove.action = Action.PASS;
                                    
                    source.sendMove(sMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(sMove.action);
                }
                // Turn: dealer = CLIENT
                // Expected move: BET or PASS
                else 
                {
                    cMove = this.getClientValidMove(gameState.state);
                    this.manageBetAndRaise(cMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(cMove.action);
                }
                break;

            case COUNTER:
                if (gameState.isServerTurn())
                {
                    // TODO ia
                    
                    sMove = new Move();
                    sMove.action = Action.CALL;
                    
                    source.sendMove(sMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(sMove.action);
                }
                else
                {
                    cMove = this.getClientValidMove(gameState.state);
                    this.manageBetAndRaise(cMove);
                    this.manageFold(cMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(cMove.action);
                }                
                break;

            case DRAW:
                // Turn: CLIENT
                // Expected move: DRAW
                
                cMove = this.getClientValidMove(gameState.state);
                
                // TODO manage if the cards are correct (of the hand) and send an error message if not
                if(cMove.cards.length != 0)
                {
                    gameData.cHand.discard(cMove.cards);
                    gameData.cHand.putNCards(gameData.deck, cMove.cards.length);
                }
                
                gameState.apply(cMove.action);
                break;

            case DRAW_SERVER:
                // Turn: SERVER
                // Move to send: DRAW_SERVER
                
                sMove = new Move();
                sMove.action = Action.DRAW_SERVER;
                
                // TODO ia
                // Now change only the first card
                sMove.cards = new Card[]{gameData.sHand.getCards().get(0), gameData.sHand.getCards().get(1)}; 
                gameData.sHand.discard(sMove.cards);
                gameData.sHand.putNCards(gameData.deck, 2);

                source.sendMove(sMove);
                gameState.apply(sMove.action);
                break;

            case SHOWDOWN:
                // Turn: SERVER

                if (!gameState.isFold()) {
                    sMove = new Move();
                    sMove.action = Action.SHOW;
                    sMove.cards = new Card[Hand.SIZE];
                    gameData.sHand.getCards().toArray(sMove.cards);
                    source.sendMove(sMove);
                }

                gameState.setFold(false);
                sMove = new Move();
                sMove.action = Action.STAKES;
                sMove.cStakes = gameData.cChips;   // STAKES parameter
                sMove.sStakes = gameData.sChips;   // STAKES parameter

                source.sendMove(sMove);
                gameState.apply(sMove.action);
                break;

            case QUIT:
                gameState.apply(Action.QUIT);
                break;
        }
    }

    private void manageBetAndRaise(Move move) {
        if (move.action.equals(Action.BET) || move.action.equals(Action.RAISE)) {
            if (gameState.isServerTurn()) {
                if (gameData.sChips >= move.chips && move.chips > gameData.minBet) {
                    gameData.sChips -= move.chips;
                    gameData.sBet += move.chips;
                }
            } else if (gameData.cChips >= move.chips && move.chips > gameData.minBet) {
                gameData.cChips -= move.chips;
                gameData.cBet += move.chips;
            } else {
                this.manageBetAndRaise(this.sentNotValidChipsValue());
            }
        }
    }
    
    private void manageFold(Move move)
    {
     if (move.action.equals(Action.FOLD)) {
            gameState.setFold(true);
            if(gameState.isServerTurn())
                gameData.cChips += gameData.sBet + gameData.cBet;
            else
                gameData.sChips += gameData.sBet + gameData.cBet;
            gameData.cBet = 0;
            gameData.sBet = 0;
        }
    }
    
    private Move sentNotValidChipsValue()
    {
        Move errMove = new Move();
        errMove.action = Action.ERROR;
        errMove.error = "Logic Error. Not valid chips value.";
        source.sendMove(errMove);
        return source.getNextMove();
    }
    
    private Move sendProtocolErrorMsg(State actualState)
    {
        Move errMove = new Move();
        errMove.action = Action.ERROR;
        errMove.error = "Protocol Error. Expecting for: " + actualState.transitions.keySet();
        source.sendMove(errMove);
        return source.getNextMove();

    }
    
    private Move getClientValidMove(State actualState)
    {        
        // get the next clients move
        Move cMove = source.getNextMove();

        // wait until the clients move is valid
        while (!gameState.state.transitions.containsKey(cMove.action))
            cMove = this.sendProtocolErrorMsg(actualState);
        
        // return the clients valid move
        return cMove;
    }

    public GameState.State getState() {
        return gameState.state;
    }
    
    
}
