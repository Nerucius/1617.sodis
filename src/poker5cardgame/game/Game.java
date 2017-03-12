package poker5cardgame.game;

import java.util.Arrays;
import poker5cardgame.io.Source;
import java.util.HashMap;
import java.util.Map;

/**
 * Finite State Machine for the Game State
 */
public class Game {

    // Define all the game actions
    public enum Action {
        START,
        ANTE_STAKES,
        STAKES,
        QUIT,
        ANTE_OK,
        DEALER_HAND,
        PASS,
        BET,
        RAISE,
        FOLD,
        CALL,
        DRAW,
        DRAW_SERVER,
        SHOW,
        // Special no-operation command to do nothing
        NOOP,
        // Special command to send an error message to the client
        ERROR,
        // Special command to terminate the game anytime
        TERMINATE;
    }

    // Define all the game states
    public enum State {
        INIT,
        START,
        ACCEPT_ANTE,
        PLAY,
        BETTING,
        BETTING_DEALER,
        COUNTER,
        DRAW,
        DRAW_SERVER,
        SHOWDOWN,
        QUIT;

        // Define a map with the valid state -> action -> state combinations
        // This map contains a Game.State as value (the next state after applying 
        // a specific action) and a Game.Action as key (the specific applied action)
        private Map<Game.Action, Game.State> transitions;

        private State() {
            transitions = new HashMap<>();
        }
    }

    // Fill the transitions map
    static {
        for (State state : State.values())
            switch (state) {

                case INIT:
                    state.transitions.put(Action.START, State.START);
                    break;

                case START:
                    state.transitions.put(Action.ANTE_STAKES, State.ACCEPT_ANTE);
                    break;
                    
                case ACCEPT_ANTE:
                    state.transitions.put(Action.ANTE_OK, State.PLAY);
                    state.transitions.put(Action.QUIT, State.QUIT);
                    break;
                    
                case PLAY:
                    state.transitions.put(Action.DEALER_HAND, State.BETTING);
                    break;
                    
                case BETTING:
                    state.transitions.put(Action.PASS, State.BETTING_DEALER);
                    state.transitions.put(Action.BET, State.COUNTER);
                    break;
                    
                case BETTING_DEALER:
                    state.transitions.put(Action.PASS, State.DRAW);
                    state.transitions.put(Action.BET, State.COUNTER);
                    // 2nd time betting, exits to showdown
                    state.transitions.put(Action.SHOW, State.SHOWDOWN);
                    break;
                    
                case COUNTER:
                    state.transitions.put(Action.CALL, State.DRAW);
                    state.transitions.put(Action.RAISE, State.COUNTER);
                    state.transitions.put(Action.FOLD, State.QUIT);
                    // 2nd time betting, exits to showdown
                    state.transitions.put(Action.SHOW, State.SHOWDOWN);
                    break;
                    
                case DRAW:
                    state.transitions.put(Action.DRAW, State.DRAW_SERVER);
                    break;
                    
                case DRAW_SERVER:
                    state.transitions.put(Action.DRAW_SERVER, State.BETTING);
                    break;
                    
                case SHOWDOWN:
                    state.transitions.put(Action.STAKES, State.ACCEPT_ANTE);
                    break;
            }
    }

    private Source source;
    private GameData data;

    /**
     * Create a new Game Instance with the given source. Which is in charge of
     * providing the game with the means of communicating with the exterior
     * world in the language the game understands, Actions.
     *
     * @param source
     */
    public Game(Source source) {
        this.source = source;
        this.data = new GameData();
    }

    public Game.State getState()
    {
        return this.data.state;
    }
    
    /**
     * Set to the game state the next game state after applying a specific
     * action to the actual game state.
     *
     * @param action Action that is applied to the actual game state
     */
    public void apply(Action action) {
        // If the action is noop, do nothing
        if (action == Action.NOOP)
            return;

        // If the action is terminate, finish the game
        if (action == Action.TERMINATE) {
            System.err.println("Game: Terminating Game now due to Error");
            data.state = State.QUIT;
            return;
        }

        // If the action we want to apply to the actual state is valid, set the next state as actual
        if (data.state.transitions.containsKey(action)) {

            if (action == Action.STAKES)
                data.setSecondRound(false); // set again to false after the second round 
            
            // We finish a round in the next cases: 
            // 1) applying a PASS action being in a BETTING_DEALER state (finish 1st round, go to 2nd)
            // 2) applying a CALL action being in a COUNTER state (finish 2nd round)
            if ((data.state == State.BETTING_DEALER && action == Action.PASS) ||
                (data.state == State.COUNTER && action == Action.CALL)) 
            
                if (!data.isSecondRound())
                    data.setSecondRound(true);
                else 
                {
                    // Apply the action show after finishing the 2nd round
                    this.apply(Action.SHOW);
                    return;
                }

            // Set the next state as actual
            State nextState = data.state.transitions.get(action);
            System.out.println(data.state + " -> " + action + " -> " + nextState + " R2:" + data.isSecondRound()); // TODO delete print
            data.state = nextState;
        } 
        
        // If the action we want to apply to the actual state is invalid, send an ERROR move
        else {
            System.err.println("--- ILLEGAL ACTION");
            System.err.println("--- " + data.state + " DOES NOT ACCEPT " + action);

            // Send special Error Move -> ERRO Packet
            Move errMove = new Move();
            errMove.action = Action.ERROR;
            errMove.error = "Protocol Error";
            source.sendMove(errMove);
        }
    }

