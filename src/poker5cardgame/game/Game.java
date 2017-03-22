package poker5cardgame.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.ai.IntelligentClientAI;
import poker5cardgame.ai.IntelligentServerAI;
import poker5cardgame.ai.RandomClientAI;
import poker5cardgame.ai.RandomServerAI;
import poker5cardgame.io.Source;
import poker5cardgame.game.GameState.Action;

public class Game {
    
    private Source netSource;
    private GameData gameData;
    private GameState gameState;
    private ArtificialIntelligence sAI;
    private ArtificialIntelligence cAI;

    /**
     * Create a new Game Instance with the given source. Which is in charge of
     * providing the game with the means of communicating with the exterior
     * world in the language the game understands, Actions.
     *
     * @param source
     */
    public Game(Source source) {
        this.netSource = source;
        this.gameData = new GameData();
        this.gameState = new GameState();
        //this.setAI();
    }

    public GameState.State getState() {
        return gameState.state;
    }
    
    private void setAI()
    {
        switch(gameData.sInteractive) {
            case GameData.MODE_RANDOM_AI: this.sAI = new RandomServerAI(); break;
            case GameData.MODE_INTELLIGENT_AI: this.sAI = new IntelligentServerAI(); break;
        }
       
        switch (gameData.cInteractive) {
            case GameData.MODE_RANDOM_AI: this.cAI = new RandomClientAI(); break;
            case GameData.MODE_INTELLIGENT_AI: this.cAI = new IntelligentClientAI(); break;
        }
    }
    
