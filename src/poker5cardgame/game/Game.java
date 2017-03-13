package poker5cardgame.game;

import java.util.Arrays;
import poker5cardgame.io.Source;
import java.util.HashMap;
import java.util.Map;

// TODO URGENT solve error after sending DRAW_SERVER (comUtils sends a Protocol error in the second round betting)
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
                    state.transitions.put(Action.FOLD, State.SHOWDOWN); // TODO to know: edited State.QUIT to SHOWDOWN
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
            // 1) applying a PASS action being in a BETTING_DEALER state
            // 2) applying a CALL action being in a COUNTER state
            if ((data.state == State.BETTING_DEALER && action == Action.PASS) ||
                (data.state == State.COUNTER && action == Action.CALL)) 
            {
                if (!data.isSecondRound())
                    data.setSecondRound(true);
                else 
                {
                    // Apply the action show after finishing the 2nd round
                    this.apply(Action.SHOW); // TODO set data, better do that with a flag?
                    return;
                }
            }

            // Set the next state as actual
            State nextState = data.state.transitions.get(action);
            System.out.println("[DEBUG Game] " +data.state + " -> " + action + " -> " + nextState + " (2nd round: " + data.isSecondRound()+")"); // TODO delete print
            data.state = nextState;
        } 
    }

    /**
     * Run the game with the next iteration of commands
     */
    public void update() {
        // TODO @sonia Acabar la logica de tots els estats i comprovar que seguim el protocol
        // TODO @sonia metode pq cmove sigui valid 

        Move sMove, cMove;
        System.out.println("[DEBUG Game] " + data);

        switch (data.state) {

            case INIT:
                // Turn: CLIENT
                // Expected move: START
                
                cMove = this.getClientValidMove(data.state);
                apply(cMove.action);
                break;

            case START:
                // Turn: SERVER
                // Move to send: ANTE_STAKES
                // Parameters: CHIPS (minimum bet), CHIPS (client chips), CHIPS (server chips)
                
                sMove = new Move();
                sMove.action = Action.ANTE_STAKES;
                
                sMove.chips = data.minBet;     // ANTE parameter
                sMove.cStakes = data.cChips;   // STAKES parameter
                sMove.sStakes = data.sChips;   // STAKES parameter
                
                source.sendMove(sMove);
                apply(sMove.action);
                break;

            case ACCEPT_ANTE:
                // Turn: CLIENT
                // Expected move: ANTE_OK or QUIT
                
                cMove = this.getClientValidMove(data.state);
                apply(cMove.action);
                break;

            case PLAY:
                // Turn: SERVER
                // Move to send: DEALER_HAND
                // Parameters: '0' (dealer = server) or '1' (dealer = client), client hand
               
                // the game is accepted, so set the minimum bet as the bet of each player
                data.cBet = data.minBet;
                data.sBet = data.minBet;
                
                sMove = new Move();
                sMove.action = Action.DEALER_HAND;
                
                // choose the dealer randomly (0: server; 1: client)
                sMove.dealer = Math.random() > 0.5 ? 1 : 0;
                data.setServerTurn(sMove.dealer == 1); // the non dealer has the next turn
                
                // generate the server and the client hands
                data.deck.generate();                
                data.cHand.generate(data.deck);
                data.sHand.generate(data.deck);
                sMove.cards = new Card[Hand.SIZE];
                data.cHand.getCards().toArray(sMove.cards);

                source.sendMove(sMove);
                apply(sMove.action);                
                break;

            case BETTING:                
                // Turn: non dealer = SERVER
                // Move to send: BET or PASS
                // Parameters: CHIPS (if bet) or none (if PASS)     
                if (data.isServerTurn()) 
                {
                    // TODO @sonia RandomIA(= no IA) and IntelligentIA
                    // sMove = ia.getMoveForGame(Game g)
                    // NOW implemented with PASS example
                    
                    sMove = new Move();
                    sMove.action = Action.PASS;
                                    
                    source.sendMove(sMove);                                        
                    data.setServerTurn(!data.isServerTurn());
                    apply(sMove.action);
                }
                // Turn: non dealer = CLIENT
                // Expected move: BET or PASS
                else 
                {
                    cMove = this.getClientValidMove(data.state);
                    this.manageBetAndRaise(cMove);
                    data.setServerTurn(!data.isServerTurn());
                    apply(cMove.action);

                }
                break;

            case BETTING_DEALER:
                // TODO same as BETTING? considero que ara mateix si pq no hi ha IA           
    
                // Turn: dealer = SERVER
                // Move to send: BET or PASS
                // Parameters: CHIPS (if bet) or none (if PASS)    
                if (data.isServerTurn()) 
                {
                    // TODO @sonia RandomIA(= no IA) and IntelligentIA
                    // sMove = ia.getMoveForGame(Game g)
                    // NOW implemented with PASS example
                    
                    sMove = new Move();
                    sMove.action = Action.PASS;
                                    
                    source.sendMove(sMove);
                    data.setServerTurn(!data.isServerTurn());
                    apply(sMove.action);
                }
                // Turn: dealer = CLIENT
                // Expected move: BET or PASS
                else 
                {
                    cMove = this.getClientValidMove(data.state);
                    this.manageBetAndRaise(cMove);
                    data.setServerTurn(!data.isServerTurn());
                    apply(cMove.action);
                }
                break;

            case COUNTER:
                if (data.isServerTurn())
                {
                    // TODO ia
                    
                    sMove = new Move();
                    sMove.action = Action.CALL;
                    
                    source.sendMove(sMove);
                    data.setServerTurn(!data.isServerTurn());
                    apply(sMove.action);
                }
                else
                {
                    cMove = this.getClientValidMove(data.state);
                    this.manageBetAndRaise(cMove);
                    this.manageFold(cMove);
                    data.setServerTurn(!data.isServerTurn());
                    apply(cMove.action);
                }                
                break;

            case DRAW:
                // Turn: CLIENT
                // Expected move: DRAW
                
                cMove = this.getClientValidMove(data.state);
                
                // TODO manage if the cards are correct (of the hand) and send an error message if not
                if(cMove.cards.length != 0)
                {
                    data.cHand.discard(cMove.cards);
                    data.cHand.putNCards(data.deck, cMove.cards.length);
                }
                
                apply(cMove.action);
                break;

            case DRAW_SERVER:
                // Turn: SERVER
                // Move to send: DRAW_SERVER
                
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

                if (!data.isFold()) {
                    sMove = new Move();
                    sMove.action = Action.SHOW;
                    sMove.cards = new Card[Hand.SIZE];
                    data.sHand.getCards().toArray(sMove.cards);
                    source.sendMove(sMove);
                }

                data.setFold(false);
                sMove = new Move();
                sMove.action = Action.STAKES;
                sMove.cStakes = data.cChips;   // STAKES parameter
                sMove.sStakes = data.sChips;   // STAKES parameter

                source.sendMove(sMove);
                apply(sMove.action);
                break;

            case QUIT:
                apply(Action.QUIT);
                break;
        }
    }

    private void manageBetAndRaise(Move move) {

        if ((move.action.equals(Action.BET) || move.action.equals(Action.RAISE)) && move.chips > data.minBet) {
            if (data.isServerTurn()) {
                if (data.sChips >= move.chips) {
                    data.sChips -= move.chips;
                    data.sBet += move.chips;
                }
            } else if (data.cChips >= move.chips) {
                data.cChips -= move.chips;
                data.cBet += move.chips;
            }

        } else {
            this.manageBetAndRaise(this.sentNotValidChipsValue());
        }
    }
    
    private void manageFold(Move move)
    {
     if (move.action.equals(Action.FOLD)) {
            data.setFold(true);
            if(data.isServerTurn())
                data.cChips += data.sBet + data.cBet;
            else
                data.sChips += data.sBet + data.cBet;
            data.cBet = 0;
            data.sBet = 0;
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
        while (!data.state.transitions.containsKey(cMove.action))
            cMove = this.sendProtocolErrorMsg(actualState);
        
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