    /**
     * Run the game with the next iteration of commands
     */
    public void update() {
        // TODO @sonia Acabar la logica de tots els estats i comprovar que seguim el protocol
        // TODO @sonia metode pq cmove sigui valid 

        Move sMove, cMove;

        switch (data.state) {

            case INIT:
                // Turn: CLIENT
                // Expected move: START
                
                // get the move from the client and apply it to the game
                cMove = this.getClientValidMove(data.state);
                apply(cMove.action);
                break;

            case START:
                // Turn: SERVER (Client last move: START)
                // Expected first move: ANTE
                // Parameters: CHIPS (minimum bet)
                // Expected second move: STAKES
                // Parameters: CHIPS (client chips), CHIPS (server chips)
                
                // set the server next move to ANTE_STAKES
                sMove = new Move();
                sMove.action = Action.ANTE_STAKES;
                
                // set the required parameters
                sMove.chips = data.getMinBet();     // ANTE parameter
                sMove.cStakes = data.getcChips();   // STAKES parameter
                sMove.sStakes = data.getsChips();   // STAKES parameter
                
                // send the move to the client and apply it to the game
                source.sendMove(sMove);
                apply(sMove.action);
                break;

            case ACCEPT_ANTE:
                // Turn: CLIENT (SERVER last move: ANTE_STAKES)
                // Expected move: ANTE_OK or QUIT
                
                // get the move from the client and apply it to the game
                cMove = this.getClientValidMove(data.state);
                apply(cMove.action);
                break;

            case PLAY:
                // Turn: SERVER (Client last move: ANTE_OK)
                // Expected first move: DEALER
                // Parameters: '0' (dealer = server) or '1' (dealer = client)
                // Expected second move: HAND
                // Parameters: client hand
               
                // the game is accepted, so set the minimum bet as the bet of each player
                data.setcBet(data.getMinBet());
                data.setsBet(data.getMinBet());
                
                // set the server next move to DEALER_HAND
                sMove = new Move();
                sMove.action = Action.DEALER_HAND;
                
                // set the required parameters
                // choose the dealer randomly (0: server; 1: client)
                sMove.dealer = Math.random() > 0.5 ? 1 : 0;
                data.setServerTurn(sMove.dealer == 1); // the non dealer has the next turn
                
                // generate the server and the client hands
                data.deck.generate();                
                data.cHand.generate(data.deck);
                data.sHand.generate(data.deck);
                sMove.cards = new Card[Hand.SIZE];
                data.cHand.getCards().toArray(sMove.cards);

                // send the move to the client and apply it to the game
                source.sendMove(sMove);
                apply(sMove.action);                
                break;

            case BETTING:
                // Turn: non dealer
                
                // Case: non dealer = SERVER
                // Expected move: BET or PASS
                // Parameters: CHIPS (if bet) or none (if PASS)     
                if (data.isServerTurn()) 
                {
                    // TODO @sonia RandomIA(= no IA) and IntelligentIA
                    // sMove = ia.getMoveForGame(Game g)
                    // NOW implemented with PASS example
                    
                    // set the server next move to PASS
                    sMove = new Move();
                    sMove.action = Action.PASS;
                                    
                    // send the move to the client and apply it to the game
                    source.sendMove(sMove);
                    apply(sMove.action);
                }
                // Case: non dealer = CLIENT
                // Expected move: BET or PASS
                else 
                {
                    // get the move from the client
                    cMove = this.getClientValidMove(data.state);

                    if(cMove.action.equals(Action.BET))
                        data.setcBet(++cMove.chips);
                    
                    // apply the move to the game
                    apply(cMove.action);

                }
                data.setServerTurn(!data.isServerTurn());
                break;

            case BETTING_DEALER:
                // TODO same as BETTING? considero que ara mateix si pq no hi ha IA           
    
                // Turn: dealer
                
                // Case: dealer = SERVER
                // Expected move: BET or PASS
                // Parameters: CHIPS (if bet) or none (if PASS)     
                if (data.isServerTurn()) 
                {
                    // TODO @sonia RandomIA(= no IA) and IntelligentIA
                    // sMove = ia.getMoveForGame(Game g)
                    // NOW implemented with PASS example
                    
                    // set the server next move to PASS
                    sMove = new Move();
                    sMove.action = Action.PASS;
                                    
                    // send the move to the client and apply it to the game
                    source.sendMove(sMove);
                    apply(sMove.action);
                }
                // Case: dealer = CLIENT
                // Expected move: BET or PASS
                else 
                {
                    // get the move from the client
                    cMove = this.getClientValidMove(data.state);

                    if(cMove.action.equals(Action.BET))
                    {
                        data.setcBet(++cMove.chips); // TODO disminuir els cChips i comprovar que pot apostar tant
                    }
                    
                    // apply the move to the game
                    apply(cMove.action);

                }
                data.setServerTurn(!data.isServerTurn());
                break;

            case COUNTER:
                if (data.isServerTurn())
                {
                    // TODO ia
                    sMove = new Move();
                    sMove.action = Action.CALL;
                    source.sendMove(sMove);
                    apply(sMove.action);
                }
                else
                {
                    cMove = this.getClientValidMove(data.state);
                    
                    if(cMove.action.equals(Action.RAISE)){
                        data.setcBet(++cMove.chips); // TODO disminuir els cChips i comprovar que pot apostar tant
                    }
                    apply(cMove.action);
                }
                
                break;

            case DRAW:
                // Turn: CLIENT
                // Expected move: DRAW
                
                // get the move from the client
                cMove = this.getClientValidMove(data.state);
                
                if(cMove.cards.length != 0)
                {
                    data.cHand.discard(cMove.cards);
                    data.cHand.putNCards(data.deck, cMove.cards.length);
                }
                
                apply(cMove.action);
                break;

            case DRAW_SERVER:
                // Turn: SERVER
                // Expected move: DRAW_SERVER
                
                sMove = new Move();
                sMove.action = Action.DRAW_SERVER;
                
                // TODO ia
                // Now change only the first card
                sMove.cards = new Card[]{data.sHand.getCards().get(0)}; 
                data.sHand.discard(data.sHand.getCards().get(0));
                data.sHand.putNCards(data.deck, 1);
                
                source.sendMove(sMove);
                apply(sMove.action);                
                break;

            case SHOWDOWN:
                // Turn: SERVER

                sMove = new Move();
                sMove.action = Action.SHOW;
                sMove.cards = new Card[Hand.SIZE];
                data.sHand.getCards().toArray(sMove.cards);
                source.sendMove(sMove);
                
                sMove = new Move();
                sMove.action = Action.STAKES;
                sMove.cStakes = data.getcChips();   // STAKES parameter
                sMove.sStakes = data.getsChips();   // STAKES parameter
                
                // send the move to the client and apply it to the game
                source.sendMove(sMove);
                apply(sMove.action);
                break;
                
            case QUIT:
                apply(Action.QUIT);
                break;
        }
    }


