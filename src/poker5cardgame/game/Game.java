package poker5cardgame.game;

import java.util.HashMap;
import java.util.Map;
import poker5cardgame.network.Network;

/**
 * Finite State Machine for the Game State
 */
public class Game {

    // Game Resources
    private Source source;
    private Deck deck;
    private Hand sHand;
    private Hand cHand;

    // Game Flags
    protected State state = State.INIT;
    private boolean round2 = false;
    private boolean isServerTurn;
    private int anteBet = 100;
    private int serverChips = 10000, clientChips;
    private int clientBet, serverBet;

    /**
     * Create a new Game Instance with the given source. Which is in charge of
     * providing the game with the means of communicating with the exterior
     * world in the language the game understands, Actions.
     *
     * @param source
     */
    public Game(Source source) {
        this.source = source;

        deck = new Deck();
        sHand = new Hand();
        cHand = new Hand();
    }

    /**
     * Run the game with the next iteration of commands
     */
    public void update() {
        // TODO implement game logic for all states
        Move cMove;
        Move sMove;

        switch (this.state) {

            case INIT:
                // Game Init, waiting for the START action
                cMove = source.getNextMove();
                apply(cMove.action);
                break;

            case START:
                // Game has begun, we now send the ANTE_STAKES action,
                // which will send 2 packets to the client and advance the
                // Game state
                sMove = new Move();
                sMove.action = Action.ANTE_STAKES;
                sMove.chips = anteBet;
                sMove.cStakes = 1000; // TODO Read client stakes from some stakes database indexed by client
                sMove.sStakes = serverChips;
                source.sendMove(sMove);
                apply(sMove.action);
                break;

            case ANTE:
                // We wait for ANTE_OK or QUIT
                cMove = source.getNextMove();
                apply(cMove.action);
                break;

            case PLAY:
                // Player has agreed, so now we set min bet and decide who bets first
                clientBet = anteBet;
                serverBet = anteBet;
                sMove = new Move();
                sMove.action = Action.DEALER_HAND;

                // Decide on a dealer and update the flag
                sMove.dealer = Math.random() > 0.5 ? 1 : 0;
                isServerTurn = sMove.dealer == 1;
                
                // Shuffle the Deck and draw a hand.
                deck.generate();
                cHand.draw5FromDeck(deck);
                sHand.draw5FromDeck(deck);
                
                sMove.cards = new Card[5];
                cHand.getCards().toArray(sMove.cards);

                source.sendMove(sMove);
                apply(sMove.action);
                break;

            case BETTING:
                if (isServerTurn) {
                    // On our turn, we pass
                    sMove = new Move();
                    sMove.action = Action.PASS;
                    source.sendMove(sMove);

                    isServerTurn = !isServerTurn;
                    apply(sMove.action);
                } else {
                    // On player turn, we read his move
                    cMove = source.getNextMove();
                    if (cMove.action == Action.BET) {
                        clientBet += cMove.chips;
                    }
                    apply(cMove.action);
                }
                break;

            case BETTING_DEALER:
                break;

            case COUNTER:
                break;

            case DRAW:
                break;

            case DRAW_SERVER:
                break;

            case SHOWDOWN:
                break;

            case QUIT:
                break;
        }
    }

    public void apply(Game.Action action) {
        if (action == Action.NOOP)
            return;

        if (this.state.transitions.containsKey(action)) {

            if (action == Action.STAKES)
                round2 = false;

            // Special Case for 1st and 2nd Round Betting
            if ((state == State.BETTING_DEALER && action == Action.PASS)
                    || (state == State.COUNTER && action == Action.CALL)) {
                if (!round2)
                    round2 = true;
                else {
                    this.apply(Action.SHOW);
                    return;
                }
            }

            State newState = this.state.transitions.get(action);
            System.out.println(state + " -> " + action + " -> " + newState + " R2:" + round2);
            this.state = newState;
        } else {
            System.out.println("--- ILLEGAL ACTION");
            System.out.println("--- " + state + " DOES NOT ACCEPT " + action);

            // Special case for NetworkSoruce, send an error packet
            if (source instanceof NetworkSource) {
                String[] msg = {"Illegal Action Error"};
                Network.Packet packet = new Network.Packet(Network.Command.ERROR, msg);
                NetworkSource src = (NetworkSource)source;
                src.getNetwork().write_NetworkPacket(packet);
            }
            
        }
    }

    public Game.State getState() {
        return state;
    }

    /**
     * Class defining a move made by either player, affecting the game.
     */
    public static class Move {

        Game.Action action;
        // Client Game ID
        int id;
        // Generic chips param for ANTE, BET or RAISE
        int chips;
        // Client and server Stakes
        int cStakes, sStakes;
        // Dealer Flag, 1 for Client dealer, 0 for server dealer
        int dealer;
        // Array of cards to deal or discard
        Card[] cards;

        public Move() {
            action = Action.NOOP;
        }
    }

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
        NOOP
    }

    // TODO Evaluate best possible state list
    public enum State {
        INIT,
        START,
        ANTE,
        PLAY,
        BETTING,
        BETTING_DEALER,
        COUNTER,
        DRAW,
        DRAW_SERVER,
        SHOWDOWN,
        QUIT;

        Map<Game.Action, Game.State> transitions;

        State() {
            transitions = new HashMap<>();
        }

    }

    // Define State Transitions
    static {
        for (State state : State.values()) {
            switch (state) {
                case INIT:
                    state.transitions.put(Action.START, State.START);
                    break;
                case START:
                    state.transitions.put(Action.ANTE_STAKES, State.ANTE);
                    break;
                case ANTE:
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
                    state.transitions.put(Action.STAKES, State.ANTE);
                    break;

            }
        }
    }

}
