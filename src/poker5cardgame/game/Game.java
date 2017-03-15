package poker5cardgame.game;

import java.util.Set;
import poker5cardgame.io.Source;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.GameState.State;

/**
 * Finite State Machine for the Game State
 */
public class Game {

    // TODO Quit should stop the game?
    // TODO manage when CALL i PASS sumen chips
    
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
        this.gameState = new GameState();
    }

    public GameState.State getState() {
        return gameState.state;
    }
    
    
    /**
     * Run the game with the next iteration of commands
     */
    public void update() {
        // TODO @sonia Acabar la logica de tots els estats i comprovar que seguim el protocol

        Move sMove, cMove;
        
        System.out.println("[DEBUG Game] " + gameState);

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
                this.setMinBetServer();
                this.setMinBetClient();
              
                
                sMove = new Move();
                sMove.action = Action.DEALER_HAND;
                
                // choose the dealer randomly (0: server; 1: client)
                sMove.dealer = Math.random() > 0.5 ? 1 : 0;
                gameState.setServerTurn(sMove.dealer == 1); // the non dealer has the next turn
                
                // generate the server and the client hands
                if(gameData.newPlay)
                {
                    gameData.deck.generate();
                    gameData.newPlay = false;
                }                
                 
                try {
                    gameData.cHand.generate(gameData.deck);
                    gameData.sHand.generate(gameData.deck);
                } catch (Exception ex) {
                    this.sendErrorMsg(ex.getMessage());
                }
                
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
                    // TODO @sonia RandomIA(= no IA) and IntelligentIA
                    // sMove = ia.getMoveForGame(Game g)
                    // NOW implemented with PASS example
                    
                    sMove = new Move();
                    sMove.action = Action.BET;
                    sMove.chips = 125;
                    this.manageBetServer(sMove);
                                    
                    source.sendMove(sMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(sMove.action);
                }
                // Turn: non dealer = CLIENT
                // Expected move: BET or PASS
                else 
                {
                    cMove = this.getClientValidMove(getState());
                    if(cMove.action.equals(Action.BET)) this.manageBetClient(cMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(cMove.action);
                }
                break;

            case BETTING_DEALER:
    
                // Turn: dealer = SERVER
                // Move to send: BET or PASS
                // Parameters: CHIPS (if bet) or none (if PASS)    
                if (gameState.isServerTurn()) 
                {
                    // TODO @sonia RandomIA(= no IA) and IntelligentIA
                    // sMove = ia.getMoveForGame(Game g)
                    // NOW implemented with BET example
                    
                    sMove = new Move();
                    sMove.action = Action.BET;
                    sMove.chips = 125;
                    this.manageBetServer(sMove);
                                    
                    source.sendMove(sMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(sMove.action);
                }
                // Turn: dealer = CLIENT
                // Expected move: BET or PASS
                else 
                {
                    cMove = this.getClientValidMove(gameState.state);
                    if(cMove.action.equals(Action.BET)) this.manageBetClient(cMove);
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
                    this.manageCallServer();
                    
                    source.sendMove(sMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(sMove.action);
                }
                else
                {
                    cMove = this.getClientValidMove(gameState.state);

                    if(cMove.action.equals(Action.RAISE)) this.manageRaiseClient(cMove);
                    if(cMove.action.equals(Action.CALL)) this.manageCallClient();
                    if(cMove.action.equals(Action.FOLD)) this.manageFoldClient(cMove);
                    gameState.setServerTurn(!gameState.isServerTurn());
                    gameState.apply(cMove.action);
                }    
              
                break;

            case DRAW:
                // Turn: CLIENT
                // Expected move: DRAW
                                
                // TODO send protocol error if the client drawn number does not match with the cards lenght

                cMove = this.getClientValidMove(gameState.state);              
                gameData.cDrawn = cMove.cDrawn;
                
                if (gameData.cDrawn > 0) {
                    try {
                        gameData.cHand.discard(cMove.cards);
                    } catch (Exception ex) {
                        this.sendErrorMsg(ex.getMessage());
                    }
                }

                gameState.apply(cMove.action);
                break;

            case DRAW_SERVER:
                // Turn: SERVER
                // Move to send: DRAW_SERVER
                
                sMove = new Move();
                sMove.action = Action.DRAW_SERVER;
                
                try {
                    // send the client cards to the client
                    sMove.cards = gameData.cHand.putNCards(gameData.deck, gameData.cDrawn);

                    // TODO ia
                    // Now change only the first card                
                    gameData.sHand.discard(gameData.sHand.getCards().get(0));
                    gameData.sHand.putNCards(gameData.deck, 1);
                } catch (Exception ex) {
                    this.sendErrorMsg(ex.getMessage());
                }

                gameData.sDrawn = 1;
                
                sMove.sDrawn = gameData.sDrawn;

                source.sendMove(sMove);
                gameState.apply(sMove.action);
                break;

            case SHOWDOWN:
                // Turn: SERVER                    
                
                if(!gameState.isFold())
                {
                    if(!gameState.isShowTime())
                        this.sendErrorMsg("Server can not show the cards now.");
                    sMove = new Move();
                    sMove.action = Action.SHOW;
                    sMove.cards = new Card[Hand.SIZE];
                    gameData.sHand.getCards().toArray(sMove.cards);
                    source.sendMove(sMove);
                }
                gameState.setFold(false);
                gameState.setShowTime(false);
                gameData.sDrawn = 0;
                gameData.cDrawn = 0;
                
                // manage Winner with handranker
                gameData.sHand.generateRankerInformation();
                gameData.cHand.generateRankerInformation();
                if(gameData.sHand.wins(gameData.cHand))            
                    this.allChipsToServer();
                else
                    this.allChipsToClient();
                
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
        System.out.println("[DEBUG Game] " + gameData);
    }

    private void manageBetAndRaise(Move move) {
        if (move.action.equals(Action.BET) || move.action.equals(Action.RAISE)) {
            if (gameState.isServerTurn()) {
                if (gameData.sChips >= move.chips && move.chips >= gameData.minBet) {
                    gameData.sChips -= move.chips;
                    gameData.sBet += move.chips;
                }
            } else if (gameData.cChips >= move.chips && move.chips >= gameData.minBet) {
                gameData.cChips -= move.chips;
                gameData.cBet += move.chips;
            } else {
                this.manageBetAndRaise(this.sendErrorMsg("Logic Error. Not valid chips value."));
            }
        }
    }
    
    private void setMinBetServer()
    {
        if (gameData.sChips >= gameData.minBet) {
            gameData.sBet += gameData.minBet;
            gameData.sChips -= gameData.minBet;
        } else {
            this.sendErrorMsg("Logic Error. Not enough chips for the minimum bet.");
        }
    }
    
    private void setMinBetClient()
    {
        if (gameData.cChips >= gameData.minBet) {
            gameData.cChips -= gameData.minBet;
            gameData.cBet += gameData.minBet;
        } else {
            this.sendErrorMsg("Logic Error. Not enough chips for the minimum bet.");
        }
    }
    
    private void manageBetServer(Move move)
    {
        if (move.chips >= gameData.minBet && gameData.sChips >= move.chips) {
            gameData.sChips -= move.chips;
            gameData.sBet += move.chips;
        } else {
            this.sendErrorMsg("Logic Error. Not valid chips value.");
        }
    }
    
    private void manageBetClient(Move move)
    {
        if (move.chips >= gameData.minBet && gameData.cChips >= move.chips) {
            gameData.cChips -= move.chips;
            gameData.cBet += move.chips;
        } else {
            this.sendErrorMsg("Logic Error. Not valid chips value.");
        }
    }
    
    private void manageRaiseServer(Move move)
    {
        this._manageCallServer(move.chips);
    }
    private void manageRaiseClient(Move move)
    {
       this._manageCallClient(move.chips);
    }
    
    private void manageCallServer()
    {
        this._manageCallServer(0);
    }
    
    private void _manageCallServer(int raise)
    {
        int amountToBet = gameData.cBet - gameData.sBet + raise;
        if(gameData.sChips >= amountToBet)
        {
            gameData.sChips -= amountToBet;
            gameData.sBet += amountToBet;
        } else {
            this.sendErrorMsg("Logic Error. Not enough chips to call.");
        }
    }
    
    private void manageCallClient()
    {
        this._manageCallClient(0);
    }
    
    private void _manageCallClient(int raise)
    {
        int amountToBet = gameData.sBet - gameData.cBet + raise;
        if(gameData.cChips >= amountToBet)
        {
            gameData.cChips -= amountToBet;
            gameData.cBet += amountToBet;
        } else {
            this.sendErrorMsg("Logic Error. Not enough chips to call.");
        }
    }
    
    private void manageFoldServer(Move move)
    {
        gameState.setFold(true);
        this.allChipsToClient();
    }
    
    private void manageFoldClient(Move move)
    {
        gameState.setFold(true);
        this.allChipsToServer();
    }
    
    private void manageFold(Move move)
    {
     if (move.action.equals(Action.FOLD)) {
            gameState.setFold(true);
            if(gameState.isServerTurn())
                this.allChipsToClient();
            else
                this.allChipsToServer();
        }
    }
    
    private void allChipsToServer() {
        gameData.sChips += gameData.sBet + gameData.cBet;
        this.resetBets();
    }

    private void allChipsToClient() {
        gameData.cChips += gameData.sBet + gameData.cBet;
        this.resetBets();
    }

    private void resetBets() {
        gameData.cBet = 0;
        gameData.sBet = 0;
    }
    
    private Move sendErrorMsg(String msg)
    {
        Move errMove = new Move();
        errMove.action = Action.ERROR;
        errMove.error = msg;
        source.sendMove(errMove);
        return source.getNextMove();
    }
    
    private Move getClientValidMove(State actualState)
    {        
        // get the next clients move
        Move cMove = source.getNextMove();

        // wait until the clients move is valid
        while (!gameState.state.transitions.containsKey(cMove.action) || cMove.action == Action.SHOW)
        {
            Set validActions = getState().transitions.keySet();
            if (validActions.contains(Action.SHOW)) validActions.remove(Action.SHOW);
            cMove = this.sendErrorMsg("Protocol Error. Expecting for: " + validActions);
        }
        
        // return the clients valid move
        return cMove;
    }
}