    private Move getClientValidMove(State actualState)
    {        
        System.out.println("DEBUG actualState: " +actualState);
        // get the next clients move
        Move cMove = source.getNextMove();
        // wait until the clients move is valid
        while (!data.state.transitions.containsKey(cMove.action))
        {
            // to debug: print an error message for the server -> TODO send a ERR message to the client
            System.out.println("Game: ERRO Invalid move. Valid moves are now: " + actualState.transitions.values());
            cMove = source.getNextMove();
        }
        // return the clients valid move
        return cMove;
    }
    
    /**
     * Class defining a move made by either player, affecting the game.
     */
    public static class Move {

        public Action action;
        // Client Game ID
        public int id = -1;
        // Generic chips param for ANTE, BET or RAISE
        public int chips = -1;
        // Client and server Stakes
        public int cStakes = -1, sStakes = -1;
        // Dealer Flag, 1 for Client dealer, 0 for server dealer
        public int dealer = -1;
        // Array of cards to deal or discard
        public Card[] cards = null;
        // Error Message
        public String error = null;

        public Move() {
            action = Action.NOOP;
        }
        
        @Override
        public String toString() {
            String str = action.toString()
                    + (id == -1 ? "" : " " + id)
                    + (chips == -1 ? "" : " " + chips)
                    + (cStakes == -1 ? "" : " " + cStakes)
                    + (sStakes == -1 ? "" : " " + sStakes)
                    + (dealer == -1 ? "" : " " + dealer)
                    + (cards == null ? "" : " " + Arrays.toString(cards))
                    + (error == null ? "" : " " + error);
            return str;
        }
    }
}