    // TODO delete or sthg
    private void wait(int seconds)
    {
        try {
            Thread.sleep(seconds*000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Run the game with the next iteration of commands
     */
    public void update()
    {
        System.out.println("\n[DEBUG Game] " + gameState);
        this.wait(1);

        Move move = new Move();
        try {
            
            switch(getState())
            {
                case INIT:
                    move = this.updateClient();
                    break;
                    
                case START:                    
                move = this.updateServer();
                break;
                    
                case ACCEPT_ANTE:
                    move = this.updateClient();
                    break;
                    
                case PLAY:
                    move = this.updateServer();
                    break;
                    
                case BETTING:
                    if(gameState.isServerTurn())
                        move = this.updateServer();
                    else
                        move = this.updateClient();
                    gameState.setServerTurn(!gameState.isServerTurn());
                    break;
                    
                case BETTING_DEALER:
                    if(gameState.isServerTurn())
                        move = this.updateServer();
                    else
                        move = this.updateClient();
                    gameState.setServerTurn(!gameState.isServerTurn());
                    break;
                    
                case COUNTER:
                    if(gameState.isServerTurn())
                        move = this.updateServer();
                    else
                        move = this.updateClient();
                    gameState.setServerTurn(!gameState.isServerTurn());
                    break;
                    
                case DRAW:
                    move = this.updateClient();
                    break;
                    
                case DRAW_SERVER:
                    move = this.updateServer();
                    break;
                    
                case SHOWDOWN:
                    move = this.updateServer();                    
                    break;
            }            
            gameState.apply(move.action);

        } catch (Exception e) {
            this.sendErrorMsg(e.getMessage());
            if (getState().equals(GameState.State.QUIT)) {
                gameState.apply(Action.TERMINATE);
                this.sendErrorMsg("QUIT GAME due to ERROR: " + e.getMessage());
            }
        }
        System.out.println("[DEBUG Game] " + gameData +'\n');
        this.wait(1);
    }
    
    
    
    private Move updateClient() throws Exception {
        Move cMove;
        
        if(gameData.cInteractive == GameData.MODE_MANUAL)
            cMove = this.getClientValidMove();
        else
            cMove = cAI.getMoveForGame(gameData, gameState);
        
        // manage the move information
        switch(getState())
        {
            case BETTING:
                    if (cMove.action.equals(Action.BET)) {
                        this.manageBetClient(cMove);
                    }             
                break;

            case BETTING_DEALER:
                    if (cMove.action.equals(Action.BET)) {
                        this.manageBetClient(cMove);
                    }                
                break;

            case COUNTER:
                    if (cMove.action.equals(Action.RAISE)) {
                        this.manageRaiseClient(cMove);
                    }
                    if (cMove.action.equals(Action.CALL)) {
                        this.manageCallClient();
                    }
                    if (cMove.action.equals(Action.FOLD)) {
                        this.manageFoldClient();
                    }                
                break;
                
            case DRAW:
                gameData.cDrawn = cMove.cDrawn;     
                if (gameData.cDrawn > 0) {
                    gameData.cHand.discard(cMove.cards);
                }
                break;
                
        }        
        
        if(gameData.cInteractive != GameData.MODE_MANUAL)
            netSource.sendMove(cMove);
        return cMove;      
    }
    
    private Move updateServer() throws Exception {

        Move sMove = new Move();
        
        switch(getState())
        {
            case START: // default behaviour independent of the AI
                sMove.action = Action.ANTE_STAKES;                
                sMove.chips = gameData.minBet;
                sMove.cStakes = gameData.cChips;
                sMove.sStakes = gameData.sChips;
                break;
                
            case PLAY: // default behaviour independent of the AI
                // the game is accepted, so set the minimum bet as the bet of each player
                this.setMinBetServer();
                this.setMinBetClient();
                
                sMove.action = Action.DEALER_HAND;
                
                // choose the dealer randomly (0: server; 1: client)
                sMove.dealer = Math.random() > 0.5 ? 1 : 0;
                gameState.setServerTurn(sMove.dealer == 1); // the non dealer has the next turn
                           
                gameData.deck = new Deck();
                gameData.cHand.draw5FromDeck(gameData.deck);
                gameData.sHand.draw5FromDeck(gameData.deck);

                sMove.cards = new Card[Hand.SIZE];
                gameData.cHand.getCards().toArray(sMove.cards);
                
                break;
                
            case BETTING:
                sMove = sAI.getMoveForGame(gameData, gameState);
                if (sMove.action.equals(Action.BET)) {
                    this.manageBetServer(sMove);
                }
                break;

            case BETTING_DEALER:
                sMove = sAI.getMoveForGame(gameData, gameState);
                if (sMove.action.equals(Action.BET)) {
                    this.manageBetServer(sMove);
                }
                break;
                
            case COUNTER:
                sMove = sAI.getMoveForGame(gameData, gameState);
                if (sMove.action.equals(Action.RAISE)) {
                    this.manageRaiseServer(sMove);
                }
                if (sMove.action.equals(Action.CALL)) {
                    this.manageCallServer();
                }
                if (sMove.action.equals(Action.FOLD)) {
                    this.manageFoldServer();
                }
                break;
                
            case DRAW_SERVER:
                // get the move from the ai (who decides which cards the server discards)
                sMove = sAI.getMoveForGame(gameData, gameState);
                
                // complete the server hand with the missing cards
                gameData.sDrawn = sMove.sDrawn;
                gameData.sHand.putNCards(gameData.deck, gameData.sDrawn);
                
                // set the client drawn cards
                sMove.cDrawn = gameData.cDrawn;
                //sMove.cards = new Card[sMove.cDrawn];                
                sMove.cards = gameData.cHand.putNCards(gameData.deck, gameData.cDrawn);
               
                break;
                
            case SHOWDOWN: // Default behaviour independent of the AI
                if(!gameState.isFold())
                {
                    if(!gameState.isShowTime())
                        throw new Exception("Server can not show the cards now.");
                    sMove.action = Action.SHOW;
                    sMove.cards = new Card[Hand.SIZE];
                    gameData.sHand.getCards().toArray(sMove.cards);
                    netSource.sendMove(sMove);
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

                break;
        }
        netSource.sendMove(sMove);
        return sMove;

    }
    
    private void setMinBetServer() throws Exception
    {
        if (gameData.sChips >= gameData.minBet) {
            gameData.sBet += gameData.minBet;
            gameData.sChips -= gameData.minBet;
        } else {
            gameState.state = GameState.State.QUIT;
            throw new Exception("Logic Error. Not enough chips for the minimum bet.");
        }
    }
    
    private void setMinBetClient() throws Exception
    {
        if (gameData.cChips >= gameData.minBet) {
            gameData.cChips -= gameData.minBet;
            gameData.cBet += gameData.minBet;
        } else {
            gameState.state = GameState.State.QUIT;
            throw new Exception("Logic Error. Not enough chips for the minimum bet.");
        }
    }
    
    private void manageBetServer(Move move) throws Exception
    {
        if (move.chips >= gameData.minBet && gameData.sChips >= move.chips) {
            gameData.sChips -= move.chips;
            gameData.sBet += move.chips;
        } else {
            throw new Exception("Logic Error. Not valid chips value.");
        }
    }
    
    private void manageBetClient(Move move) throws Exception
    {
        if (move.chips >= gameData.minBet && gameData.cChips >= move.chips) {
            gameData.cChips -= move.chips;
            gameData.cBet += move.chips;
        } else {
            throw new Exception("Logic Error. Not valid chips value.");
        }
    }
    
    private void manageRaiseServer(Move move) throws Exception
    {
        this._manageCallServer(move.chips);
    }
    private void manageRaiseClient(Move move) throws Exception
    {
       this._manageCallClient(move.chips);
    }
    
    private void manageCallServer() throws Exception
    {
        this._manageCallServer(0);
    }
    
    private void _manageCallServer(int raise) throws Exception
    {
        int amountToBet = gameData.cBet - gameData.sBet + raise;
        if(gameData.sChips >= amountToBet)
        {
            gameData.sChips -= amountToBet;
            gameData.sBet += amountToBet;
        } else {
            throw new Exception("Logic Error. Not enough chips to call or raise.");
        }
    }
    
    private void manageCallClient() throws Exception
    {
        this._manageCallClient(0);
    }
    
    private void _manageCallClient(int raise) throws Exception
    {
        int amountToBet = gameData.sBet - gameData.cBet + raise;
        if(gameData.cChips >= amountToBet)
        {
            gameData.cChips -= amountToBet;
            gameData.cBet += amountToBet;
        } else {
            throw new Exception("Logic Error. Not enough chips to call or raise.");
        }
    }
    
    private void manageFoldServer()
    {
        gameState.setFold(true);
        this.allChipsToClient();
    }
    
    private void manageFoldClient()
    {
        gameState.setFold(true);
        this.allChipsToServer();
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
    
    private void sendErrorMsg(String msg)
    {
        Move errMove = new Move();
        errMove.action = Action.ERROR;
        errMove.error = msg;
        netSource.sendMove(errMove);
    }
    
    private Move getClientValidMove()
    {        
        // get the next clients move
        Move cMove = netSource.getNextMove();

        // wait until the clients move is valid
        while (!getState().transitions.containsKey(cMove.action) || cMove.action == Action.SHOW)
        {
            List<Action> validActions = new ArrayList();
            validActions.addAll(gameState.state.transitions.keySet());
            if (validActions.contains(Action.SHOW)) validActions.remove(Action.SHOW);
            this.sendErrorMsg("Protocol Error. Expecting for: " + validActions);
            cMove = netSource.getNextMove();
        }
        
        // return the clients valid move
        return cMove;
    }
}