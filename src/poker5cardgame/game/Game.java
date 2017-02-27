package poker5cardgame.game;

import com.sun.javafx.image.impl.ByteBgr;
import java.util.HashMap;
import java.util.Map;

/**
 * Finite State Machine for the Game State
 */
public class Game {

    protected State state = State.INIT;

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
        SHOW
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

    public Game() {
    }
    
    boolean round2 = false;

    public void apply(Game.Action action) {
        if (this.state.transitions.containsKey(action)) {
            
            if(action == Action.STAKES)
                round2 = false;
            
            // Special Case for 1st and 2nd Round Betting
            if((state == State.BETTING_DEALER && action == Action.PASS) ||
               (state == State.COUNTER && action == Action.CALL)){
                if(!round2)
                    round2 = true;
                else{
                    this.apply(Action.SHOW);
                    return;
                }
            }
            
            State newState = this.state.transitions.get(action);
            System.out.println(state + " -> "+ action +" ->" + newState + " R2:" + round2);
            this.state = newState;
        } else {
            System.out.println("--- ILLEGAL ACTION");
            System.out.println("--- "+state +" DOES NOT ACCEPT " + action);
        }
    }

    public Game.State getState() {
        return state;
    }

}
